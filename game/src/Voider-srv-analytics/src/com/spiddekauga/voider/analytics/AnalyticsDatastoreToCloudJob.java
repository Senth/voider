package com.spiddekauga.voider.analytics;

import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.mapreduce.MapJob;
import com.google.appengine.tools.mapreduce.MapReduceResult;
import com.google.appengine.tools.mapreduce.MapSettings;
import com.google.appengine.tools.mapreduce.MapSpecification;
import com.google.appengine.tools.mapreduce.MapSpecification.Builder;
import com.google.appengine.tools.mapreduce.inputs.DatastoreInput;
import com.google.appengine.tools.pipeline.FutureValue;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Job1;
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
public class AnalyticsDatastoreToCloudJob extends Job0<Void> {

	@Override
	public Value<Void> run() throws Exception {
		// Sessions
		FutureValue<MapReduceResult<List<AnalyticsSession>>> sessionsFuture = futureCall(new MapJob<>(getSessionJobSpec(), getMapSettings()),
				getJobSettings());

		// Scenes
		FutureValue<Void> sceneFuture = futureCall(new SceneJob(), sessionsFuture, getJobSettings());

		// Events
		FutureValue<Void> eventFuture = futureCall(new EventJob(), sessionsFuture, getJobSettings(waitFor(sceneFuture)));

		// To Cloud Storage
		FutureValue<Void> cloudFuture = null;

		// TODO Auto-generated method stub
		return cloudFuture;
	}

	/**
	 * Generator job for getting scenes in found sessions
	 */
	private static class SceneJob extends Job1<Void, MapReduceResult<List<AnalyticsSession>>> {
		@Override
		public Value<Void> run(MapReduceResult<List<AnalyticsSession>> sessions) throws Exception {
			for (AnalyticsSession session : sessions.getOutputResult()) {
				futureCall(new MapJob<>(getSceneJobSpec(session), getMapSettings()), getJobSettings());
			}

			return null;
		}

		/**
		 * @param session
		 * @return scene MapReduce job specification
		 */
		private static MapSpecification<Entity, AnalyticsScene, Void> getSceneJobSpec(AnalyticsSession session) {
			Query query = new Query("analytics_scene", session.getKey());
			DatastoreInput input = new DatastoreInput(query, AnalyticsConfig.SHARDS_PER_QUERY);
			SceneMapper mapper = new SceneMapper();
			SceneOutput output = new SceneOutput(session);

			Builder<Entity, AnalyticsScene, Void> builder = new Builder<>(input, mapper, output);
			builder.setJobName("Analytics Scene (Datastore -> Object)");
			return builder.build();
		}
	}

	/**
	 * Generator job for getting events in found scenes
	 */
	private static class EventJob extends Job1<Void, MapReduceResult<List<AnalyticsSession>>> {
		@Override
		public Value<Void> run(MapReduceResult<List<AnalyticsSession>> sessions) throws Exception {
			for (AnalyticsSession session : sessions.getOutputResult()) {
				for (AnalyticsScene scene : session.getScenes()) {
					futureCall(new MapJob<>(getEventJobSpec(scene), getMapSettings()), getJobSettings());
				}
			}

			return null;
		}

		/**
		 * @param scene
		 * @return event MapReduce job specification
		 */
		private static MapSpecification<Entity, AnalyticsEvent, Void> getEventJobSpec(AnalyticsScene scene) {
			Query query = new Query("analytics_event", scene.getKey());
			DatastoreInput input = new DatastoreInput(query, AnalyticsConfig.SHARDS_PER_QUERY);
			EventMapper mapper = new EventMapper();
			EventOutput output = new EventOutput(scene);

			Builder<Entity, AnalyticsEvent, Void> builder = new Builder<>(input, mapper, output);
			builder.setJobName("Analytics Event (Datastore -> Object");
			return builder.build();
		}
	}

	/**
	 * Job for printing all the new analytics session to the google cloud storage
	 */
	private static class CloudStoreJob extends Job1<Void, MapReduceResult<List<AnalyticsSession>>> {
		@Override
		public Value<Void> run(MapReduceResult<List<AnalyticsSession>> sessions) throws Exception {
			StringBuilder builder = new StringBuilder();

			// TODO

			String json = builder.toString();

			return null;
		}

		GcsService mGcsService = GcsServiceFactory.createGcsService();

	}

	/**
	 * @param settings optional extra parameters
	 * @return job settings
	 */
	private static JobSetting[] getJobSettings(JobSetting... settings) {
		JobSetting[] defaultSettings = new JobSetting[] { onQueue(QUEUE_NAME), onModule(MODULE_NAME) };

		if (settings.length == 0) {
			return defaultSettings;
		} else {
			JobSetting[] combinedSettings = new JobSetting[settings.length + defaultSettings.length];
			for (int i = 0; i < defaultSettings.length; i++) {
				combinedSettings[i] = defaultSettings[i];
			}
			int offset = defaultSettings.length;
			for (int i = 0; i < settings.length; i++) {
				combinedSettings[i + offset] = settings[i];
			}
			return combinedSettings;
		}
	}

	/**
	 * @return map settings
	 */
	private static MapSettings getMapSettings() {
		return new MapSettings.Builder().setWorkerQueueName(QUEUE_NAME).setModule(MODULE_NAME).build();
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

	private static final String QUEUE_NAME = "analytics-queue";
	private static final String MODULE_NAME = "analytics";
}
