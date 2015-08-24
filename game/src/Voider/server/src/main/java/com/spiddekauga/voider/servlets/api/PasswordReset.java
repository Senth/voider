package com.spiddekauga.voider.servlets.api;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.utils.BCrypt;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.user.PasswordResetMethod;
import com.spiddekauga.voider.network.user.PasswordResetResponse;
import com.spiddekauga.voider.network.user.PasswordResetResponse.Statuses;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPasswordReset;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUsers;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Try to reset a password
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class PasswordReset extends VoiderApiServlet<PasswordResetMethod> {
	@Override
	protected void onInit() {
		mResponse = new PasswordResetResponse();
		mResponse.status = Statuses.FAILED_SERVER_ERROR;
	}

	@Override
	protected IEntity onRequest(PasswordResetMethod method) throws ServletException, IOException {
		Entity user = getUser(method.email);
		Key tokenKey = isTokenValid(user.getKey(), method.token);
		if (tokenKey != null) {
			if (changePassword(user, method.password)) {
				removeToken(tokenKey);
			}
		}

		return mResponse;
	}

	/**
	 * Get the user entity from an email
	 * @param email
	 * @return user entity, null if not found
	 */
	private Entity getUser(String email) {
		return DatastoreUtils.getSingleEntity(DatastoreTables.USERS, new FilterWrapper(CUsers.EMAIL, email));
	}

	/**
	 * Checks if the token is valid for this user
	 * @param userKey
	 * @param token
	 * @return datastore key if token is valid, null if invalid. If invalid the error code
	 *         will be updated
	 */
	private Key isTokenValid(Key userKey, String token) {
		Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.PASSWORD_RESET, new FilterWrapper(CPasswordReset.TOKEN, token));

		// Doesn't match
		if (userKey == null || entity == null || !userKey.equals(entity.getParent())) {
			mResponse.status = Statuses.FAILED_TOKEN;
			return null;
		}

		// Expired
		Date expires = (Date) entity.getProperty(CPasswordReset.EXPIRES);
		if (expires.before(new Date())) {
			mResponse.status = Statuses.FAILED_EXPIRED;
			return null;
		}

		return entity.getKey();
	}

	/**
	 * Change the password
	 * @param user user entity
	 * @param password new password
	 * @return false if the password wasn't changed
	 */
	private boolean changePassword(Entity user, String password) {
		if (!RegisterUser.isPasswordLengthValid(password)) {
			mResponse.status = Statuses.FAILED_PASSWORD_TOO_SHORT;
			return false;
		}

		// New Private key
		UUID privateKey = UUID.randomUUID();
		DatastoreUtils.setProperty(user, CUsers.PRIVATE_KEY, privateKey);

		// Hashed password
		String salt = BCrypt.gensalt();
		String hashedPassword = BCrypt.hashpw(password, salt);
		user.setProperty(CUsers.PASSWORD, hashedPassword);

		Key userKey = DatastoreUtils.put(user);

		if (userKey == null) {
			return false;
		}

		mResponse.status = Statuses.SUCCESS;
		return true;
	}

	/**
	 * Removes the token from the database
	 * @param tokenKey
	 */
	private void removeToken(Key tokenKey) {
		DatastoreUtils.delete(tokenKey);
	}

	private PasswordResetResponse mResponse = null;
}
