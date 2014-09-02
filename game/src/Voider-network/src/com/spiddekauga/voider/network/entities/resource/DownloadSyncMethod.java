package com.spiddekauga.voider.network.entities.resource;

import java.util.Date;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method to check if there are any new published resources to download.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class DownloadSyncMethod implements IMethodEntity {
	/** Date of last sync */
	public Date lastSync = null;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.DOWNLOAD_SYNC;
	}

}
