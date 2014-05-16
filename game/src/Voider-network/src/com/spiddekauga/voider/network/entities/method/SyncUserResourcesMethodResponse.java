package com.spiddekauga.voider.network.entities.method;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response from when syncing user resource revisions
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class SyncUserResourcesMethodResponse implements IEntity, ISuccessStatuses {
	/** Response status */
	public Statuses status = null;


	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	/** Success statuses */
	public enum Statuses implements ISuccessStatuses {
		/** Successfully uploaded and synced all resources */
		SUCCESS_ALL,
		/** Successfylly uploaded some resources */
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
