package com.spiddekauga.voider.servlets.api;

import java.io.File;
import java.io.FileInputStream;
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
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.network.misc.Motd.MotdTypes;
import com.spiddekauga.voider.network.user.LoginMethod;
import com.spiddekauga.voider.network.user.LoginResponse;
import com.spiddekauga.voider.network.user.LoginResponse.RestoreDate;
import com.spiddekauga.voider.network.user.LoginResponse.Statuses;
import com.spiddekauga.voider.network.user.LoginResponse.VersionInformation;
import com.spiddekauga.voider.server.util.ServerConfig;
import com.spiddekauga.voider.server.util.ServerConfig.Builds;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CMotd;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CRestoreDate;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUsers;
import com.spiddekauga.voider.server.util.VoiderApiServlet;
import com.spiddekauga.voider.version.VersionContainer;
import com.spiddekauga.voider.version.VersionParser;

/**
 * Tries to login to the server

 */
@SuppressWarnings("serial")
public class Login extends VoiderApiServlet<LoginMethod> {

	@Override
	protected void onInit() {
		mResponse = new LoginResponse();
		mResponse.status = Statuses.FAILED_SERVER_ERROR;
	}

	@Override
	protected IEntity onRequest(LoginMethod method) throws ServletException, IOException {
		switch (getMaintenanceMode()) {
		case UP:
			// Skip if already logged in
			if (!mUser.isLoggedIn()) {
				checkClientVersion(method);
				checkRestoreDate(method);
				login(method);

				// MOTD
				if (mResponse.isSuccessful()) {
					getMessageOfTheDay();
				}
			}
			break;

		case DOWN:
			mResponse.status = Statuses.FAILED_SERVER_MAINTENANCE;
			checkClientVersion(method);
			getMessageOfTheDay();
			break;
		}

		return mResponse;
	}

	/**
	 * Check client version and sets the client version status in the response depending
	 * on the client version.
	 * @param method
	 */
	private void checkClientVersion(LoginMethod method) {
		// New versions available
		if (!mVersionContainer.isLatest(method.currentVersion)) {
			// Check client needs to be updated
			if (mVersionContainer.isUpdateRequired(method.currentVersion)) {
				mResponse.versionInfo.status = VersionInformation.Statuses.UPDATE_REQUIRED;
			} else {
				mResponse.versionInfo.status = VersionInformation.Statuses.NEW_VERSION_AVAILABLE;
			}

			mResponse.versionInfo.newVersions = mVersionContainer.getVersionsAfter(method.currentVersion);

			// Add download URL
			Builds build = Builds.getCurrent();
			if (build != null) {
				mResponse.versionInfo.downloadLocationDesktop = build.getDownloadDesktopUrl();
			}
		} else {
			mResponse.versionInfo.status = VersionInformation.Statuses.UP_TO_DATE;
		}
	}

	/**
	 * Check if the client needs to revert and restore the local data
	 * @param method
	 */
	private void checkRestoreDate(LoginMethod method) {
		if (method.lastLogin != null) {

			// Was last login between any restore date?
			FilterWrapper beforeFromDate = new FilterWrapper(CRestoreDate.FROM_DATE, FilterOperator.GREATER_THAN_OR_EQUAL, method.lastLogin);
			Iterable<Entity> entities = DatastoreUtils.getEntities(DatastoreTables.RESTORE_DATE, beforeFromDate);

			// Check if there is one that's between
			for (Entity entity : entities) {

				Date toDate = (Date) entity.getProperty(CRestoreDate.TO_DATE);

				if (toDate.before(method.lastLogin)) {
					mResponse.restoreDate = new RestoreDate();
					mResponse.restoreDate.from = (Date) entity.getProperty(CRestoreDate.FROM_DATE);
					mResponse.restoreDate.to = toDate;
				}
			}
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
				mResponse.email = (String) datastoreEntity.getProperty(CUsers.EMAIL);
				updateLastLoggedIn(datastoreEntity);

				// Only login online if we have a valid version
				if (mResponse.isServerLoginAvailable()) {
					mUser.login(datastoreEntity.getKey(), mResponse.username, method.clientId);
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

	@Override
	protected boolean isHandlingRequestDuringMaintenance() {
		return true;
	}

	/**
	 * Loads the current version container
	 */
	private static void loadVersionContainer() {
		File file = new File(ServerConfig.VERSION_FILE);
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			VersionParser versionParser = new VersionParser(fileInputStream);
			mVersionContainer = versionParser.parse();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private LoginResponse mResponse = null;
	private static VersionContainer mVersionContainer = null;

	static {
		loadVersionContainer();
	}
}
