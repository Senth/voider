package com.spiddekauga.voider.repo;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.spiddekauga.voider.utils.User;

/**
 * Preference gateway for user repository
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class UserPrefsGateway {
	/**
	 * Initializes the user preferences gateway
	 */
	UserPrefsGateway() {
		mPreferences = Gdx.app.getPreferences(PREFERENCES_NAME);
	}

	/**
	 * Set last logged in (online) user
	 * @param username username or email of the user that was logged in
	 * @param privateKey the private key which will be used for automatic login
	 * in the future.
	 * @param serverKey user id on the server
	 */
	void setLastUser(String username, UUID privateKey, String serverKey) {
		mPreferences.putString(LAST_USER__USERNAME, username);
		mPreferences.putBoolean(LAST_USER__ONLINE, true);
		mPreferences.putString(LAST_USER__PRIVATE_KEY, privateKey.toString());
		mPreferences.putString(LAST_USER__SERVER_KEY, serverKey);
		mPreferences.remove(LAST_USER__PASSWORD);
		mPreferences.flush();
	}

	/**
	 * Set last logged in (offline) user
	 * @param username username or email of the user that was logged in
	 * @param password
	 */
	void setLastUser(String username, String password) {
		mPreferences.putString(LAST_USER__USERNAME, username);
		mPreferences.putBoolean(LAST_USER__ONLINE, false);
		mPreferences.putString(LAST_USER__PASSWORD, password);
		mPreferences.remove(LAST_USER__PRIVATE_KEY);
		mPreferences.remove(LAST_USER__SERVER_KEY);
		mPreferences.flush();
	}

	/**
	 * Removes the last logged in user
	 */
	void removeLastUser() {
		mPreferences.remove(LAST_USER__USERNAME);
		mPreferences.remove(LAST_USER__PRIVATE_KEY);
		mPreferences.remove(LAST_USER__SERVER_KEY);
		mPreferences.remove(LAST_USER__PASSWORD);
		mPreferences.flush();
	}

	/**
	 * Get information of the last user that was logged in
	 * @return last user logged in, null if not found
	 */
	User getLastUser() {
		User userInfo = new User();

		userInfo.setUsername(mPreferences.getString(LAST_USER__USERNAME));
		userInfo.setOnline(mPreferences.getBoolean(LAST_USER__ONLINE));


		// Return if missing variables
		if (userInfo.getUsername() == null || userInfo.getUsername().equals("")) {
			return null;
		}

		if (userInfo.isOnline()) {
			// Login key
			String privateKeyString = mPreferences.getString(LAST_USER__PRIVATE_KEY);

			if (privateKeyString != null && !privateKeyString.equals("")) {
				userInfo.setPrivateKey(UUID.fromString(privateKeyString));
			}

			// Server key
			String serverKey = mPreferences.getString(LAST_USER__SERVER_KEY);
			if (serverKey != null) {
				userInfo.setServerKey(serverKey);
			}
		} else {
			userInfo.setPassword(mPreferences.getString(LAST_USER__PASSWORD));
		}

		if (userInfo.getPassword() == null && userInfo.getPrivateKey() == null) {
			return null;
		}

		return userInfo;
	}

	/**
	 * Save that this app has registered one user, thus no more users can be registered
	 */
	void setAsRegistered() {
		mPreferences.putBoolean(REGISTER__HAS_REGISTERED, true);
	}

	/**
	 * @return true if no user has been registered on this app
	 */
	boolean isRegisterAvailable() {
		return !mPreferences.getBoolean(REGISTER__HAS_REGISTERED, false);
	}

	/** Preferences */
	private Preferences mPreferences;
	/** Preferences name */
	private static final String PREFERENCES_NAME = "Voider_users";

	// Names
	// LAST_USER
	/** Username of last user */
	private static final String LAST_USER__USERNAME = "lastUser_username";
	/** Private key of last user */
	private static final String LAST_USER__PRIVATE_KEY = "lastUser_privateKey";
	/** If saved last user was online or offline */
	private static final String LAST_USER__ONLINE = "lastUser_online";
	/** Password */
	private static final String LAST_USER__PASSWORD = "lastUser_password";
	/** User id on the server */
	private static final String LAST_USER__SERVER_KEY = "lastUser_serverKey";

	// REGISTER
	/** True if the app has registered one user */
	private static final String REGISTER__HAS_REGISTERED = "register_hasRegistered";
}