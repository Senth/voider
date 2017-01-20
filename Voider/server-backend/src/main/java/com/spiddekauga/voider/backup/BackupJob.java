package com.spiddekauga.voider.backup;

import com.google.appengine.tools.pipeline.FutureValue;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Value;
import com.spiddekauga.appengine.pipeline.backup.DatastoreBackupJob;
import com.spiddekauga.voider.BackendConfig;

/**
 * Creates a full backup of the Datastore in Google Cloud Storage. Does a full backup. But also
 * removes old backups that probably won't be used anymore.
 */
class BackupJob extends Job0<Void> {
@Override
public Value<Void> run() throws Exception {
	// Export Datastore to GCS
	FutureValue<Void> exportDatastore = exportDatastore();

	return immediate(null);
}

private FutureValue<Void> exportDatastore() {
	return futureCall(new DatastoreBackupJob(), immediate(BackupConfig.DATASTORE_BACKUP_CONFIG), BackendConfig.getJobSettings());
}

@Override
public String getJobDisplayName() {
	return "Backup Job";
}
}
