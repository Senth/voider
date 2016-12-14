package com.spiddekauga.voider.analytics;

import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.appengine.tools.mapreduce.GoogleCloudStorageFileSet;
import com.google.appengine.tools.mapreduce.MapJob;
import com.google.appengine.tools.mapreduce.MapReduceResult;
import com.google.appengine.tools.mapreduce.MapSettings;
import com.google.appengine.tools.mapreduce.MapSpecification;
import com.google.appengine.tools.mapreduce.MapSpecification.Builder;
import com.google.appengine.tools.mapreduce.inputs.DatastoreInput;
import com.google.appengine.tools.mapreduce.outputs.BigQueryStoreResult;
import com.google.appengine.tools.pipeline.FutureValue;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Job1;
import com.google.appengine.tools.pipeline.Job3;
import com.google.appengine.tools.pipeline.JobSetting;
import com.google.appengine.tools.pipeline.Value;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.pipeline.BigQueryLoadGoogleCloudStorageFilesJob;
import com.spiddekauga.appengine.pipeline.BigQueryLoadJobReference;

/**
 * Converts new analytics in Datastore to Google Cloud Storage. This class has several
 * children jobs.
 * <ol>
 * <li>Get new analytics session</li>
 * <li>Get scenes for all sessions</li>
 * <li>Get events for all the scenes</li>
 * <li>Save JSON output in Google Cloud Storage</li>
 * </ol>

 */
@SuppressWarnings("serial")
public class AnalyticsToBigQueryJob extends Job0<Void> {

	@Override
	public Value<Void> run() throws Exception {
		// Sessions
		FutureValue<MapReduceResult<List<AnalyticsSession>>> sessionsFuture = futureCall(new MapJob<>(getSessionJobSpec(), getMapSettings()),
				getJobSettings());

		// Scenes
		FutureValue<MapReduceResult<List<AnalyticsScene>>> sceneFuture = futureCall(new MapJob<>(getSceneJobSpec(), getMapSettings()),
				getJobSettings());

		// Events
		FutureValue<MapReduceResult<List<AnalyticsEvent>>> eventFuture = futureCall(new MapJob<>(getEventJobSpec(), getMapSettings()),
				getJobSettings());

		// Combine results
		FutureValue<List<AnalyticsSession>> combinedSessionsFuture = futureCall(new CombineAnalyticsListsJob(), sessionsFuture, sceneFuture,
				eventFuture, getJobSettings());

		// To Cloud Storage
		FutureValue<Void> cloudFuture = futureCall(new CloudStoreJob(), combinedSessionsFuture, getJobSettings());

		// To Big Query
		FutureValue<List<BigQueryLoadJobReference>> bigQueryFuture = futureCall(new ImportToBigQueryJob(), getJobSettings(waitFor(cloudFuture)));

		// CleanupServlet
		FutureValue<Void> updateDatastore = futureCall(new CleanupJob(), combinedSessionsFuture, getJobSettings(waitFor(bigQueryFuture)));

		return updateDatastore;
	}

	/**
	 * Combine Sessions, scenes and events into one list
	 */
	private static class CombineAnalyticsListsJob
			extends
			Job3<List<AnalyticsSession>, MapReduceResult<List<AnalyticsSession>>, MapReduceResult<List<AnalyticsScene>>, MapReduceResult<List<AnalyticsEvent>>> {

		@Override
		public Value<List<AnalyticsSession>> run(MapReduceResult<List<AnalyticsSession>> sessionsNew,
				MapReduceResult<List<AnalyticsScene>> scenesNew, MapReduceResult<List<AnalyticsEvent>> eventsNew) throws Exception {
			List<AnalyticsSession> updatedSessions = new ArrayList<>();

			// Map sessions for faster access
			Map<Key, AnalyticsSession> sessionMap = new HashMap<>();
			for (AnalyticsSession session : sessionsNew.getOutputResult()) {
				sessionMap.put(session.getKey(), session);
				updatedSessions.add(session);
			}

			// Add scenes to sessions and map scenes for faster access
			Map<Key, AnalyticsScene> sceneMap = new HashMap<>();
			for (AnalyticsScene scene : scenesNew.getOutputResult()) {
				sceneMap.put(scene.getKey(), scene);
				AnalyticsSession session = sessionMap.get(scene.getSessionKey());
				session.addScene(scene);
			}

			// Add events to scenes
			for (AnalyticsEvent event : eventsNew.getOutputResult()) {
				AnalyticsScene scene = sceneMap.get(event.getSceneKey());
				scene.addEvent(event);
			}

			return immediate(updatedSessions);
		}

		@Override
		public String getJobDisplayName() {
			return "Combine Analytics Results";
		}
	}

	/**
	 * Job for printing all the new analytics session to the google cloud storage
	 */
	private static class CloudStoreJob extends Job1<Void, List<AnalyticsSession>> {
		@Override
		public Value<Void> run(List<AnalyticsSession> sessions) throws Exception {
			ObjectMapper jackson = new ObjectMapper();
			GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
			StringBuilder builder = new StringBuilder();
			// Logger logger = Logger.getLogger("CloudStoreJob");

			for (AnalyticsSession session : sessions) {
				String sessionJson = jackson.writeValueAsString(session);
				builder.append(sessionJson).append('\n');
			}

			String json = builder.toString();


			GcsFileOptions.Builder optionBuilder = new GcsFileOptions.Builder();
			optionBuilder.contentEncoding("UTF-8");
			optionBuilder.mimeType("application/json");
			GcsOutputChannel outputChannel = gcsService.createOrReplace(GCS_FILENAME, optionBuilder.build());
			PrintWriter printWriter = new PrintWriter(Channels.newWriter(outputChannel, "UTF-8"));
			printWriter.write(json);
			printWriter.close();

			return immediate(null);
		}

		@Override
		public String getJobDisplayName() {
			return "Save JSON in Cloud Storage";
		}
	}

	/**
	 * Job for importing analytics to the google cloud storage
	 */
	private static class ImportToBigQueryJob extends Job0<List<BigQueryLoadJobReference>> {
		@Override
		public Value<List<BigQueryLoadJobReference>> run() throws Exception {

			BigQueryLoadGoogleCloudStorageFilesJob bigQueryJob = new BigQueryLoadGoogleCloudStorageFilesJob(AnalyticsConfig.BIG_DATASET_NAME,
					AnalyticsConfig.BIG_TABLE_NAME, APP_ID);

			List<String> fileNames = new ArrayList<>();
			fileNames.add(FILENAME);
			GoogleCloudStorageFileSet fileSet = new GoogleCloudStorageFileSet(BUCKET_NAME, fileNames);
			BigQueryStoreResult<GoogleCloudStorageFileSet> storeResult = new BigQueryStoreResult<GoogleCloudStorageFileSet>(fileSet,
					AnalyticsConfig.getClientSchema());

			return futureCall(bigQueryJob, immediate(storeResult), getJobSettings());
		}

		@Override
		public String getJobDisplayName() {
			return "Import to Big Query";
		}
	}

	/**
	 * Clean-up job after analytics have been imported
	 */
	private static class CleanupJob extends Job1<Void, List<AnalyticsSession>> {
		@Override
		public Value<Void> run(List<AnalyticsSession> sessions) throws Exception {

			// Get all datastore keys for all entities to update
			List<Key> keys = new ArrayList<>();
			for (AnalyticsSession session : sessions) {
				keys.add(session.getKey());
				for (AnalyticsScene scene : session.getScenes()) {
					keys.add(scene.getKey());
					for (AnalyticsEvent event : scene.getEvents()) {
						keys.add(event.getKey());
					}
				}
			}

			// Datastore -> Delete
			DatastoreUtils.delete(keys);

			return immediate(null);
		}

		@Override
		public String getJobDisplayName() {
			return "Clean-up";
		}
	}

	/**
	 * @param settings optional extra parameters
	 * @return job settings
	 */
	public static JobSetting[] getJobSettings(JobSetting... settings) {
		return AnalyticsConfig.getJobSettings(settings);
	}

	/**
	 * @return map settings
	 */
	private static MapSettings getMapSettings() {
		return AnalyticsConfig.getMapSettings();
	}

	/**
	 * @return session MapReduce job specification
	 */
	private static MapSpecification<Entity, AnalyticsSession, List<AnalyticsSession>> getSessionJobSpec() {
		Query query = new Query("analytics_session");
		query.setFilter(new Query.FilterPredicate("exported", FilterOperator.EQUAL, false));
		DatastoreInput input = new DatastoreInput(query, AnalyticsConfig.SHARDS_PER_QUERY);
		SessionMapper mapper = new SessionMapper();
		SessionOutput output = new SessionOutput();

		Builder<Entity, AnalyticsSession, List<AnalyticsSession>> builder = new Builder<>(input, mapper, output);
		builder.setJobName("Analytics Session (Datastore -> Object)");
		return builder.build();
	}

	/**
	 * @return scene MapReduce job specification
	 */
	private static MapSpecification<Entity, AnalyticsScene, List<AnalyticsScene>> getSceneJobSpec() {
		Query query = new Query("analytics_scene");
		query.setFilter(new Query.FilterPredicate("exported", FilterOperator.EQUAL, false));
		DatastoreInput input = new DatastoreInput(query, AnalyticsConfig.SHARDS_PER_QUERY);
		SceneMapper mapper = new SceneMapper();
		SceneOutput output = new SceneOutput();

		Builder<Entity, AnalyticsScene, List<AnalyticsScene>> builder = new Builder<>(input, mapper, output);
		builder.setJobName("Analytics Scene (Datastore -> Object)");
		return builder.build();
	}

	/**
	 * @return event MapReduce job specification
	 */
	private static MapSpecification<Entity, AnalyticsEvent, List<AnalyticsEvent>> getEventJobSpec() {
		Query query = new Query("analytics_event");
		query.setFilter(new Query.FilterPredicate("exported", FilterOperator.EQUAL, false));
		DatastoreInput input = new DatastoreInput(query, AnalyticsConfig.SHARDS_PER_QUERY);
		EventMapper mapper = new EventMapper();
		EventOutput output = new EventOutput();

		Builder<Entity, AnalyticsEvent, List<AnalyticsEvent>> builder = new Builder<>(input, mapper, output);
		builder.setJobName("Analytics Event (Datastore -> Object)");
		return builder.build();
	}


	private static final String APP_ID = SystemProperty.applicationId.get();
	private static final String BUCKET_NAME = APP_ID;
	private static final String FILENAME = "analytics.json";
	private static final GcsFilename GCS_FILENAME = new GcsFilename(BUCKET_NAME, FILENAME);
}
