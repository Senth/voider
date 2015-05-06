package com.spiddekauga.voider.servlets.api;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.utils.BCrypt;
import com.spiddekauga.voider.ClientVersions;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.network.misc.Motd.MotdTypes;
import com.spiddekauga.voider.network.misc.NetworkConfig;
import com.spiddekauga.voider.network.user.LoginMethod;
import com.spiddekauga.voider.network.user.LoginResponse;
import com.spiddekauga.voider.network.user.LoginResponse.ClientVersionStatuses;
import com.spiddekauga.voider.network.user.LoginResponse.Statuses;
import com.spiddekauga.voider.server.util.ServerConfig.Builds;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CMotd;
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
		mResponse = new LoginResponse();
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
				getMessageOfTheDay();
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

			// Add download URL
			Builds build = Builds.getCurrent();
			if (build != null) {
				String downloadUrl = build.getDownloadDesktopUrl();
				if (downloadUrl != null) {
					mResponse.changeLogMessage += NetworkConfig.SPLIT_TOKEN + downloadUrl;
				}
			}
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
		FilterWrapper property = new FilterWrapper(CUsers.USERNAME_LOWCASE, method.username.toLowerCase(Locale.ENGLISH));
		Entity datastoreEntity = DatastoreUtils.getSingleEntity(DatastoreTables.USERS, property);
		// Check username vs email
		if (datastoreEntity == null) {
			property = new FilterWrapper(CUsers.EMAIL, method.username.toLowerCase(Locale.ENGLISH));
			datastoreEntity = DatastoreUtils.getSingleEntity(DatastoreTables.USERS, property);
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
				mResponse.privateKey = DatastoreUtils.getPropertyUuid(datastoreEntity, CUsers.PRIVATE_KEY);
				mResponse.username = (String) datastoreEntity.getProperty(CUsers.USERNAME);
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
	 * Get new messages (MOTD) if we successfully logged in
	 */
	private void getMessageOfTheDay() {
		// Could not login, skip message of the day
		if (!mResponse.isSuccessful()) {
			return;
		}


		// Get all active MOTD
		FilterWrapper notExpired = new FilterWrapper(CMotd.EXPIRES, FilterOperator.GREATER_THAN_OR_EQUAL, new Date());
		Iterable<Entity> entities = DatastoreUtils.getEntities(DatastoreTables.MOTD, notExpired);

		// Convert to MOTD types
		for (Entity entity : entities) {
			Motd motd = new Motd();
			motd.created = (Date) entity.getProperty(CMotd.CREATED);
			motd.title = (String) entity.getProperty(CMotd.TITLE);
			motd.content = (String) entity.getProperty(CMotd.CONTENT);
			motd.type = DatastoreUtils.getPropertyIdStore(entity, CMotd.TYPE, MotdTypes.class);

			mResponse.motds.add(motd);
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

		UUID datastorePrivateKey = DatastoreUtils.getPropertyUuid(datastoreEntity, CUsers.PRIVATE_KEY);
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

	private LoginResponse mResponse = null;
}
