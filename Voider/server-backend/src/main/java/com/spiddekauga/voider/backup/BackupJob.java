package com.spiddekauga.voider.backup;

import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Value;

/**
 * Creates a full backup of the Datastore in Google Cloud Storage. Does a full backup. But also
 * removes old backups that probably won't be used anymore.
 */
class BackupJob extends Job0<Void> {
@Override
public Value<Void> run() throws Exception {
	return null;
}
}
