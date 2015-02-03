package com.spiddekauga.voider.network.user;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Response when trying to reset the password
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class PasswordResetResponse implements IEntity {
	/** Response status */
	public Statuses status = Statuses.FAILED_SERVER_CONNECTION;

	/**
	 * Response statuses
	 */
	public enum Statuses {
		/** Successfully change to the new password */
		SUCCESS,
		/** Failed, no matching token */
		FAILED_TOKEN,
		/** Failed, token expired */
		FAILED_EXPIRED,
		/** Internal server error */
		FAILED_SERVER_ERROR,
		/** Server connection errror */
		FAILED_SERVER_CONNECTION,
	}
}
