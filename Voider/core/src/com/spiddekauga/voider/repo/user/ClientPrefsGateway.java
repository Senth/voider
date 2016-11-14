package com.spiddekauga.voider.repo.user;

import com.spiddekauga.voider.repo.PrefsGateway;

import java.util.Date;
import java.util.UUID;

/**
 * Preference gateway for user repository
 */
class ClientPrefsGateway extends PrefsGateway {
/** Username of last user */
private static final String LAST_USER__USERNAME = "lastUser_username";
/** Email of last user */
private static final String LAST_USER__EMAIL = "lastUser_email";
/** Private key of last user */
private static final String LAST_USER__PRIVATE_KEY = "lastUser_privateKey";
/** User id on the server */
private static final String LAST_USER__SERVER_KEY = "lastUser_serverKey";
/** Client id */
private static final String CLIENT__ID = "client_id";
/** Last login date of a user */
private static final String LAST_LOGIN__ = "lastLogin_";

/**
 * Initializes the user preferences gateway
 */
ClientPrefsGateway() {
	super(false);
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
 * Sets the client id
 * @param clientId new client id
 */
void setClientId(UUID clientId) {
	mPreferences.putString(CLIENT__ID, clientId.toString());
}

/**
 * Set last logged in (online) user
 * @param username username of the user that was logged in
 * @param email email of the user that was logged in
 * @param privateKey the private key which will be used for automatic login in the future.
 * @param serverKey user id on the server
 */
void setLastUser(String username, String email, UUID privateKey, String serverKey) {
	mPreferences.putString(LAST_USER__USERNAME, username);
	mPreferences.putString(LAST_USER__EMAIL, email);
	mPreferences.putString(LAST_USER__PRIVATE_KEY, privateKey.toString());
	mPreferences.putString(LAST_USER__SERVER_KEY, serverKey);
	mPreferences.flush();
}

/**
 * Removes the last logged in user
 */
void removeLastUser() {
	mPreferences.remove(LAST_USER__USERNAME);
	mPreferences.remove(LAST_USER__EMAIL);
	mPreferences.remove(LAST_USER__PRIVATE_KEY);
	mPreferences.remove(LAST_USER__SERVER_KEY);
	mPreferences.flush();
}

// Names
// LAST_USER

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
	userInfo.setEmail(mPreferences.getString(LAST_USER__EMAIL));
	userInfo.setOnline(true);


	// Return if missing variables
	if (userInfo.getUsername() == null || userInfo.getUsername().isEmpty()) {
		return null;
	}

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

	return userInfo;
}

/**
 * Update the last login online to the current date
 * @param username username of the user
 * @param email user's email
 */
void updateLastLogin(String username, String email) {
	Date currentDate = new Date();
	mPreferences.putLong(LAST_LOGIN__ + username.toLowerCase(), currentDate.getTime());
	mPreferences.putLong(LAST_LOGIN__ + email.toLowerCase(), currentDate.getTime());
	mPreferences.flush();
}

/**
 * Clear the last login online for the specified user
 * @param username name of the user
 * @param email user's email
 */
void clearLastLogin(String username, String email) {
	mPreferences.remove(LAST_LOGIN__ + username.toLowerCase());
	mPreferences.remove(LAST_LOGIN__ + email.toLowerCase());
	mPreferences.flush();
}

/**
 * Get the last time the user logged in online through this client
 * @param usernameOrEmail can be either username or email
 * @return date when the user logged in the last time, null if the user haven't logged in previously
 * on this client
 */
Date getLastLogin(String usernameOrEmail) {
	long time = mPreferences.getLong(LAST_LOGIN__ + usernameOrEmail.toLowerCase(), -1);

	if (time != -1) {
		return new Date(time);
	} else {
		return null;
	}
}

// LAST_LOGIN

@Override
protected PreferenceNames getPreferenceName() {
	return PreferenceNames.USERS;
}

}