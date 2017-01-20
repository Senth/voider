package com.spiddekauga.voider.backup;

import com.spiddekauga.appengine.pipeline.backup.DatastoreBackupConfig;
import com.spiddekauga.voider.BackendConfig;

/**
 * Configuration for backup
 */
class BackupConfig {
static final String[] BACKUP_TABLES = {
		"dependency",
		"highscore",
		"level_stat",
		"level_tag",
		"published",
		"sync_published",
		"user_level_stat",
		"user_resources",
		"user_resources_deleted",
		"users",
};

static final DatastoreBackupConfig DATASTORE_BACKUP_CONFIG = new DatastoreBackupConfig.Builder()
		.setMapSettings(BackendConfig.getMapSettings())
		.setJobSettings(BackendConfig.getJobSettings())
		.addBackupTables(BackupConfig.BACKUP_TABLES)
		.build();

}
