package com.spiddekauga.voider.repo;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.spiddekauga.voider.utils.UserInfo;

/**
 * Local repository for user
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class UserPrefsGateway {
	/**
	 * Initializes the user preferences gateway
	 */
	public UserPrefsGateway() {
		mPreferences = Gdx.app.getPreferences(PREFERENCES_NAME);
	}

	/**
	 * Set last logged in user
	 * @param username username or email of the user that was logged in
	 * @param privateKey the private key which will be used for automatic login
	 * in the future.
	 */
	void setLastUser(String username, UUID privateKey) {
		mPreferences.putString(LAST_USER__USERNAME, username);
		mPreferences.putString(LAST_USER__PRIVATE_KEY, privateKey.toString());
		mPreferences.flush();
	}

	/**
	 * Removes the last logged in user
	 */
	void removeLastUser() {
		mPreferences.remove(LAST_USER__USERNAME);
		mPreferences.remove(LAST_USER__PRIVATE_KEY);
		mPreferences.flush();
	}

	/**
	 * Get information of the last user that was logged in
	 * @return last user logged in, null if not found
	 */
	UserInfo getLastUser() {
		UserInfo userInfo = new UserInfo();

		userInfo.username = mPreferences.getString(LAST_USER__USERNAME);
		String privateKeyString = mPreferences.getString(LAST_USER__PRIVATE_KEY);

		if (userInfo.username == null || userInfo.username.equals("") ||
				privateKeyString == null || privateKeyString.equals("")) {
			return null;
		}

		userInfo.privateKey = UUID.fromString(privateKeyString);

		return userInfo;
	}

	/** Preferences */
	private Preferences mPreferences;
	/** Preferences name */
	private static final String PREFERENCES_NAME = "Voider_users";

	// Names
	/** Username of last user */
	private static final String LAST_USER__USERNAME = "lastUser_username";
	/** Private key of last user */
	private static final String LAST_USER__PRIVATE_KEY = "lastUser_privateKey";
}