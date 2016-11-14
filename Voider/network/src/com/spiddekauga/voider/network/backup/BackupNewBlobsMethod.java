package com.spiddekauga.voider.network.backup;

import com.spiddekauga.voider.network.entities.IMethodEntity;

import java.util.Date;

/**
 * Backup new blobs
 */
public class BackupNewBlobsMethod implements IMethodEntity {
/** Date of last backup */
public Date lastBackup = null;

@Override
public MethodNames getMethodName() {
	return MethodNames.BACKUP_NEW_BLOBS;
}
}
