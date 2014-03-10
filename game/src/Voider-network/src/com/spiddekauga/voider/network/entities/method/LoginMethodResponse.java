package com.spiddekauga.voider.network.entities.method;

import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;


/**
 * Response from the login method
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class LoginMethodResponse implements IEntity {
	/** Username, the user could log in with email, thus reply with the real username */
	public String username = null;
	/** If the login was successful */
	public Statuses status = null;
	/** The private key which can be used to login without a password */
	public UUID privateKey = null;
	/** Userkey */
	public String userKey = null;

	/**
	 * Response statuses
	 */
	public enum Statuses {
		/** Successfully logged in */
		SUCCESS,
		/** Failed due to username password mismatch */
		FAILED_USERNAME_PASSWORD_MISMATCH,
		/** Failed due to could not connect to server */
		FAILED_SERVER_CONNECTION,
		/** Internal server error */
		FAILED_SERVER_ERROR,
	}
}
