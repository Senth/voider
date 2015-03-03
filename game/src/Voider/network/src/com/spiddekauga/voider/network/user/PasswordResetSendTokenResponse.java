package com.spiddekauga.voider.network.user;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Response from the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class PasswordResetSendTokenResponse implements IEntity {
	/** Response status from the server */
	public Statuses status = Statuses.FAILED_SERVER_CONNECTION;

	/**
	 * Password reset response statuses
	 */
	public enum Statuses {
		/** Successfully sent a password reset token */
		SUCCESS,
		/** No user with that email */
		FAILED_EMAIL,
		/** Internal server error */
		FAILED_SERVER_ERROR,
		/** Failed to connect to the server */
		FAILED_SERVER_CONNECTION,
	}
}
