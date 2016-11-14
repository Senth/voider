package com.spiddekauga.voider.network.resource;

import java.util.Date;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method to check if there are any new published resources to download.

 */
public class DownloadSyncMethod implements IMethodEntity {
	/** Date of last sync */
	public Date lastSync = null;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.DOWNLOAD_SYNC;
	}

}
