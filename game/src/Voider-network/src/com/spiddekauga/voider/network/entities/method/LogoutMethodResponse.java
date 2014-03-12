package com.spiddekauga.voider.network.entities.method;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Logout response
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LogoutMethodResponse implements IEntity {
	/** Logout status */
	public Statuses status = null;

	/**
	 * Logout statuses
	 */
	public enum Statuses {
		/** Logout successful */
		SUCCESS,
		/** Failed to connect to server, might be offline */
		FAILED_SERVER_CONNECTION,
		/** Internal server error */
		FAILED_SERVER_ERROR,
		/** Already logged out */
		FAILED_NOT_LOGGED_IN,
	}
}
