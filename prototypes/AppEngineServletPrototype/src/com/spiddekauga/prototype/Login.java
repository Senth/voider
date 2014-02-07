package com.spiddekauga.prototype;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.prototype.ServerConfig.DatastoreTables;
import com.spiddekauga.utils.BCrypt;
import com.spiddekauga.voider.prototype.entities.IEntity;
import com.spiddekauga.voider.prototype.entities.LoginMethod;
import com.spiddekauga.voider.prototype.entities.LoginMethodResponse;
import com.spiddekauga.voider.prototype.entities.NetworkEntitySerializer;
import com.spiddekauga.web.VoiderServlet;

/**
 * Tries to login a user
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class Login extends VoiderServlet {

	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		byte[] entityData = NetworkGateway.getEntity(request);
		IEntity networkEntity = NetworkEntitySerializer.deserializeEntity(entityData);

		LoginMethodResponse methodResponse = new LoginMethodResponse();
		methodResponse.success = false;

		if (networkEntity instanceof LoginMethod) {
			Entity datastoreEntity = DatastoreUtils.getSingleItem(DatastoreTables.USERS.toString(), "username", ((LoginMethod) networkEntity).username);

			if (datastoreEntity != null) {
				String hashedPassword = (String)datastoreEntity.getProperty("password");

				if (BCrypt.checkpw(((LoginMethod) networkEntity).password, hashedPassword)) {
					mUser.login(((LoginMethod) networkEntity).username);
					methodResponse.success = true;
				}
			}
		}

		byte[] byteResponse = NetworkEntitySerializer.serializeEntity(methodResponse);
		NetworkGateway.sendResponse(response, byteResponse);
	}

}
