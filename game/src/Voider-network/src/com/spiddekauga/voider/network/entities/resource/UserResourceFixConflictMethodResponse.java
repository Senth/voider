package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response from fixing user resource conflicts
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class UserResourceFixConflictMethodResponse implements IEntity, ISuccessStatuses {
	/**
	 * Resources to download. Even if we should keep client resources new resources
	 * could've been added.
	 */
	public HashMap<UUID, ArrayList<ResourceRevisionBlobEntity>> blobsToDownload = new HashMap<>();
	/** Resources to delete */
	public ArrayList<UUID> resourcesToRemove = new ArrayList<>();
	/** Status */
	public Statuses status = null;
	/** New sync date */
	public Date syncTime = null;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	/** Success statuses */
	public enum Statuses implements ISuccessStatuses {
		/** Successfully uploaded and synced all resources */
		SUCCESS,
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
