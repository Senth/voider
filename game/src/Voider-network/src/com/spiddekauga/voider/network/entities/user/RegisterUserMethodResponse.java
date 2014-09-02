package com.spiddekauga.voider.network.entities.user;

import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response from registering a new user
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class RegisterUserMethodResponse implements IEntity, ISuccessStatuses {
	/** If the register was a success */
	public Statuses status = null;
	/** Private key, for logging in automatically without password */
	public UUID privateKey = null;
	/** User key */
	public String userKey = null;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	/**
	 * Response statuses when registering users
	 */
	public enum Statuses implements ISuccessStatuses {
		/** Successfully created user */
		SUCCESS,
		/** Email already in use */
		FAIL_EMAIL_EXISTS,
		/** Username already in use */
		FAIL_USERNAME_EXISTS,
		/** Username too short */
		FAIL_USERNAME_TOO_SHORT,
		/** Password too short */
		FAIL_PASSWORD_TOO_SHORT,
		/** Internal server error */
		FAIL_SERVER_ERROR,
		/** Server connection error */
		FAIL_SERVER_CONNECTION,

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}
