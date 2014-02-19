package com.spiddekauga.voider.network.entities.method;

import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Response from registering a new user
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class RegisterUserMethodResponse implements IEntity {
	/** If the register was a success */
	public StatusResponses status;
	/** Private key, for logging in automatically without password */
	public UUID privateKey = null;

	/**
	 * Response statuses when registering users
	 */
	public enum StatusResponses {
		/** Successfully created user */
		SUCCESS,
		/** Email already in use */
		FAIL_EMAIL_EXISTS,
		/** Username already in use */
		FAIL_USERNAME_EXISTS,
		/** Internal server error */
		FAIL_SERVER
	}
}
