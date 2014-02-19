package com.spiddekauga.voider.menu;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.method.LoginMethodResponse;
import com.spiddekauga.voider.network.entities.method.RegisterUserMethodResponse;
import com.spiddekauga.voider.repo.UserLocalRepo;
import com.spiddekauga.voider.repo.UserWebRepo;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.UserInfo;

/**
 * Login scene
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class LoginScene extends Scene {
	/**
	 * Default constructor
	 */
	public LoginScene() {
		super(new LoginGui());

		((LoginGui)mGui).setLoginScene(this);

		login();
	}

	@Override
	protected void loadResources() {
		ResourceCacheFacade.load(InternalNames.UI_GENERAL);

		super.loadResources();
	}

	@Override
	protected void unloadResources() {
		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);

		super.unloadResources();
	}

	@Override
	public boolean keyDown(int keycode) {
		if (KeyHelper.isBackPressed(keycode)) {
			Gdx.app.exit();
			return true;
		}

		return false;
	}

	/**
	 * Try to login using stored username and private key
	 */
	void login() {
		UserInfo userInfo = UserLocalRepo.getLastUser();

		if (userInfo != null && userInfo.online) {
			LoginMethodResponse response = UserWebRepo.login(userInfo.username, userInfo.privateKey);

			if (response != null) {
				if (response.success) {
					setOutcome(Outcomes.LOGGED_IN);
					Config.Network.setOnline(true);
				} else {
					mGui.showErrorMessage("Could not auto-login " + userInfo.username);
					UserLocalRepo.removeLastUser();
				}
			}
			// Failed to auto-login, server could be down. Go offline
			else {
				setOutcome(Outcomes.LOGGED_IN);
				Config.Network.setOnline(false);
				Config.User.setUsername(userInfo.username);
			}
		}
		// Test offline
		else if (userInfo != null && !userInfo.online) {
			loginOffline(userInfo.username, userInfo.password);
		}
	}

	/**
	 * Try to login with the specified username and password
	 * @param username the username to login with
	 * @param password the password to login with
	 * @return true if the user was successfully logged in
	 */
	boolean login(String username, String password) {
		LoginMethodResponse response = UserWebRepo.login(username, password);

		if (response != null) {
			if (response.success) {
				// Update last user to login for auto-login
				UserLocalRepo.setLastUser(response.username, response.privateKey);
				Config.User.setUsername(response.username);

				setOutcome(Outcomes.LOGGED_IN);
				Config.Network.setOnline(true);
				return true;
			}
		}

		// Try to login offline instead
		return loginOffline(username, password);
	}

	/**
	 * Try to login offline
	 * @param username
	 * @param password
	 * @return true if succeeded
	 */
	private boolean loginOffline(String username, String password) {
		UserInfo foundUser = UserLocalRepo.getTempUser(username);

		if (foundUser != null) {
			if (password.equals(foundUser.password)) {
				UserLocalRepo.setLastUser(foundUser.username, password);
				Config.User.setUsername(foundUser.username);
				setOutcome(Outcomes.LOGGED_IN);
				Config.Network.setOnline(false);
				return true;
			}
		}

		return false;
	}

	/**
	 * @return true if register option is available
	 */
	boolean isRegisterAvailable() {
		if (Config.Debug.RELEASE) {
			return UserLocalRepo.isRegisterAvailable();
		} else {
			return true;
		}
	}

	/**
	 * Try to register a new user with the specified username, password, and email
	 * @param username the new username
	 * @param password the new password
	 * @param email the email to register with the username
	 * @return true if the register was successful
	 */
	boolean register(String username, String password, String email) {
		RegisterUserMethodResponse response = UserWebRepo.register(username, password, email);

		if (response != null) {
			switch (response.status) {
			case SUCCESS:
				setOutcome(Outcomes.NOT_APPLICAPLE);

				UserLocalRepo.setLastUser(username, response.privateKey);
				UserLocalRepo.setAsRegistered();

				return true;


			case FAIL_EMAIL_EXISTS:
				mGui.showErrorMessage("Email is already registered");
				break;


			case FAIL_USERNAME_EXISTS:
				mGui.showErrorMessage("Username is occupied");
				break;


				// Connection error, create a local user instead
			case FAIL_SERVER: {
				((LoginGui)mGui).showCreateOfflineUser();
				break;
			}
			}
		} else {
			((LoginGui)mGui).showCreateOfflineUser();
		}

		return false;
	}

	/**
	 * Creates an offline user
	 * @param username
	 * @param password
	 * @param email
	 */
	void createOfflineUser(String username, String password, String email) {
		boolean success = UserLocalRepo.createTempUser(username, password, email);

		if (success) {
			UserLocalRepo.setLastUser(username, password);
			UserLocalRepo.setAsRegistered();
			Config.User.setUsername(username);
			setOutcome(Outcomes.LOGGED_IN);
		} else {
			mGui.showErrorMessage("A temporary user with that username or email already exists");
		}
	}

	@Override
	protected Scene getNextScene() {
		return new MainMenu();
	}


}
