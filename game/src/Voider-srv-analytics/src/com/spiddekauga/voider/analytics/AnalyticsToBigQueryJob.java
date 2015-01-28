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
import com.google.appengine.tools.mapreduce.MapJob;
import com.google.appengine.tools.mapreduce.MapReduceResult;
import com.google.appengine.tools.mapreduce.MapSettings;
import com.google.appengine.tools.mapreduce.MapSpecification;
import com.google.appengine.tools.mapreduce.MapSpecification.Builder;
import com.google.appengine.tools.mapreduce.inputs.DatastoreInput;
import com.google.appengine.tools.pipeline.FutureValue;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Job1;
import com.google.appengine.tools.pipeline.Job3;
import com.google.appengine.tools.pipeline.JobSetting;
import com.google.appengine.tools.pipeline.Value;
import com.spiddekauga.voider.config.AnalyticsConfig;

/**
 * Converts new analytics in Datastore to Google Cloud Storage. This class has several
 * children jobs.
 * <ol>
 * <li>Get new analytics session</li>
 * <li>Get scenes for all sessions</li>
 * <li>Get events for all the scenes</li>
 * <li>Save JSON output in Google Cloud Storage</li>
 * </ol>
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class AnalyticsToBigQueryJob extends Job0<List<AnalyticsSession>> {

	@Override
	public Value<List<AnalyticsSession>> run() throws Exception {
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
		// FutureValue<Void> cloudFuture = futureCall(new CloudStoreJob(), sessionsFuture,
		// getJobSettings(waitFor(eventFuture)));

		// To Big Query
		FutureValue<Void> bigQueryFuture = null;

		// Set sessions as exported
		FutureValue<Void> updateDatastore = null;

		return combinedSessionsFuture;
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

	}

	/**
	 * Job for printing all the new analytics session to the google cloud storage
	 */
	private static class CloudStoreJob extends Job1<Void, MapReduceResult<List<AnalyticsSession>>> {
		@Override
		public Value<Void> run(MapReduceResult<List<AnalyticsSession>> sessions) throws Exception {
			StringBuilder builder = new StringBuilder();

			for (AnalyticsSession session : sessions.getOutputResult()) {
				String sessionJson = mJackson.writeValueAsString(session);
				builder.append(sessionJson).append('\n');
			}

			String json = builder.toString();

			GcsFileOptions.Builder optionBuilder = new GcsFileOptions.Builder();
			optionBuilder.contentEncoding("application/javascript");
			GcsOutputChannel outputChannel = mGcsService.createOrReplace(GCS_FILENAME, optionBuilder.build());
			PrintWriter printWriter = new PrintWriter(Channels.newWriter(outputChannel, "UTF8"));
			printWriter.write(json);
			printWriter.close();

			return null;
		}

		@Override
		public String getJobDisplayName() {
			return "Cloud Store Job";
		}

		private ObjectMapper mJackson = new ObjectMapper();
		private final GcsService mGcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
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


	private static final String BUCKET_NAME = SystemProperty.applicationId.get();
	private static final String FILENAME = "analytics.json";
	private static final GcsFilename GCS_FILENAME = new GcsFilename(BUCKET_NAME, FILENAME);
}
