package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.utils.BCrypt;
import com.spiddekauga.voider.ClientVersions;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.user.LoginMethod;
import com.spiddekauga.voider.network.user.LoginMethodResponse;
import com.spiddekauga.voider.network.user.LoginMethodResponse.ClientVersionStatuses;
import com.spiddekauga.voider.network.user.LoginMethodResponse.Statuses;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUsers;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Tries to login to the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Login extends VoiderServlet {

	@Override
	protected void onInit() {
		mResponse = new LoginMethodResponse();
		mResponse.status = Statuses.FAILED_SERVER_ERROR;
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		// Skip if already logged in
		if (!mUser.isLoggedIn()) {
			if (methodEntity instanceof LoginMethod) {
				LoginMethod loginMethod = (LoginMethod) methodEntity;
				checkClientVersion(loginMethod);
				login(loginMethod);
			}
		}

		return mResponse;
	}

	/**
	 * Check client version and sets the client version status in the response depending
	 * on the client version.
	 * @param method
	 */
	private void checkClientVersion(LoginMethod method) {
		boolean updateAvailable = !ClientVersions.isLatestVersion(method.clientVersion);

		// Check client needs to be updated
		if (updateAvailable) {
			if (ClientVersions.isUpdateNeeded(method.clientVersion)) {
				mResponse.clientVersionStatus = ClientVersionStatuses.UPDATE_REQUIRED;
			} else {
				mResponse.clientVersionStatus = ClientVersionStatuses.NEW_VERSION_AVAILABLE;
			}
			mResponse.changeLogMessage = ClientVersions.getChangeLogs(method.clientVersion);
		} else {
			mResponse.clientVersionStatus = ClientVersionStatuses.UP_TO_DATE;
		}
	}

	/**
	 * Try to login
	 * @param method
	 */
	private void login(LoginMethod method) {
		// Check username vs username first
		FilterWrapper property = new FilterWrapper(CUsers.USERNAME, method.username);
		Entity datastoreEntity = DatastoreUtils.getSingleEntity(DatastoreTables.USERS.toString(), property);
		// Check username vs email
		if (datastoreEntity == null) {
			property = new FilterWrapper(CUsers.EMAIL, method.username);
			datastoreEntity = DatastoreUtils.getSingleEntity(DatastoreTables.USERS.toString(), property);
		}

		if (datastoreEntity != null) {
			// Test password / private key
			if (isPrivateKeyMatch(datastoreEntity, method.privateKey)) {
				mResponse.status = Statuses.SUCCESS;
			} else if (isPasswordMatch(datastoreEntity, method.password)) {
				mResponse.status = Statuses.SUCCESS;
			}

			// Login and update last logged in date
			if (mResponse.status == Statuses.SUCCESS) {
				mResponse.userKey = KeyFactory.keyToString(datastoreEntity.getKey());
				mResponse.privateKey = DatastoreUtils.getUuidProperty(datastoreEntity, CUsers.PRIVATE_KEY);
				mResponse.username = (String) datastoreEntity.getProperty(CUsers.USERNAME);
				mResponse.dateFormat = (String) datastoreEntity.getProperty(CUsers.DATE_FORMAT);
				updateLastLoggedIn(datastoreEntity);

				// Only login online if we have a valid version
				switch (mResponse.clientVersionStatus) {
				case NEW_VERSION_AVAILABLE:
				case UP_TO_DATE:
					mUser.login(datastoreEntity.getKey(), mResponse.username, method.clientId);
					break;

				case UNKNOWN:
				case UPDATE_REQUIRED:
					// Do nothing
					break;
				}
			}
		}

		if (mResponse.status != Statuses.SUCCESS) {
			mResponse.status = Statuses.FAILED_USERNAME_PASSWORD_MISMATCH;
		}
	}

	/**
	 * Try to use private key authorization
	 * @param datastoreEntity the datastore entity to test with
	 * @param privateKey the private key to test
	 * @return true if the private keys match
	 */
	private boolean isPrivateKeyMatch(Entity datastoreEntity, UUID privateKey) {
		if (privateKey == null) {
			return false;
		}

		UUID datastorePrivateKey = DatastoreUtils.getUuidProperty(datastoreEntity, CUsers.PRIVATE_KEY);
		return privateKey.equals(datastorePrivateKey);
	}

	/**
	 * Try to use password authorization
	 * @param datastoreEntity the datastore entity to test with
	 * @param password plain password (not hashed)
	 * @return true if the passwords match
	 */
	private boolean isPasswordMatch(Entity datastoreEntity, String password) {
		if (password == null) {
			return false;
		}

		String hashedPassword = (String) datastoreEntity.getProperty(CUsers.PASSWORD);
		return BCrypt.checkpw(password, hashedPassword);
	}

	/**
	 * Updates last logged in of the user
	 * @param userEntity the user that logged in
	 */
	private void updateLastLoggedIn(Entity userEntity) {
		userEntity.setProperty(CUsers.LOGGED_IN, new Date());
		DatastoreUtils.put(userEntity);
	}

	private LoginMethodResponse mResponse = null;
}
