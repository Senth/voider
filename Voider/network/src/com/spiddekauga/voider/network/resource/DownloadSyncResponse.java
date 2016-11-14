package com.spiddekauga.voider.network.resource;

import java.util.ArrayList;
import java.util.Date;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * All resources that should be downloaded (synced) from the server

 */
public class DownloadSyncResponse implements IEntity, ISuccessStatuses {
	/** All published resources to download */
	public ArrayList<ResourceBlobEntity> resources = new ArrayList<>();
	/** Sync time */
	public Date syncTime = null;
	/** Status of the sync */
	public Statuses status = null;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	/** Response statuses */
	public enum Statuses implements ISuccessStatuses {
		/** Successfully tested */
		SUCCESS,
		/** User not logged in */
		FAILED_USER_NOT_LOGGED_IN,
		/** Failed internal server error */
		FAILED_INTERNAL,
		/** Connection failed */
		FAILED_CONNECTION,
		/** Failed to download resources */
		FAILED_DOWNLOAD,

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}
