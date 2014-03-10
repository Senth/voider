package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.utils.BCrypt;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.network.entities.method.RegisterUserMethod;
import com.spiddekauga.voider.network.entities.method.RegisterUserMethodResponse;
import com.spiddekauga.voider.network.entities.method.RegisterUserMethodResponse.Statuses;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Registers a user
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class RegisterUser extends VoiderServlet {

	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		byte[] entityData = NetworkGateway.getEntity(request);
		IEntity networkEntity = NetworkEntitySerializer.deserializeEntity(entityData);

		RegisterUserMethodResponse methodResponse = new RegisterUserMethodResponse();
		methodResponse.status = Statuses.FAIL_SERVER_ERROR;

		if (networkEntity instanceof RegisterUserMethod) {

			// Check if username is free
			if (!DatastoreUtils.containsEntity(DatastoreTables.USERS.toString(), "username", ((RegisterUserMethod) networkEntity).username)) {
				// Check email
				if (!DatastoreUtils.containsEntity(DatastoreTables.USERS.toString(), "email", ((RegisterUserMethod) networkEntity).email)) {
					createNewUser((RegisterUserMethod) networkEntity, methodResponse);
				} else {
					methodResponse.status = Statuses.FAIL_EMAIL_EXISTS;
				}
			} else {
				methodResponse.status = Statuses.FAIL_USERNAME_EXISTS;
			}
		}

		byte[] byteResponse = NetworkEntitySerializer.serializeEntity(methodResponse);
		NetworkGateway.sendResponse(response, byteResponse);
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

		// Private key
		UUID privateKey = UUID.randomUUID();
		DatastoreUtils.setProperty(datastoreEntity, "privateKey", privateKey);

		// Hashed password
		String salt = BCrypt.gensalt();
		String hashedPassword = BCrypt.hashpw(networkEntity.password, salt);
		datastoreEntity.setProperty("password", hashedPassword);

		Key userKey = DatastoreUtils.mDatastore.put(datastoreEntity);

		if (userKey != null) {
			methodResponse.userKey = KeyFactory.keyToString(userKey);
			methodResponse.privateKey = privateKey;
			methodResponse.status = Statuses.SUCCESS;
			mUser.login(userKey);
		}
	}
}
