package com.spiddekauga.voider.network.entities.stat;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response from syncing statistics
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class StatSyncMethodResponse implements IEntity, ISuccessStatuses {
	/** Stats to sync to client */
	public StatSyncEntity syncEntity = new StatSyncEntity();
	/** Response status */
	public Statuses status = null;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	/** Response statuses */
	public enum Statuses implements ISuccessStatuses {
		/** SUCCESS */
		SUCCESS,
		/** Failed internal error */
		FAILED_INTERNAL,
		/** Failed to connect to server */
		FAILED_CONNECTION,
		/** Failed user is not logged in */
		FAILED_USER_NOT_LOGGED_IN,

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}
