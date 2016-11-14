package com.spiddekauga.voider.network.backup;

import java.util.Date;

import com.spiddekauga.voider.network.entities.IMethodEntity;

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
