package com.spiddekauga.voider.network.entities.method;

import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Response from registering a new user
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class RegisterUserMethodResponse implements IEntity {
	/** If the register was a success */
	public Statuses status = null;
	/** Private key, for logging in automatically without password */
	public UUID privateKey = null;
	/** User key */
	public String userKey = null;

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
