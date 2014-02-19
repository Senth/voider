package com.spiddekauga.voider.repo;

import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.LoginMethod;
import com.spiddekauga.voider.network.entities.method.LoginMethodResponse;
import com.spiddekauga.voider.network.entities.method.LogoutMethod;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.network.entities.method.RegisterUserMethod;
import com.spiddekauga.voider.network.entities.method.RegisterUserMethodResponse;


/**
 * Repository for handling server calls regarding the user
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class UserWebRepo {
	/**
	 * Tries to login the user
	 * @param username
	 * @param password
	 * @return response of the login, null if something went wrong
	 */
	public static LoginMethodResponse login(String username, String password) {
		LoginMethod loginMethod = new LoginMethod();
		loginMethod.password = password;
		loginMethod.username = username;

		IEntity entity = serializeAndSend(loginMethod);

		if (entity instanceof LoginMethodResponse) {
			return ((LoginMethodResponse) entity);
		}

		return null;
	}

	/**
	 * Tries to login the user
	 * @param username
	 * @param privateKey Uses the private key to login instead of a password
	 * @return Login method response, null if something went wrong
	 */
	public static LoginMethodResponse login(String username, UUID privateKey) {
		LoginMethod loginMethod = new LoginMethod();
		loginMethod.privateKey = privateKey;
		loginMethod.username = username;

		IEntity entity = serializeAndSend(loginMethod);

		if (entity instanceof LoginMethodResponse) {
			return ((LoginMethodResponse) entity);
		}

		return null;
	}

	/**
	 * Tries to create a new user
	 * @param username
	 * @param password
	 * @param email
	 * @return the response from the server
	 */
	public static RegisterUserMethodResponse register(String username, String password, String email) {
		RegisterUserMethod registerMethod = new RegisterUserMethod();
		registerMethod.email = email;
		registerMethod.username = username;
		registerMethod.password = password;

		IEntity entity = serializeAndSend(registerMethod);

		if (entity instanceof RegisterUserMethodResponse) {
			return (RegisterUserMethodResponse) entity;
		}

		return null;
	}

	/**
	 * Tries to logout the current user
	 */
	public static void logout() {
		LogoutMethod logoutMethod = new LogoutMethod();

		serializeAndSend(logoutMethod);
	}

	/**
	 * @param username checks if this username is registered already
	 * @return true if the username exists
	 */
	public static boolean isUsernameExists(String username) {
		// TODO
		return false;
	}

	/**
	 * @param email checks if this email is registered already
	 * @return true if the email exists
	 */
	public static boolean isEmailExists(String email) {
		// TODO
		return false;
	}

	/**
	 * Serializes and sends the entity
	 * @param methodEntity the entity to send
	 * @return response entity, null if something went wrong
	 */
	private static IEntity serializeAndSend(IMethodEntity methodEntity) {
		byte[] entitySend = NetworkEntitySerializer.serializeEntity(methodEntity);
		if (entitySend != null) {
			byte[] response = WebGateway.sendRequest(methodEntity.getMethodName(), entitySend);
			if (response != null) {
				return NetworkEntitySerializer.deserializeEntity(response);
			}
		}

		return null;
	}
}
