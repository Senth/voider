package com.spiddekauga.voider.repo.user;

import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.user.LoginMethod;
import com.spiddekauga.voider.network.user.LoginResponse;
import com.spiddekauga.voider.network.user.LogoutMethod;
import com.spiddekauga.voider.network.user.LogoutResponse;
import com.spiddekauga.voider.network.user.PasswordResetMethod;
import com.spiddekauga.voider.network.user.PasswordResetResponse;
import com.spiddekauga.voider.network.user.PasswordResetSendTokenMethod;
import com.spiddekauga.voider.network.user.PasswordResetSendTokenResponse;
import com.spiddekauga.voider.network.user.RegisterUserMethod;
import com.spiddekauga.voider.network.user.RegisterUserResponse;
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
		registerMethod.key = user.getRegisterKey();

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

	/**
	 * Send a password reset token to the user
	 * @param email user's email
	 * @param responseListeners listens to the web response
	 */
	public void passwordResetSendToken(String email, IResponseListener... responseListeners) {
		PasswordResetSendTokenMethod method = new PasswordResetSendTokenMethod();
		method.email = email;

		sendInNewThread(method, responseListeners);
	}

	/**
	 * Tries to reset a password
	 * @param email user's email
	 * @param password new password
	 * @param token reset password token
	 * @param responseListeners listens to the web response
	 */
	public void passwordReset(String email, String password, String token, IResponseListener... responseListeners) {
		PasswordResetMethod method = new PasswordResetMethod();
		method.email = email;
		method.password = password;
		method.token = token;

		sendInNewThread(method, responseListeners);
	}

	@Override
	protected void handleResponse(IMethodEntity methodEntity, IEntity response, IResponseListener[] callerResponseListeners) {
		IEntity responseToSend = null;

		// Login
		if (methodEntity instanceof LoginMethod) {
			if (response instanceof LoginResponse) {
				responseToSend = response;
			} else {
				responseToSend = new LoginResponse();
			}
		}
		// Register
		else if (methodEntity instanceof RegisterUserMethod) {
			if (response instanceof RegisterUserResponse) {
				responseToSend = response;
			} else {
				responseToSend = new RegisterUserResponse();
			}
		}
		// Logout
		else if (methodEntity instanceof LogoutMethod) {
			if (response instanceof LogoutResponse) {
				responseToSend = response;
			} else {
				responseToSend = new LogoutResponse();
			}
		}
		// Password Reset -> Send Token
		else if (methodEntity instanceof PasswordResetSendTokenMethod) {
			if (response instanceof PasswordResetSendTokenResponse) {
				responseToSend = response;
			} else {
				responseToSend = new PasswordResetSendTokenResponse();
			}
		} else if (methodEntity instanceof PasswordResetMethod) {
			if (response instanceof PasswordResetResponse) {
				responseToSend = response;
			} else {
				responseToSend = new PasswordResetResponse();
			}
		}


		// Send the actual response
		if (responseToSend != null) {
			sendResponseToListeners(methodEntity, responseToSend, callerResponseListeners);
		}
	}


	/** Instance of this class */
	private static UserWebRepo mInstance = null;
}
