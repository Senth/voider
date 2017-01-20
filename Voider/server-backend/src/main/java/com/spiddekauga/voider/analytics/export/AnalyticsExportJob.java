package com.spiddekauga.voider.analytics.export;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.tools.mapreduce.MapJob;
import com.google.appengine.tools.mapreduce.MapReduceResult;
import com.google.appengine.tools.mapreduce.MapSettings;
import com.google.appengine.tools.mapreduce.MapSpecification;
import com.google.appengine.tools.mapreduce.MapSpecification.Builder;
import com.google.appengine.tools.mapreduce.inputs.DatastoreInput;
import com.google.appengine.tools.pipeline.FutureValue;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.JobSetting;
import com.google.appengine.tools.pipeline.Value;
import com.spiddekauga.appengine.pipeline.BigQueryLoadJobReference;
import com.spiddekauga.voider.BackendConfig;

import java.util.List;

/**
 * Converts new analytics in Datastore to Google Cloud Storage. This class has several child jobs.
 * <ol> <li>Get new analytics session</li> <li>Get scenes for all sessions</li> <li>Get events for
 * all the scenes</li> <li>Save JSON output in Google Cloud Storage</li> </ol>
 */
@SuppressWarnings("serial")
class AnalyticsExportJob extends Job0<Void> {

@Override
public Value<Void> run() throws Exception {
	// Sessions
	FutureValue<MapReduceResult<List<Session>>> sessionsFuture = futureCall(new MapJob<>(getSessionJobSpec(), getMapSettings()),
			getJobSettings());

	// Scenes
	FutureValue<MapReduceResult<List<Scene>>> sceneFuture = futureCall(new MapJob<>(getSceneJobSpec(), getMapSettings()),
			getJobSettings());

	// Events
	FutureValue<MapReduceResult<List<Event>>> eventFuture = futureCall(new MapJob<>(getEventJobSpec(), getMapSettings()),
			getJobSettings());

	// Combine results
	FutureValue<List<Session>> combinedSessionsFuture = futureCall(new CombineAnalyticsJob(), sessionsFuture, sceneFuture,
			eventFuture, getJobSettings());

	// To Cloud Storage
	FutureValue<Void> cloudFuture = futureCall(new CloudStoreJob(), combinedSessionsFuture, getJobSettings());

	// To Big Query
	FutureValue<List<BigQueryLoadJobReference>> bigQueryFuture = futureCall(new ImportToBigQueryJob(), getJobSettings(waitFor(cloudFuture)));

	// CleanupServlet
	return futureCall(new CleanupDatastoreJob(), combinedSessionsFuture, getJobSettings(waitFor(bigQueryFuture)));
}

/**
 * @return session MapReduce job specification
 */
private static MapSpecification<Entity, Session, List<Session>> getSessionJobSpec() {
	Query query = new Query("analytics_session");
	query.setFilter(new Query.FilterPredicate("exported", FilterOperator.EQUAL, false));
	DatastoreInput input = new DatastoreInput(query, BackendConfig.SHARDS_PER_QUERY);
	SessionMapper mapper = new SessionMapper();
	SessionOutput output = new SessionOutput();

	Builder<Entity, Session, List<Session>> builder = new Builder<>(input, mapper, output);
	builder.setJobName("Analytics Session (Datastore -> Object)");
	return builder.build();
}

/**
 * @return map settings
 */
private static MapSettings getMapSettings() {
	return BackendConfig.getMapSettings();
}

/**
 * @param jobSettings additional job settings
 * @return default job settings
 */
private static JobSetting[] getJobSettings(JobSetting... jobSettings) {
	return BackendConfig.getJobSettings(jobSettings);
}

/**
 * @return scene MapReduce job specification
 */
private static MapSpecification<Entity, Scene, List<Scene>> getSceneJobSpec() {
	Query query = new Query("analytics_scene");
	query.setFilter(new Query.FilterPredicate("exported", FilterOperator.EQUAL, false));
	DatastoreInput input = new DatastoreInput(query, BackendConfig.SHARDS_PER_QUERY);
	SceneMapper mapper = new SceneMapper();
	SceneOutput output = new SceneOutput();

	Builder<Entity, Scene, List<Scene>> builder = new Builder<>(input, mapper, output);
	builder.setJobName("Analytics Scene (Datastore -> Object)");
	return builder.build();
}

/**
 * @return event MapReduce job specification
 */
private static MapSpecification<Entity, Event, List<Event>> getEventJobSpec() {
	Query query = new Query("analytics_event");
	query.setFilter(new Query.FilterPredicate("exported", FilterOperator.EQUAL, false));
	DatastoreInput input = new DatastoreInput(query, BackendConfig.SHARDS_PER_QUERY);
	EventMapper mapper = new EventMapper();
	EventOutput output = new EventOutput();

	Builder<Entity, Event, List<Event>> builder = new Builder<>(input, mapper, output);
	builder.setJobName("Analytics Event (Datastore -> Object)");
	return builder.build();
}

}
