package com.spiddekauga.voider.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.spiddekauga.utils.Observable;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.GameEvent.Events;

/**
 * User information class
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class User extends Observable {
	/**
	 * @return global instance of this user, logged in or logged out.
	 * @note Users can still be created, this is not a singleton class
	 */
	public static User getGlobalUser() {
		return mGlobalUser;
	}

	/**
	 * @return current username
	 */
	public String getUsername() {
		return mUsername;
	}

	/**
	 * Sets the username
	 * @param username set the name of the user
	 * @see #login(String, String, boolean)
	 */
	public void setUsername(String username) {
		mUsername = username;
	}

	/**
	 * Logs out the user
	 */
	public void logout() {
		mEmail = null;
		mOnline = false;
		mPassword = null;
		mPrivateKey = null;
		mServerKey = null;
		mLoggedIn = false;
		mUsername = "(None)";
		mDateFormat = DATE_FORMAT_DEFAULT;

		// Update user path
		if (this == mGlobalUser) {
			Config.File.setUserPaths(mUsername);
		}

		setChanged();
		notifyObservers(new GameEvent(Events.USER_LOGOUT));
	}

	/**
	 * Set a user from another user
	 * @param from the other user to set from
	 */
	public void set(User from) {
		mEmail = from.mEmail;
		mOnline = from.mOnline;
		mPassword = from.mPassword;
		mPrivateKey = from.mPrivateKey;
		mServerKey = from.mServerKey;
		mUsername = from.mUsername;
		mDateFormat = from.mDateFormat;
	}

	/**
	 * Sets all necessary variables when logging in
	 * @param username the username
	 * @param serverKey user key on the server, can be null
	 * @param online true if the user is online
	 */
	public void login(String username, String serverKey, boolean online) {
		mUsername = username;
		mServerKey = serverKey;
		mOnline = online;
		mLoggedIn = true;

		// Update user path
		if (this == mGlobalUser) {
			Config.File.setUserPaths(mUsername);
		}

		setChanged();
		notifyObservers(new GameEvent(Events.USER_LOGIN));
	}

	/**
	 * Make the user offline
	 */
	public void makeOffline() {
		mOnline = false;
	}

	/**
	 * @return true if the user is logged in
	 */
	public boolean isLoggedIn() {
		return mLoggedIn;
	}

	/**
	 * Sets the date format for the user
	 * @param dateFormat preferred date format for the user
	 */
	public void setDateFormat(String dateFormat) {
		mDateFormat = new SimpleDateFormat(dateFormat);
	}

	/**
	 * Sets the user id on the server
	 * @param serverKey user id of the server
	 */
	public void setServerKey(String serverKey) {
		mServerKey = serverKey;
	}

	/**
	 * @return user key on the server, null if unknown
	 */
	public String getServerKey() {
		return mServerKey;
	}

	/**
	 * Sets the user to online/offline
	 * @param online true if online mode, false if offline mode
	 * @see #login(String, String, boolean)
	 */
	public void setOnline(boolean online) {
		mOnline = online;
	}

	/**
	 * @return true if user is online, false if in offline mode
	 */
	public boolean isOnline() {
		return mOnline;
	}

	/**
	 * @return the Password
	 */
	public String getPassword() {
		return mPassword;
	}

	/**
	 * @param password the Password to set
	 */
	public void setPassword(String password) {
		mPassword = password;
	}

	/**
	 * @return the Email
	 */
	public String getEmail() {
		return mEmail;
	}

	/**
	 * @param email the Email to set
	 */
	public void setEmail(String email) {
		mEmail = email;
	}

	/**
	 * @param privateKey private login key to set
	 */
	public void setPrivateKey(UUID privateKey) {
		mPrivateKey = privateKey;
	}

	/**
	 * @return private login key
	 */
	public UUID getPrivateKey() {
		return mPrivateKey;
	}

	/**
	 * Converts a date to the user's preferred date format
	 * @param date the date to convert to string
	 * @return date as string in the user's preferred date format
	 */
	public synchronized String dateToString(Date date) {
		return mDateFormat.format(date);
	}

	/** Default datetime format */
	private final static SimpleDateFormat DATE_FORMAT_DEFAULT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	/** Date to string format */
	private SimpleDateFormat mDateFormat = DATE_FORMAT_DEFAULT;
	/** Global user */
	private static User mGlobalUser = new User();
	/** Current username */
	private String mUsername = "(None)";
	/** Current user key */
	private String mServerKey = null;
	/** If the user is online */
	private boolean mOnline = false;
	/** Password, usually not used or stored */
	private String mPassword = null;
	/** Email, usually not used or stored */
	private String mEmail = null;
	/** Private login key */
	private UUID mPrivateKey = null;
	/** User is logged in */
	private boolean mLoggedIn = false;
}
