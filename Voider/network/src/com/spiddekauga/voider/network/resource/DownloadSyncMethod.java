package com.spiddekauga.voider.network.resource;

import com.spiddekauga.voider.network.entities.IMethodEntity;

import java.util.Date;

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
