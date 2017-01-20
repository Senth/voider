package com.spiddekauga.voider.analytics.export;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.pipeline.Job1;
import com.google.appengine.tools.pipeline.Value;
import com.spiddekauga.appengine.DatastoreUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Clean-up job after analytics have been imported
 */
class CleanupDatastoreJob extends Job1<Void, List<Session>> {
@Override
public Value<Void> run(List<Session> sessions) throws Exception {

	// Get all datastore keys for all entities to update
	List<Key> keys = new ArrayList<>();
	for (Session session : sessions) {
		keys.add(session.getKey());
		for (Scene scene : session.getScenes()) {
			keys.add(scene.getKey());
			for (Event event : scene.getEvents()) {
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
