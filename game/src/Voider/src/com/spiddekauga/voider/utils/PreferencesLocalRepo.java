package com.spiddekauga.voider.utils;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Local repository for preferences
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PreferencesLocalRepo {
	/**
	 * Initializes the preferences
	 */
	public static void init() {
		mPreferences = Gdx.app.getPreferences("Voider");
	}

	/**
	 * Set last logged in user
	 * @param username username or email of the user that was logged in
	 * @param privateKey the private key which will be used for automatic login
	 * in the future.
	 */
	public static void setLastUser(String username, UUID privateKey) {
		mPreferences.putString(LAST_USER__USERNAME, username);
		mPreferences.putString(LAST_USER__PRIVATE_KEY, privateKey.toString());
		mPreferences.flush();
	}

	/**
	 * Removes the last logged in user
	 */
	public static void removeLastUser() {
		mPreferences.remove(LAST_USER__USERNAME);
		mPreferences.remove(LAST_USER__PRIVATE_KEY);
		mPreferences.flush();
	}

	/**
	 * Get information of the last user that was logged in
	 * @return last user logged in, null if not found
	 */
	public static UserInfo getLastUser() {
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
	private static Preferences mPreferences;


	// Names
	/** Username of last user */
	private static final String LAST_USER__USERNAME = "lastUser_username";
	/** Private key of last user */
	private static final String LAST_USER__PRIVATE_KEY = "lastUser_privateKey";
}