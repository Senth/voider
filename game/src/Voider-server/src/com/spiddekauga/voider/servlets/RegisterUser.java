package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.utils.BCrypt;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.RegisterUserMethod;
import com.spiddekauga.voider.network.entities.method.RegisterUserMethodResponse;
import com.spiddekauga.voider.network.entities.method.RegisterUserMethodResponse.Statuses;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Registers a user
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class RegisterUser extends VoiderServlet {

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		RegisterUserMethodResponse methodResponse = new RegisterUserMethodResponse();
		methodResponse.status = Statuses.FAIL_SERVER_ERROR;

		if (methodEntity instanceof RegisterUserMethod) {
			// Check if username is free
			if (!DatastoreUtils.exists(DatastoreTables.USERS.toString(), "username", ((RegisterUserMethod) methodEntity).username)) {
				// Check email
				if (!DatastoreUtils.exists(DatastoreTables.USERS.toString(), "email", ((RegisterUserMethod) methodEntity).email)) {
					createNewUser((RegisterUserMethod) methodEntity, methodResponse);
				} else {
					methodResponse.status = Statuses.FAIL_EMAIL_EXISTS;
				}
			} else {
				methodResponse.status = Statuses.FAIL_USERNAME_EXISTS;
			}
		}

		return methodResponse;
	}

	/**
	 * Adds the new user to the datastore
	 * @param networkEntity the network entity
	 * @param methodResponse entity to respond with
	 */
	private void createNewUser(RegisterUserMethod networkEntity, RegisterUserMethodResponse methodResponse){
		Entity datastoreEntity = new Entity(DatastoreTables.USERS.toString());

		datastoreEntity.setProperty("username", networkEntity.username);
		datastoreEntity.setProperty("created", new Date());
		datastoreEntity.setProperty("logged-in", new Date());
		datastoreEntity.setProperty("email", networkEntity.email);

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
			mUser.login(userKey, networkEntity.clientId);
		}
	}
}
