package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response from when syncing user resource revisions
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class UserResourcesSyncMethodResponse implements IEntity, ISuccessStatuses {
	/** Upload status */
	public UploadStatuses uploadStatus = null;
	/** Download status */
	public boolean downloadStatus = false;
	/** All conflicting resources */
	public HashMap<UUID, ResourceConflictEntity> conflicts = new HashMap<>();
	/** Resources to download */
	public HashMap<UUID, ArrayList<ResourceBlobEntity>> blobsToDownload = new HashMap<>();
	/** Resources to remove */
	public ArrayList<UUID> resourcesToRemove = new ArrayList<>();
	/** Latest sync time */
	public Date syncTime;


	@Override
	public boolean isSuccessful() {
		return uploadStatus != null && downloadStatus && uploadStatus.isSuccessful();
	}

	/** Success statuses */
	public enum UploadStatuses implements ISuccessStatuses {
		/** Successfully uploaded and synced all resources */
		SUCCESS_ALL,
		/** Successfully uploaded some resources */
		SUCCESS_PARTIAL,
		/** Failed internal server error */
		FAILED_INTERNAL,
		/** Failed to connect to server */
		FAILED_CONNECTION,
		/** Failed user not logged in */
		FAILED_USER_NOT_LOGGED_IN,

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}
