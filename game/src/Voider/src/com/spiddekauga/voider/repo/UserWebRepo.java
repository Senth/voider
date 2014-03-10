package com.spiddekauga.voider.repo;

import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.LoginMethod;
import com.spiddekauga.voider.network.entities.method.LoginMethodResponse;
import com.spiddekauga.voider.network.entities.method.LogoutMethod;
import com.spiddekauga.voider.network.entities.method.LogoutMethodResponse;
import com.spiddekauga.voider.network.entities.method.RegisterUserMethod;
import com.spiddekauga.voider.network.entities.method.RegisterUserMethodResponse;


/**
 * Repository for handling server calls regarding the user
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
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
	 */
	public void login(ICallerResponseListener responseListener, String username, String password) {
		LoginMethod loginMethod = new LoginMethod();
		loginMethod.password = password;
		loginMethod.username = username;

		sendInNewThread(loginMethod, responseListener);
	}

	/**
	 * Tries to login the user
	 * @param responseListener listens to the web response
	 * @param username
	 * @param privateKey Uses the private key to login instead of a password
	 */
	public void login(ICallerResponseListener responseListener, String username, UUID privateKey) {
		LoginMethod loginMethod = new LoginMethod();
		loginMethod.privateKey = privateKey;
		loginMethod.username = username;

		sendInNewThread(loginMethod, responseListener);
	}

	/**
	 * Tries to create a new user
	 * @param responseListener listens to the web response
	 * @param username
	 * @param password
	 * @param email
	 */
	public void register(ICallerResponseListener responseListener, String username, String password, String email) {
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
	public void logout(ICallerResponseListener responseListener) {
		LogoutMethod logoutMethod = new LogoutMethod();

		serializeAndSend(logoutMethod);
	}

	@Override
	protected void handleResponse(IMethodEntity methodEntity, IEntity response, ICallerResponseListener callerResponseListener) {
		// Login
		if (methodEntity instanceof LoginMethod) {
			if (response instanceof LoginMethodResponse) {
				callerResponseListener.handleWebResponse(response);
			} else {
				LoginMethodResponse loginMethodResponse = new LoginMethodResponse();
				loginMethodResponse.status = LoginMethodResponse.Statuses.FAILED_SERVER_CONNECTION;
				callerResponseListener.handleWebResponse(loginMethodResponse);
			}
		}
		// Register
		else if (methodEntity instanceof RegisterUserMethod) {
			if (response instanceof RegisterUserMethodResponse) {
				callerResponseListener.handleWebResponse(response);
			} else {
				RegisterUserMethodResponse registerUserMethodResponse = new RegisterUserMethodResponse();
				registerUserMethodResponse.status = RegisterUserMethodResponse.Statuses.FAIL_SERVER_CONNECTION;
				callerResponseListener.handleWebResponse(registerUserMethodResponse);
			}
		}
		// Logout
		else if (methodEntity instanceof LogoutMethod) {
			if (response instanceof LogoutMethodResponse) {
				callerResponseListener.handleWebResponse(response);
			} else {
				LogoutMethodResponse logoutMethodResponse = new LogoutMethodResponse();
				logoutMethodResponse.status = LogoutMethodResponse.Statuses.FAILED_SERVER_CONNECTION;
				callerResponseListener.handleWebResponse(logoutMethodResponse);
			}
		}
	}

	/**
	 * @param responseListener listens to the web response
	 * @param username checks if this username is registered already
	 */
	public void isUsernameExists(ICallerResponseListener responseListener, String username) {
		// TODO
	}

	/**
	 * @param responseListener listens to the web response
	 * @param email checks if this email is registered already
	 */
	public void isEmailExists(ICallerResponseListener responseListener, String email) {
		// TODO
	}

	/** Instance of this class */
	private static UserWebRepo mInstance = null;
}
