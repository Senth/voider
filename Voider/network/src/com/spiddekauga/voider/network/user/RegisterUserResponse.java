package com.spiddekauga.voider.network.user;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

import java.util.UUID;

/**
 * Response from registering a new user
 */
public class RegisterUserResponse implements IEntity, ISuccessStatuses {
/** If the register was a success */
public Statuses status = Statuses.FAIL_SERVER_CONNECTION;
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
	/** Beta/Register key has already been used */
	FAIL_REGISTER_KEY_USED,
	/** Beta/Register key is invalid */
	FAIL_REGISTER_KEY_INVALID,;

	@Override
	public boolean isSuccessful() {
		return name().contains("SUCCESS");
	}
}
}
