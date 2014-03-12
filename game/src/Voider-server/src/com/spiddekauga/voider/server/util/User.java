package com.spiddekauga.voider.server.util;

import java.io.Serializable;

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
	 */
	public void login(Key userId) {
		mId = userId;
		mIsLoggedIn = true;
		setChanged();
	}

	/**
	 * Logs out the user
	 */
	public void logout() {
		mId = null;
		mIsLoggedIn = false;
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

	/** User identity key */
	private Key mId = null;
	/** If the user is logged in */
	private boolean mIsLoggedIn = false;

	/** Serializable id */
	private static final long serialVersionUID = -7213740820922710320L;
}
