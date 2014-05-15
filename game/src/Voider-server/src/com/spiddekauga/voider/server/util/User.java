package com.spiddekauga.voider.server.util;

import java.io.Serializable;
import java.util.UUID;

import com.google.appengine.api.datastore.Key;

/**
 * Logged in user
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class User extends SessionVariable implements Serializable {
	/**
	 * Login the user and set all parameters
	 * @param userId id/key of the user in the datastore
	 * @param clientId where the user logged in from
	 */
	public void login(Key userId, UUID clientId) {
		mId = userId;
		mIsLoggedIn = true;
		mClientId = clientId;
		setChanged();
	}

	/**
	 * Logs out the user
	 */
	public void logout() {
		mId = null;
		mIsLoggedIn = false;
		mClientId = null;
		setChanged();
	}

	/**
	 * @return true if the user is logged in
	 */
	public boolean isLoggedIn() {
		return mIsLoggedIn;
	}

	/**
	 * @return user id/key in the datastore
	 */
	public Key getKey() {
		return mId;
	}

	/**
	 * @return client the user is logged in from
	 */
	public UUID getClientId() {
		return mClientId;
	}

	/** User identity key */
	private Key mId = null;
	/** If the user is logged in */
	private boolean mIsLoggedIn = false;
	/** Client id */
	private UUID mClientId = null;

	/** Serializable id */
	private static final long serialVersionUID = -7213740820922710320L;
}
