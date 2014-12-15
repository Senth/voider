package com.spiddekauga.voider.repo.user;

import java.util.UUID;

import com.spiddekauga.voider.utils.User;

/**
 * Local repository for users
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class UserLocalRepo {
	/**
	 * Disposes all resources
	 */
	public static void dispose() {
	}

	/**
	 * Set last logged in (online) user
	 * @param username username or email of the user that was logged in
	 * @param privateKey the private key which will be used for automatic login
	 * @param serverKey user id on the server in the future.
	 */
	public static void setLastUser(String username, UUID privateKey, String serverKey) {
		mClientPrefsGateway.setLastUser(username, privateKey, serverKey);
	}

	/**
	 * Set last logged in (offline) user
	 * @param username username or email of the user that was logged in
	 * @param password
	 */
	public static void setLastUser(String username, String password) {
		mClientPrefsGateway.setLastUser(username, password);
	}

	/**
	 * Removes the last logged in user
	 */
	public static void removeLastUser() {
		mClientPrefsGateway.removeLastUser();
	}

	/**
	 * Get information of the last user that was logged in
	 * @return last user logged in, null if not found
	 */
	public static User getLastUser() {
		return mClientPrefsGateway.getLastUser();
	}

	/**
	 * @return client id, creates a new client id if one doesn't exist
	 */
	public static UUID getClientId() {
		UUID clientId = mClientPrefsGateway.getClientId();
		if (clientId == null) {
			clientId = UUID.randomUUID();
			mClientPrefsGateway.setClientId(clientId);
		}

		return clientId;
	}

	/**
	 * Save that this app has registered one user, thus no more users can be registered
	 */
	public static void setAsRegistered() {
		mClientPrefsGateway.setAsRegistered();
	}

	/**
	 * @return true if no user has been registered on this app
	 */
	public static boolean isRegisterAvailable() {
		return mClientPrefsGateway.isRegisterAvailable();
	}


	private static ClientPrefsGateway mClientPrefsGateway = new ClientPrefsGateway();

}
