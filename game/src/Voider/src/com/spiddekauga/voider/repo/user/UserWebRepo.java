package com.spiddekauga.voider.repo.user;

import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.user.LoginMethod;
import com.spiddekauga.voider.network.entities.user.LoginMethodResponse;
import com.spiddekauga.voider.network.entities.user.LogoutMethod;
import com.spiddekauga.voider.network.entities.user.LogoutMethodResponse;
import com.spiddekauga.voider.network.entities.user.RegisterUserMethod;
import com.spiddekauga.voider.network.entities.user.RegisterUserMethodResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebRepo;


/**
 * Repository for handling server calls regarding the user
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class UserWebRepo extends WebRepo {
	/**
	 * @return instance of this class
	 */
	public static UserWebRepo getInstance() {
		if (mInstance == null) {
			mInstance = new UserWebRepo();
		}

		return mInstance;
	}

	/**
	 * Protected constructor to enforce singleton usage
	 */
	protected UserWebRepo() {
		// Does nothing
	}

	/**
	 * Tries to login the user
	 * @param responseListener listens to the web response
	 * @param username
	 * @param password
	 * @param clientId unique client id
	 */
	public void login(IResponseListener responseListener, String username, String password, UUID clientId) {
		LoginMethod loginMethod = new LoginMethod();
		loginMethod.password = password;
		loginMethod.username = username;
		loginMethod.clientId = clientId;

		sendInNewThread(loginMethod, responseListener);
	}

	/**
	 * Tries to login the user
	 * @param responseListener listens to the web response
	 * @param username
	 * @param privateKey Uses the private key to login instead of a password
	 * @param clientId unique client id
	 */
	public void login(IResponseListener responseListener, String username, UUID privateKey, UUID clientId) {
		LoginMethod loginMethod = new LoginMethod();
		loginMethod.privateKey = privateKey;
		loginMethod.username = username;
		loginMethod.clientId = clientId;

		sendInNewThread(loginMethod, responseListener);
	}

	/**
	 * Tries to create a new user
	 * @param responseListener listens to the web response
	 * @param username
	 * @param password
	 * @param email
	 * @param clientId unique client id
	 */
	public void register(IResponseListener responseListener, String username, String password, String email, UUID clientId) {
		RegisterUserMethod registerMethod = new RegisterUserMethod();
		registerMethod.email = email;
		registerMethod.username = username;
		registerMethod.password = password;

		sendInNewThread(registerMethod, responseListener);
	}

	/**
	 * Tries to logout the current user
	 * @param responseListener listens to the web response
	 */
	public void logout(IResponseListener responseListener) {
		LogoutMethod logoutMethod = new LogoutMethod();

		sendInNewThread(logoutMethod, responseListener);
	}

	@Override
	protected void handleResponse(IMethodEntity methodEntity, IEntity response, IResponseListener[] callerResponseListeners) {
		IEntity responseToSend = null;

		// Login
		if (methodEntity instanceof LoginMethod) {
			if (response instanceof LoginMethodResponse) {
				responseToSend = response;
			} else {
				LoginMethodResponse loginMethodResponse = new LoginMethodResponse();
				loginMethodResponse.status = LoginMethodResponse.Statuses.FAILED_SERVER_CONNECTION;
				responseToSend = loginMethodResponse;
			}
		}
		// Register
		else if (methodEntity instanceof RegisterUserMethod) {
			if (response instanceof RegisterUserMethodResponse) {
				responseToSend = response;
			} else {
				RegisterUserMethodResponse registerUserMethodResponse = new RegisterUserMethodResponse();
				registerUserMethodResponse.status = RegisterUserMethodResponse.Statuses.FAIL_SERVER_CONNECTION;
				responseToSend = registerUserMethodResponse;
			}
		}
		// Logout
		else if (methodEntity instanceof LogoutMethod) {
			if (response instanceof LogoutMethodResponse) {
				responseToSend = response;
			} else {
				LogoutMethodResponse logoutMethodResponse = new LogoutMethodResponse();
				logoutMethodResponse.status = LogoutMethodResponse.Statuses.FAILED_SERVER_CONNECTION;
				responseToSend = logoutMethodResponse;
			}
		}


		// Send the actual response
		if (responseToSend != null) {
			sendResponseToListeners(methodEntity, responseToSend, callerResponseListeners);
		}
	}

	/**
	 * @param responseListener listens to the web response
	 * @param username checks if this username is registered already
	 */
	public void isUsernameExists(IResponseListener responseListener, String username) {
		// TODO
	}

	/**
	 * @param responseListener listens to the web response
	 * @param email checks if this email is registered already
	 */
	public void isEmailExists(IResponseListener responseListener, String email) {
		// TODO
	}

	/** Instance of this class */
	private static UserWebRepo mInstance = null;
}
