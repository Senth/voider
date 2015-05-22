package com.spiddekauga.voider.repo.user;

import java.util.UUID;

/**
 * Local repository for users
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class UserLocalRepo {
	/**
	 * Private constructor to enforce singleton pattern
	 */
	private UserLocalRepo() {
		// Does nothing
	}

	/**
	 * @return instance of this class
	 */
	public static UserLocalRepo getInstance() {
		if (mInstance == null) {
			mInstance = new UserLocalRepo();
		}
		return mInstance;
	}

	/**
	 * Set last logged in (online) user
	 * @param username username or email of the user that was logged in
	 * @param privateKey the private key which will be used for automatic login
	 * @param serverKey user id on the server in the future.
	 */
	void setLastUser(String username, UUID privateKey, String serverKey) {
		mClientPrefsGateway.setLastUser(username, privateKey, serverKey);
	}

	/**
	 * Removes the last logged in user
	 */
	void removeLastUser() {
		mClientPrefsGateway.removeLastUser();
	}

	/**
	 * Get information of the last user that was logged in
	 * @return last user logged in, null if not found
	 */
	User getLastUser() {
		return mClientPrefsGateway.getLastUser();
	}


	/**
	 * @return client id, creates a new client id if one doesn't exist
	 */
	UUID getClientId() {
		UUID clientId = mClientPrefsGateway.getClientId();
		if (clientId == null) {
			clientId = UUID.randomUUID();
			mClientPrefsGateway.setClientId(clientId);
		}

		return clientId;
	}

	/**
	 * @return user analytics id for this client
	 */
	UUID getAnalyticsId() {
		return mUserPrefsGateway.getAnalyticsId();
	}

	/**
	 * Change the private key of the current user
	 * @param privateKey new private key
	 */
	void setPrivateKey(UUID privateKey) {
		mClientPrefsGateway.setPrivateKey(privateKey);
	}

	private UserPrefsGateway mUserPrefsGateway = new UserPrefsGateway();
	private ClientPrefsGateway mClientPrefsGateway = new ClientPrefsGateway();
	private static UserLocalRepo mInstance = null;
}
