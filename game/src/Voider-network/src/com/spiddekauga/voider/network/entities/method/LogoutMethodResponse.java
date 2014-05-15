package com.spiddekauga.voider.network.entities.method;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Logout response
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LogoutMethodResponse implements IEntity, ISuccessStatuses {
	/** Logout status */
	public Statuses status = null;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	/**
	 * Logout statuses
	 */
	public enum Statuses implements ISuccessStatuses {
		/** Logout successful */
		SUCCESS,
		/** Failed to connect to server, might be offline */
		FAILED_SERVER_CONNECTION,
		/** Internal server error */
		FAILED_SERVER_ERROR,
		/** Already logged out */
		FAILED_NOT_LOGGED_IN,

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}
