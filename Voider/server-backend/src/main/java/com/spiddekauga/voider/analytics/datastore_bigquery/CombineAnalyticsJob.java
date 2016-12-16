package com.spiddekauga.voider.analytics.datastore_bigquery;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.MapReduceResult;
import com.google.appengine.tools.pipeline.Job3;
import com.google.appengine.tools.pipeline.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Combine Sessions, scenes and events into one list
 */
class CombineAnalyticsJob
		extends
		Job3<List<Session>, MapReduceResult<List<Session>>, MapReduceResult<List<Scene>>, MapReduceResult<List<Event>>> {

@Override
public Value<List<Session>> run(MapReduceResult<List<Session>> sessionsNew,
								MapReduceResult<List<Scene>> scenesNew, MapReduceResult<List<Event>> eventsNew) throws Exception {
	List<Session> updatedSessions = new ArrayList<>();

	// Map sessions for faster access
	Map<Key, Session> sessionMap = new HashMap<>();
	for (Session session : sessionsNew.getOutputResult()) {
		sessionMap.put(session.getKey(), session);
		updatedSessions.add(session);
	}

	// Add scenes to sessions and map scenes for faster access
	Map<Key, Scene> sceneMap = new HashMap<>();
	for (Scene scene : scenesNew.getOutputResult()) {
		sceneMap.put(scene.getKey(), scene);
		Session session = sessionMap.get(scene.getSessionKey());
		session.addScene(scene);
	}

	// Add events to scenes
	for (Event event : eventsNew.getOutputResult()) {
		Scene scene = sceneMap.get(event.getSceneKey());
		scene.addEvent(event);
	}

	return immediate(updatedSessions);
}

@Override
public String getJobDisplayName() {
	return "Combine Analytics Results";
}
}
