package com.spiddekauga.voider.repo;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.voider.utils.User;

/**
 * Local repository for users
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class UserLocalRepo {
	/**
	 * Disposes all resources
	 */
	public static void dispose() {
		mSqliteGateway.dispose();
	}

	/**
	 * Set last logged in (online) user
	 * @param username username or email of the user that was logged in
	 * @param privateKey the private key which will be used for automatic login
	 * @param serverKey user id on the server
	 * in the future.
	 */
	public static void setLastUser(String username, UUID privateKey, String serverKey) {
		mPrefsGateway.setLastUser(username, privateKey, serverKey);
	}

	/**
	 * Set last logged in (offline) user
	 * @param username username or email of the user that was logged in
	 * @param password
	 */
	public static void setLastUser(String username, String password) {
		mPrefsGateway.setLastUser(username, password);
	}

	/**
	 * Removes the last logged in user
	 */
	public static void removeLastUser() {
		mPrefsGateway.removeLastUser();
	}

	/**
	 * Get information of the last user that was logged in
	 * @return last user logged in, null if not found
	 */
	public static User getLastUser() {
		return mPrefsGateway.getLastUser();
	}

	/**
	 * @return client id, creates a new client id if one doesn't exist
	 */
	public static UUID getClientId() {
		UUID clientId = mPrefsGateway.getClientId();
		if (clientId == null) {
			clientId = UUID.randomUUID();
			mPrefsGateway.setClientId(clientId);
		}

		return clientId;
	}

	/**
	 * Remove a temporary user (usually done when syncing has been done or the player decided to remove it)
	 * @param username name of the user to remove
	 */
	public static void removeTempUser(String username) {
		mSqliteGateway.removeTempUser(username);
	}

	/**
	 * Creates a new temporary offline user until Voider gets goes online
	 * @param username new username
	 * @param password the password for the user
	 * @param email the email of the user
	 * @return true if the user was created successfully, false if email or username already exists
	 */
	public static boolean createTempUser(String username, String password, String email) {
		return mSqliteGateway.createTempUser(username, password, email);
	}

	/**
	 * @return all temporary created users
	 */
	public static ArrayList<User> getTempUsers() {
		return mSqliteGateway.getTempUsers();
	}

	/**
	 * Search for a temporary user with the specified username/email
	 * @param username either username or email
	 * @return user information for the found user, null if not found
	 */
	public static User getTempUser(String username) {
		return mSqliteGateway.getTempUser(username);
	}

	/**
	 * Save that this app has registered one user, thus no more users can be registered
	 */
	public static void setAsRegistered() {
		mPrefsGateway.setAsRegistered();
	}

	/**
	 * @return true if no user has been registered on this app
	 */
	public static boolean isRegisterAvailable() {
		return mPrefsGateway.isRegisterAvailable();
	}

	/** Preferences gateway */
	private static UserPrefsGateway mPrefsGateway = new UserPrefsGateway();
	/** SQLite gateway */
	private static UserSqliteGateway mSqliteGateway = new UserSqliteGateway();

}
