package com.spiddekauga.voider.repo.user;

import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.user.AccountChangeMethod;
import com.spiddekauga.voider.network.user.AccountChangeResponse;
import com.spiddekauga.voider.network.user.AccountChangeResponse.AccountChangeStatuses;
import com.spiddekauga.voider.network.user.LoginMethod;
import com.spiddekauga.voider.network.user.LoginResponse;
import com.spiddekauga.voider.network.user.LogoutMethod;
import com.spiddekauga.voider.network.user.LogoutResponse;
import com.spiddekauga.voider.network.user.RegisterUserMethod;
import com.spiddekauga.voider.network.user.RegisterUserResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.Repo;

/**
 * User web repository
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class UserRepo extends Repo {
	/**
	 * Private constructor to enforce singleton pattern
	 */
	private UserRepo() {
		// TODO auto-generated method
	}

	/**
	 * @return instance of this class
	 */
	public static UserRepo getInstance() {
		if (mInstance == null) {
			mInstance = new UserRepo();
		}
		return mInstance;
	}

	/**
	 * Tries to login the user either with password or private key if it exists
	 * @param user the user to try to login.
	 * @param responseListeners listens to the web response
	 */
	void login(User user, IResponseListener... responseListeners) {
		mWebRepo.login(user, mLocalRepo.getClientId(), addToFront(responseListeners, this));
	}

	/**
	 * Tries to create a new user
	 * @param user all necessary user information. Needs username, password and email to
	 *        continue.
	 * @param responseListeners listens to the web response
	 */
	void register(User user, IResponseListener... responseListeners) {
		mWebRepo.register(user, mLocalRepo.getClientId(), addToFront(responseListeners, this));
	}

	/**
	 * Tries to logout the current user
	 * @param responseListeners listens to the web response
	 */
	void logout(IResponseListener... responseListeners) {
		mWebRepo.logout(addToFront(responseListeners, this));
	}

	/**
	 * Send a password reset token to the user
	 * @param email user's email
	 * @param responseListeners listens to the web response
	 */
	public void passwordResetSendToken(String email, IResponseListener... responseListeners) {
		mWebRepo.passwordResetSendToken(email, responseListeners);
	}

	/**
	 * Tries to reset a password
	 * @param email user's email
	 * @param password new password
	 * @param token reset password token
	 * @param responseListeners listens to the web response
	 */
	public void passwordReset(String email, String password, String token, IResponseListener... responseListeners) {
		mWebRepo.passwordResetSendToken(email, responseListeners);
	}

	/**
	 * Tries to change the password
	 * @param oldPassword
	 * @param newPassword
	 * @param responseListeners listens to the web response
	 */
	void changePassword(String oldPassword, String newPassword, IResponseListener... responseListeners) {
		mWebRepo.changePassword(oldPassword, newPassword, addToFront(responseListeners, this));
	}

	/**
	 * Get information of the last user that was logged in
	 * @return last user logged in, null if not found
	 */
	public User getLastUser() {
		return mLocalRepo.getLastUser();
	}

	/**
	 * @return user analytics id for this client
	 */
	public UUID getAnalyticsId() {
		return mLocalRepo.getAnalyticsId();
	}

	/**
	 * @return client ID
	 */
	public UUID getClientId() {
		return mLocalRepo.getClientId();
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		// Register
		if (method instanceof RegisterUserMethod) {
			handleRegister((RegisterUserMethod) method, (RegisterUserResponse) response);
		}
		// Login
		else if (method instanceof LoginMethod) {
			handleLogin((LoginMethod) method, (LoginResponse) response);
		}
		// Logout
		else if (method instanceof LogoutMethod) {
			handleLogout((LogoutMethod) method, (LogoutResponse) response);
		}
		// Account Change
		else if (method instanceof AccountChangeMethod) {
			handleAccountChange((AccountChangeMethod) method, (AccountChangeResponse) response);
		}
	}

	private void handleRegister(RegisterUserMethod method, RegisterUserResponse response) {
		// Set last user
		if (response.isSuccessful()) {
			mLocalRepo.setLastUser(method.username, response.privateKey, response.userKey);
		}
	}

	private void handleLogin(LoginMethod method, LoginResponse response) {
		// Logged in through text (not auto-login) -> Set last user
		if (response.isSuccessful() && method.privateKey == null) {
			mLocalRepo.setLastUser(response.username, response.privateKey, response.userKey);
		}
	}

	private void handleLogout(LogoutMethod method, LogoutResponse response) {
		// Always clear last user
		mLocalRepo.removeLastUser();
	}

	private void handleAccountChange(AccountChangeMethod method, AccountChangeResponse response) {
		if (response.status.isSuccessful()) {
			// Change password success -> Set new private key
			if (response.privateKey != null && response.changeStatuses.contains(AccountChangeStatuses.PASSWORD_SUCCESS)) {
				mLocalRepo.setPrivateKey(response.privateKey);
			}
		}
	}

	private static UserRepo mInstance = null;
	private UserWebRepo mWebRepo = UserWebRepo.getInstance();
	private UserLocalRepo mLocalRepo = UserLocalRepo.getInstance();
}
