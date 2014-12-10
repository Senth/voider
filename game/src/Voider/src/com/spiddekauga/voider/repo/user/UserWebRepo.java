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
import com.spiddekauga.voider.utils.User;


/**
 * Repository for handling server calls regarding the user
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
	 * Tries to login the user either with password or private key if it exists
	 * @param user the user to try to login.
	 * @param clientId unique client id
	 * @param responseListeners listens to the web response
	 */
	public void login(User user, UUID clientId, IResponseListener... responseListeners) {
		LoginMethod loginMethod = new LoginMethod();
		loginMethod.clientId = clientId;
		loginMethod.username = user.getUsername();
		loginMethod.password = user.getPassword();
		loginMethod.privateKey = user.getPrivateKey();

		sendInNewThread(loginMethod, responseListeners);
	}

	/**
	 * Tries to create a new user
	 * @param user all necessary user information. Needs username, password and email to
	 *        continue.
	 * @param clientId unique client id
	 * @param responseListeners listens to the web response
	 */
	public void register(User user, UUID clientId, IResponseListener... responseListeners) {
		RegisterUserMethod registerMethod = new RegisterUserMethod();
		registerMethod.clientId = clientId;
		registerMethod.email = user.getEmail();
		registerMethod.username = user.getUsername();
		registerMethod.password = user.getPassword();

		sendInNewThread(registerMethod, responseListeners);
	}

	/**
	 * Tries to logout the current user
	 * @param responseListeners listens to the web response
	 */
	public void logout(IResponseListener... responseListeners) {
		LogoutMethod logoutMethod = new LogoutMethod();

		sendInNewThread(logoutMethod, responseListeners);
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
		// TODO username exists
	}

	/**
	 * @param responseListener listens to the web response
	 * @param email checks if this email is registered already
	 */
	public void isEmailExists(IResponseListener responseListener, String email) {
		// TODO email exists
	}

	/** Instance of this class */
	private static UserWebRepo mInstance = null;
}
