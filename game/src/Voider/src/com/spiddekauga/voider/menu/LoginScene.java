package com.spiddekauga.voider.menu;

import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.LoginMethodResponse;
import com.spiddekauga.voider.network.entities.RegisterUserMethodResponse;
import com.spiddekauga.voider.network.entities.RegisterUserMethodResponse.StatusResponses;
import com.spiddekauga.voider.repo.UserLocalRepo;
import com.spiddekauga.voider.repo.UserWebRepo;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
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
		ResourceCacheFacade.load(ResourceNames.UI_GENERAL);

		super.loadResources();
	}

	@Override
	protected void unloadResources() {
		ResourceCacheFacade.unload(ResourceNames.UI_GENERAL);

		super.unloadResources();
	}

	/**
	 * Try to login using stored username and private key
	 */
	void login() {
		UserInfo userInfo = UserLocalRepo.getLastUser();

		if (userInfo != null) {
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

		return false;
	}

	/**
	 * @return true if register option is available
	 */
	boolean isRegisterAvailable() {
		/** @todo implement register available method */
		return true;
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
			if (response.status == StatusResponses.SUCCESS) {
				setOutcome(Outcomes.NOT_APPLICAPLE);

				// TODO set private key
				// TODO set no register available

				return true;
			}
		}

		return false;
	}

	@Override
	protected Scene getNextScene() {
		return new MainMenu();
	}


}
