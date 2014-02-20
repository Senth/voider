package com.spiddekauga.network;

import com.spiddekauga.voider.prototype.entities.IEntity;
import com.spiddekauga.voider.prototype.entities.LoginMethod;
import com.spiddekauga.voider.prototype.entities.LoginMethodResponse;
import com.spiddekauga.voider.prototype.entities.NetworkEntitySerializer;

/**
 * Repository for handling server calls regarding the user
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class UserRepository {
	/**
	 * Tries to login the user
	 * @param username
	 * @param password
	 * @return true if successfully logged in
	 */
	public static boolean login(String username, String password) {
		LoginMethod loginMethod = new LoginMethod();
		loginMethod.password = password;
		loginMethod.username = username;

		byte[] entitySend = NetworkEntitySerializer.serializeEntity(loginMethod);
		byte[] response = WebGateway.sendRequest(loginMethod.getMethodName(), entitySend);

		IEntity entity = NetworkEntitySerializer.deserializeEntity(response);

		if (entity instanceof LoginMethodResponse) {
			return ((LoginMethodResponse) entity).success;
		}

		return false;
	}
}
