package com.spiddekauga.voider.repo.user;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.spiddekauga.voider.Config;

/**
 * Preference gateway for user repository
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class ClientPrefsGateway {
	/**
	 * Initializes the user preferences gateway
	 */
	ClientPrefsGateway() {
		mPreferences = Gdx.app.getPreferences(PREFERENCES_NAME);
	}

	/**
	 * Sets the client id
	 * @param clientId new client id
	 */
	void setClientId(UUID clientId) {
		mPreferences.putString(CLIENT__ID, clientId.toString());
	}

	/**
	 * @return clientId, null if none exists
	 */
	UUID getClientId() {
		String stringUuid = mPreferences.getString(CLIENT__ID, null);
		if (stringUuid != null) {
			return UUID.fromString(stringUuid);
		} else {
			return null;
		}
	}

	/**
	 * Set last logged in (online) user
	 * @param username username or email of the user that was logged in
	 * @param privateKey the private key which will be used for automatic login in the
	 *        future.
	 * @param serverKey user id on the server
	 */
	void setLastUser(String username, UUID privateKey, String serverKey) {
		mPreferences.putString(LAST_USER__USERNAME, username);
		mPreferences.putString(LAST_USER__PRIVATE_KEY, privateKey.toString());
		mPreferences.putString(LAST_USER__SERVER_KEY, serverKey);
		mPreferences.flush();
	}

	/**
	 * Removes the last logged in user
	 */
	void removeLastUser() {
		mPreferences.remove(LAST_USER__USERNAME);
		mPreferences.remove(LAST_USER__PRIVATE_KEY);
		mPreferences.remove(LAST_USER__SERVER_KEY);
		mPreferences.flush();
	}

	/**
	 * Updates the private key of the last logged in user
	 * @param privateKey new private key for the user
	 */
	void setPrivateKey(UUID privateKey) {
		mPreferences.putString(LAST_USER__PRIVATE_KEY, privateKey.toString());
		mPreferences.flush();
	}

	/**
	 * Get information of the last user that was logged in
	 * @return last user logged in, null if not found
	 */
	User getLastUser() {
		User userInfo = new User();

		userInfo.setUsername(mPreferences.getString(LAST_USER__USERNAME));


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
		}

		if (userInfo.getPassword() == null && userInfo.getPrivateKey() == null) {
			return null;
		}

		return userInfo;
	}

	/** Preferences */
	private Preferences mPreferences;
	/** Preferences name */
	private static final String PREFERENCES_NAME = Config.File.PREFERENCE_PREFIX + "_users";

	// Names
	// LAST_USER
	/** Username of last user */
	private static final String LAST_USER__USERNAME = "lastUser_username";
	/** Private key of last user */
	private static final String LAST_USER__PRIVATE_KEY = "lastUser_privateKey";
	/** User id on the server */
	private static final String LAST_USER__SERVER_KEY = "lastUser_serverKey";
	/** Client id */
	private static final String CLIENT__ID = "client_id";

}