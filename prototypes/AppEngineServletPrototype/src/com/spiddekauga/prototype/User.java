package com.spiddekauga.prototype;

import java.io.Serializable;

import com.spiddekauga.web.SessionVariable;

/**
 * Logged in user
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class User extends SessionVariable implements Serializable {
	/**
	 * Login the user and set all parameters
	 * @param username
	 */
	public void login(String username) {
		mUsername = username;
		mIsLoggedIn = true;
		setChanged();
	}

	/**
	 * Logs out the user
	 */
	public void logout() {
		mUsername = null;
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
	 * @return user name
	 */
	public String getUsername() {
		return mUsername;
	}

	/** The user name */
	private String mUsername = null;
	/** If the user is logged in */
	private boolean mIsLoggedIn = false;

	/** Serializable id */
	private static final long serialVersionUID = -7213740820922710320L;
}
