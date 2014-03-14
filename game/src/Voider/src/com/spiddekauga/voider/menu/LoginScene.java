package com.spiddekauga.voider.menu;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.LoginMethodResponse;
import com.spiddekauga.voider.network.entities.method.RegisterUserMethodResponse;
import com.spiddekauga.voider.repo.ICallerResponseListener;
import com.spiddekauga.voider.repo.UserLocalRepo;
import com.spiddekauga.voider.repo.UserWebRepo;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.User;

/**
 * Login scene
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LoginScene extends Scene implements ICallerResponseListener {
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
		User userInfo = UserLocalRepo.getLastUser();

		if (userInfo != null && userInfo.isOnline()) {
			mAutoLogin = true;
			mLoggingInUser.set(userInfo);
			mUserWebRepo.login(this, userInfo.getUsername(), userInfo.getPrivateKey());
			mGui.showWaitWindow("Logging in as last user");
		}
		// Test offline
		else if (userInfo != null && !userInfo.isOnline()) {
			loginOffline(userInfo.getUsername(), userInfo.getPassword());
		}
	}

	/**
	 * Handles a login response
	 * @param response the login response
	 */
	void handleLoginResopnse(LoginMethodResponse response) {
		User userInfo = UserLocalRepo.getLastUser();

		switch (response.status) {
		case SUCCESS:
			// Update last user to login for auto-login
			UserLocalRepo.setLastUser(response.username, response.privateKey, response.userKey);
			mUser.login(response.username, response.userKey, true);
			setOutcome(Outcomes.LOGGED_IN);
			break;


		case FAILED_USERNAME_PASSWORD_MISMATCH:
			if (mAutoLogin) {
				mGui.showErrorMessage("Could not auto-login " + userInfo.getUsername());
				UserLocalRepo.removeLastUser();
				mAutoLogin = false;
				mUser.setOnline(false);
			} else {
				loginOffline(mLoggingInUser.getUsername(), mLoggingInUser.getPassword());
				mGui.showErrorMessage("No username with that password exists");
			}
			break;


		case FAILED_SERVER_ERROR:
		case FAILED_SERVER_CONNECTION:
			// Login offline if tried to auto-login
			if (mAutoLogin) {
				setOutcome(Outcomes.LOGGED_IN);
				mUser.login(mLoggingInUser.getUsername(), mLoggingInUser.getServerKey(), false);
			} else {
				mGui.showErrorMessage("Could not connect to server");
			}
			break;
		}

		mGui.hideWaitWindow();
	}

	/**
	 * Try to login with the specified username and password
	 * @param username the username to login with
	 * @param password the password to login with
	 */
	void login(String username, String password) {
		mLoggingInUser.setUsername(username);
		mLoggingInUser.setPassword(password);
		mUserWebRepo.login(this, username, password);
		mGui.showWaitWindow("Logging in");
	}

	/**
	 * Try to login offline
	 * @param username
	 * @param password
	 * @return true if succeeded
	 */
	private boolean loginOffline(String username, String password) {
		User foundUser = UserLocalRepo.getTempUser(username);

		if (foundUser != null) {
			if (password.equals(foundUser.getPassword())) {
				UserLocalRepo.setLastUser(foundUser.getUsername(), password);
				mUser.login(mLoggingInUser.getUsername(), foundUser.getServerKey(), false);
				setOutcome(Outcomes.LOGGED_IN);
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
	 * Handle register response
	 * @param response the register response
	 */
	void handleRegisterResponse(RegisterUserMethodResponse response) {
		switch (response.status) {
		case SUCCESS:
			setOutcome(Outcomes.NOT_APPLICAPLE);
			mUser.login(mLoggingInUser.getUsername(), response.userKey, true);
			UserLocalRepo.setLastUser(mLoggingInUser.getUsername(), response.privateKey, response.userKey);
			UserLocalRepo.setAsRegistered();

			break;


		case FAIL_EMAIL_EXISTS:
			mGui.showErrorMessage("Email is already registered");
			break;


		case FAIL_USERNAME_EXISTS:
			mGui.showErrorMessage("Username is occupied");
			break;


			// Connection or server error, create a local user instead
		case FAIL_SERVER_CONNECTION:
		case FAIL_SERVER_ERROR:
			((LoginGui)mGui).showCreateOfflineUser();
			break;
		}

		mGui.hideWaitWindow();
	}

	/**
	 * Try to register a new user with the specified username, password, and email
	 * @param username the new username
	 * @param password the new password
	 * @param email the email to register with the username
	 */
	void register(String username, String password, String email) {
		mLoggingInUser.setUsername(username);
		mLoggingInUser.setEmail(email);
		mLoggingInUser.setPassword(password);
		mUserWebRepo.register(this, username, password, email);
		mGui.showWaitWindow("Contacting server");
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
			mUser.login(username, null, false);
			setOutcome(Outcomes.LOGGED_IN);
		} else {
			mGui.showErrorMessage("A temporary user with that username or email already exists");
		}
	}

	@Override
	protected Scene getNextScene() {
		return new MainMenu();
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		// Login
		if (response instanceof LoginMethodResponse) {
			handleLoginResopnse((LoginMethodResponse) response);
		}
		// Register
		if (response instanceof RegisterUserMethodResponse) {
			handleRegisterResponse((RegisterUserMethodResponse) response);
		}
	}

	/** Global user */
	private static final User mUser = User.getGlobalUser();
	/** User web repository */
	private UserWebRepo mUserWebRepo = UserWebRepo.getInstance();
	/** Currently auto-logging in? */
	private boolean mAutoLogin = false;
	/** Logging in user */
	private User mLoggingInUser = new User();
}
