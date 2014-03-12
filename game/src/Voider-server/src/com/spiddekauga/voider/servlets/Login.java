package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.utils.BCrypt;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.LoginMethod;
import com.spiddekauga.voider.network.entities.method.LoginMethodResponse;
import com.spiddekauga.voider.network.entities.method.LoginMethodResponse.Statuses;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Tries to login to the server
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Login extends VoiderServlet {

	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LoginMethodResponse methodResponse = new LoginMethodResponse();
		methodResponse.status = Statuses.FAILED_SERVER_ERROR;

		// Skip if already logged in
		if (!mUser.isLoggedIn()) {

			byte[] entityData = NetworkGateway.getEntity(request);
			IEntity networkEntity = NetworkEntitySerializer.deserializeEntity(entityData);

			if (networkEntity instanceof LoginMethod) {
				// Check username vs username first
				Entity datastoreEntity = DatastoreUtils.getSingleEntity(DatastoreTables.USERS.toString(), "username", ((LoginMethod) networkEntity).username);
				// Check username vs email
				if (datastoreEntity == null) {
					datastoreEntity = DatastoreUtils.getSingleEntity(DatastoreTables.USERS.toString(), "email", ((LoginMethod) networkEntity).username);
				}

				if (datastoreEntity != null) {
					// Test password / private key
					if (isPrivateKeyMatch(datastoreEntity, ((LoginMethod) networkEntity).privateKey)) {
						methodResponse.status = Statuses.SUCCESS;
					} else if (isPasswordMatch(datastoreEntity, ((LoginMethod) networkEntity).password)) {
						methodResponse.status = Statuses.SUCCESS;
					}

					// Login and update last logged in date
					if (methodResponse.status == Statuses.SUCCESS) {
						methodResponse.userKey = KeyFactory.keyToString(datastoreEntity.getKey());
						methodResponse.privateKey = DatastoreUtils.getUuidProperty(datastoreEntity, "privateKey");
						methodResponse.username = (String) datastoreEntity.getProperty("username");
						mUser.login(datastoreEntity.getKey());
						updateLastLoggedIn(datastoreEntity);
					}
				}

				if (methodResponse.status != Statuses.SUCCESS) {
					methodResponse.status = Statuses.FAILED_USERNAME_PASSWORD_MISMATCH;
				}
			}
		}

		byte[] byteResponse = NetworkEntitySerializer.serializeEntity(methodResponse);
		NetworkGateway.sendResponse(response, byteResponse);
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

		UUID datastorePrivateKey = DatastoreUtils.getUuidProperty(datastoreEntity, "privateKey");
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

		String hashedPassword = (String)datastoreEntity.getProperty("password");
		return BCrypt.checkpw(password, hashedPassword);
	}

	/**
	 * Updates last logged in of the user
	 * @param userEntity the user that logged in
	 */
	private void updateLastLoggedIn(Entity userEntity) {
		userEntity.setProperty("logged-in", new Date());
		DatastoreUtils.mDatastore.put(userEntity);
	}
}
