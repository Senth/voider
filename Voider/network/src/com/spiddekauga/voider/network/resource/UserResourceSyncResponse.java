package com.spiddekauga.voider.network.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response from when syncing user resource revisions

 */
public class UserResourceSyncResponse implements IEntity, ISuccessStatuses {
	/** Upload status */
	public UploadStatuses uploadStatus = null;
	/** Download status */
	public boolean downloadStatus = true;
	/** All conflicting resources */
	public HashMap<UUID, ResourceConflictEntity> conflicts = new HashMap<>();
	/** Failed uploads */
	public HashMap<UUID, HashSet<Integer>> failedUploads = new HashMap<>();
	/** Resources to download */
	public HashMap<UUID, ArrayList<ResourceRevisionBlobEntity>> blobsToDownload = new HashMap<>();
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
		/** Successfully uploaded some resources, but conflicts exists */
		SUCCESS_CONFLICTS,
		/** Successfully uploaded some resources, but failed on some */
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
