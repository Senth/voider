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
import com.google.appengine.tools.pipeline.Job2;
import com.google.appengine.tools.pipeline.JobSetting;
import com.google.appengine.tools.pipeline.Value;
import com.spiddekauga.utils.pipeline.CombineMapResults;
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
		FutureValue<List<AnalyticsSession>> sceneFuture = futureCall(new SceneJob(), sessionsFuture, getJobSettings());

		// Events
		FutureValue<List<AnalyticsSession>> eventFuture = futureCall(new EventJob(), sceneFuture, getJobSettings());

		// To Cloud Storage
		// FutureValue<Void> cloudFuture = futureCall(new CloudStoreJob(), sessionsFuture,
		// getJobSettings(waitFor(eventFuture)));

		// To Big Query
		FutureValue<Void> bigQueryFuture = null;

		// Set sessions as exported
		FutureValue<Void> updateDatastore = null;

		return eventFuture;
	}

	/**
	 * Generator job for getting scenes in found sessions
	 */
	private static class SceneJob extends Job1<List<AnalyticsSession>, MapReduceResult<List<AnalyticsSession>>> {
		@Override
		public Value<List<AnalyticsSession>> run(MapReduceResult<List<AnalyticsSession>> sessions) throws Exception {
			List<FutureValue<MapReduceResult<AnalyticsSession>>> results = new ArrayList<>();
			for (AnalyticsSession session : sessions.getOutputResult()) {
				results.add(futureCall(new MapJob<>(getSceneJobSpec(session), getMapSettings()), getJobSettings()));
			}

			return futureCall(new CombineMapResults<AnalyticsSession>(), futureList(results), getJobSettings());
		}

		/**
		 * @param session
		 * @return scene MapReduce job specification
		 */
		private static MapSpecification<Entity, AnalyticsScene, AnalyticsSession> getSceneJobSpec(AnalyticsSession session) {
			Query query = new Query("analytics_scene", session.getKey());
			DatastoreInput input = new DatastoreInput(query, AnalyticsConfig.SHARDS_PER_QUERY);
			SceneMapper mapper = new SceneMapper();
			SceneOutput output = new SceneOutput(session);

			Builder<Entity, AnalyticsScene, AnalyticsSession> builder = new Builder<>(input, mapper, output);
			builder.setJobName("Analytics Scene (Datastore -> Object)");
			return builder.build();
		}

		@Override
		public String getJobDisplayName() {
			return "Scene Job Generator";
		}
	}

	/**
	 * Generator job for getting events in found scenes
	 */
	private static class EventJob extends Job1<List<AnalyticsSession>, List<AnalyticsSession>> {
		@Override
		public Value<List<AnalyticsSession>> run(List<AnalyticsSession> sessions) throws Exception {
			// StringBuilder logBuilder = new StringBuilder();

			// logBuilder.append("Session information in EventJob");
			// for (AnalyticsSession session : sessions) {
			// if (session != null) {
			// logBuilder.append("\nSession key: ").append(session.getKey());
			// logBuilder.append("\n\tNo. Scenes: ");
			// if (session.getScenes() == null) {
			// logBuilder.append("NULL");
			// } else {
			// logBuilder.append(session.getScenes().size());
			// }
			// } else {
			// logBuilder.append("Session is NULL");
			// }
			// }
			// Logger logger = Logger.getLogger("EventJob");
			// logger.info(logBuilder.toString());


			List<FutureValue<MapReduceResult<AnalyticsScene>>> results = new ArrayList<>();
			for (AnalyticsSession session : sessions) {
				for (AnalyticsScene scene : session.getScenes()) {
					results.add(futureCall(new MapJob<>(getEventJobSpec(scene), getMapSettings()), getJobSettings()));
				}
			}

			FutureValue<List<AnalyticsScene>> scenes = futureCall(new CombineMapResults<AnalyticsScene>(), futureList(results), getJobSettings());

			return futureCall(new UpdateSceneSessions(), immediate(sessions), scenes, getJobSettings());
		}

		/**
		 * @param scene
		 * @return event MapReduce job specification
		 */
		private static MapSpecification<Entity, AnalyticsEvent, AnalyticsScene> getEventJobSpec(AnalyticsScene scene) {
			Query query = new Query("analytics_event", scene.getKey());
			DatastoreInput input = new DatastoreInput(query, AnalyticsConfig.SHARDS_PER_QUERY);
			EventMapper mapper = new EventMapper();
			EventOutput output = new EventOutput(scene);

			Builder<Entity, AnalyticsEvent, AnalyticsScene> builder = new Builder<>(input, mapper, output);
			builder.setJobName("Analytics Event (Datastore -> Object");
			return builder.build();
		}

		@Override
		public String getJobDisplayName() {
			return "Event Job Generator";
		}

		/**
		 * Update session scene information from previous job
		 */
		private static class UpdateSceneSessions extends Job2<List<AnalyticsSession>, List<AnalyticsSession>, List<AnalyticsScene>> {
			@Override
			public Value<List<AnalyticsSession>> run(List<AnalyticsSession> sessionsToUpdate, List<AnalyticsScene> scenesNew) throws Exception {
				// Map scenes for faster access
				Map<Key, AnalyticsScene> scenesToUpdate = new HashMap<>();
				for (AnalyticsSession session : sessionsToUpdate) {
					if (session != null && session.getScenes() != null) {
						for (AnalyticsScene scene : session.getScenes()) {
							scenesToUpdate.put(scene.getKey(), scene);
						}
					}
				}

				// Update scene information
				for (AnalyticsScene sceneNew : scenesNew) {
					AnalyticsScene updateScene = scenesToUpdate.get(sceneNew.getKey());
					updateScene.setEvents(sceneNew.getEvents());
				}


				return immediate(sessionsToUpdate);
			}

			@Override
			public String getJobDisplayName() {
				return "Update Scene Sessions";
			}
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
		builder.setJobName("Analytics session (Datastore -> Object)");
		return builder.build();
	}


	private static final String BUCKET_NAME = SystemProperty.applicationId.get();
	private static final String FILENAME = "analytics.json";
	private static final GcsFilename GCS_FILENAME = new GcsFilename(BUCKET_NAME, FILENAME);
}
