package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.utils.BCrypt;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.user.RegisterUserMethod;
import com.spiddekauga.voider.network.entities.user.RegisterUserMethodResponse;
import com.spiddekauga.voider.network.entities.user.RegisterUserMethodResponse.Statuses;
import com.spiddekauga.voider.server.util.ServerConfig;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Registers a user
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class RegisterUser extends VoiderServlet {

	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		RegisterUserMethodResponse methodResponse = new RegisterUserMethodResponse();
		methodResponse.status = Statuses.FAIL_SERVER_ERROR;

		if (methodEntity instanceof RegisterUserMethod) {
			FilterWrapper usernameProperty = new FilterWrapper("username", ((RegisterUserMethod) methodEntity).username);
			FilterWrapper emailProperty = new FilterWrapper("email", ((RegisterUserMethod) methodEntity).email);

			// Check username length
			if (!isUsernameLengthValid(((RegisterUserMethod) methodEntity).username)) {
				methodResponse.status = Statuses.FAIL_USERNAME_TOO_SHORT;
			}
			// Check password length
			else if (!isPasswordLengthValid(((RegisterUserMethod) methodEntity).password)) {
				methodResponse.status = Statuses.FAIL_PASSWORD_TOO_SHORT;
			}
			// Check if username is free
			else if (DatastoreUtils.exists(DatastoreTables.USERS.toString(), usernameProperty)) {
				methodResponse.status = Statuses.FAIL_USERNAME_EXISTS;
			}
			// Check email
			else if (DatastoreUtils.exists(DatastoreTables.USERS.toString(), emailProperty)) {
				methodResponse.status = Statuses.FAIL_EMAIL_EXISTS;
			}
			// All valid
			else {
				createNewUser((RegisterUserMethod) methodEntity, methodResponse);
			}
		}

		return methodResponse;
	}

	/**
	 * Checks minimum password length
	 * @param password the user's password
	 * @return true if the password has the minimum required characters in the password
	 */
	private boolean isPasswordLengthValid(String password) {
		return password.length() >= ServerConfig.UserInfo.PASSWORD_LENGTH_MIN;
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
	 * Adds the new user to the datastore
	 * @param networkEntity the network entity
	 * @param methodResponse entity to respond with
	 */
	private void createNewUser(RegisterUserMethod networkEntity, RegisterUserMethodResponse methodResponse) {
		Entity datastoreEntity = new Entity(DatastoreTables.USERS.toString());

		datastoreEntity.setProperty("username", networkEntity.username);
		datastoreEntity.setUnindexedProperty("created", new Date());
		datastoreEntity.setUnindexedProperty("logged-in", new Date());
		datastoreEntity.setProperty("email", networkEntity.email);
		datastoreEntity.setUnindexedProperty("date_format", "MM/dd/yyyy HH:mm:ss");

		// Private key
		UUID privateKey = UUID.randomUUID();
		DatastoreUtils.setProperty(datastoreEntity, "private_key", privateKey);

		// Hashed password
		String salt = BCrypt.gensalt();
		String hashedPassword = BCrypt.hashpw(networkEntity.password, salt);
		datastoreEntity.setProperty("password", hashedPassword);

		Key userKey = DatastoreUtils.put(datastoreEntity);

		if (userKey != null) {
			methodResponse.userKey = KeyFactory.keyToString(userKey);
			methodResponse.privateKey = privateKey;
			methodResponse.status = Statuses.SUCCESS;
		}
	}
}
