package com.spiddekauga.voider.servlets.api;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.utils.BCrypt;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.user.RegisterUserMethod;
import com.spiddekauga.voider.network.user.RegisterUserResponse;
import com.spiddekauga.voider.network.user.RegisterUserResponse.Statuses;
import com.spiddekauga.voider.server.util.ServerConfig;
import com.spiddekauga.voider.server.util.ServerConfig.Builds;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CBetaKey;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUsers;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.ServletException;

/**
 * Registers a user
 */
@SuppressWarnings("serial")
public class RegisterUser extends VoiderApiServlet<RegisterUserMethod> {

private Entity mBetaKeyEntity = null;
private RegisterUserResponse mResponse = null;
private RegisterUserMethod mParameters = null;

@Override
protected void onInit() {
	mResponse = new RegisterUserResponse();
	mResponse.status = Statuses.FAIL_SERVER_ERROR;
}

@Override
protected IEntity onRequest(RegisterUserMethod method) throws ServletException, IOException {
	mParameters = method;

	FilterWrapper usernameProperty = new FilterWrapper(CUsers.USERNAME_LOWCASE, mParameters.username.toLowerCase(Locale.ENGLISH));
	FilterWrapper emailProperty = new FilterWrapper(CUsers.EMAIL, mParameters.email.toLowerCase(Locale.ENGLISH));
	boolean validFields = true;

	// Check username length
	if (!isUsernameLengthValid(mParameters.username)) {
		mResponse.status = Statuses.FAIL_USERNAME_TOO_SHORT;
		validFields = false;
	}
	// Check password length
	else if (!isPasswordLengthValid(mParameters.password)) {
		mResponse.status = Statuses.FAIL_PASSWORD_TOO_SHORT;
		validFields = false;
	}
	// Check if username is free
	else if (DatastoreUtils.exists(DatastoreTables.USERS, usernameProperty)) {
		mResponse.status = Statuses.FAIL_USERNAME_EXISTS;
		validFields = false;
	}
	// Check email
	else if (DatastoreUtils.exists(DatastoreTables.USERS, emailProperty)) {
		mResponse.status = Statuses.FAIL_EMAIL_EXISTS;
		validFields = false;
	}
	// Check beta key
//	else if (!isRegisterKeyValid(mParameters.key)) {
//		validFields = false;
//	}

	// All valid
	if (validFields) {
		createNewUser();
	}

	return mResponse;
}

/**
 * Checks minimum username length
 * @param username the user's name
 * @return true if the password has the minimum required characters in the username
 */
private boolean isUsernameLengthValid(String username) {
	return username.length() >= ServerConfig.UserInfo.NAME_LENGTH_MIN;
}

/**
 * Checks minimum password length
 * @param password the user's password
 * @return true if the password has the minimum required characters in the password
 */
static boolean isPasswordLengthValid(String password) {
	return password.length() >= ServerConfig.UserInfo.PASSWORD_LENGTH_MIN;
}

/**
 * Adds the new user to the datastore
 */
private void createNewUser() {
	Entity datastoreEntity = new Entity(DatastoreTables.USERS);

	datastoreEntity.setProperty(CUsers.USERNAME, mParameters.username);
	datastoreEntity.setProperty(CUsers.USERNAME_LOWCASE, mParameters.username.toLowerCase(Locale.ENGLISH));
	datastoreEntity.setUnindexedProperty(CUsers.CREATED, new Date());
	datastoreEntity.setUnindexedProperty(CUsers.LOGGED_IN, new Date());
	datastoreEntity.setProperty(CUsers.EMAIL, mParameters.email);

	// Private key
	UUID privateKey = UUID.randomUUID();
	DatastoreUtils.setProperty(datastoreEntity, CUsers.PRIVATE_KEY, privateKey);

	// Hashed password
	String salt = BCrypt.gensalt();
	String hashedPassword = BCrypt.hashpw(mParameters.password, salt);
	datastoreEntity.setProperty(CUsers.PASSWORD, hashedPassword);

	Key userKey = DatastoreUtils.put(datastoreEntity);

	if (userKey != null) {
		mResponse.userKey = KeyFactory.keyToString(userKey);
		mResponse.privateKey = privateKey;
		mResponse.status = Statuses.SUCCESS;

		if (Builds.BETA.isCurrent()) {
			setBetaKeyAsUsed();
		}
	}
}

/**
 * Set the beta key as used
 */
private void setBetaKeyAsUsed() {
	mBetaKeyEntity.setProperty(CBetaKey.USED, true);
	DatastoreUtils.put(mBetaKeyEntity);
}

/**
 * Check if beta key is valid (and required)
 * @return true if this client doesn't need a register key or if the register key is valid.
 */
private boolean isRegisterKeyValid(String registerKey) {
	if (Builds.BETA.isCurrent()) {
		if (registerKey != null) {
			Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.BETA_KEY, new FilterWrapper(CBetaKey.KEY, registerKey));

			// Found key
			if (entity != null) {
				// Not used
				Boolean used = (Boolean) entity.getProperty(CBetaKey.USED);
				if (used != null && !used) {
					mBetaKeyEntity = entity;
					return true;
				} else {
					mResponse.status = Statuses.FAIL_REGISTER_KEY_USED;
				}
			} else {
				mResponse.status = Statuses.FAIL_REGISTER_KEY_INVALID;
			}
		} else {
			mResponse.status = Statuses.FAIL_REGISTER_KEY_INVALID;
		}
		return false;
	}
	// Not beta buildType
	else {
		return true;
	}
}
}
