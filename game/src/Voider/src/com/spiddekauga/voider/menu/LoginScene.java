package com.spiddekauga.voider.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.user.LoginMethodResponse;
import com.spiddekauga.voider.network.entities.user.RegisterUserMethodResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.user.UserLocalRepo;
import com.spiddekauga.voider.repo.user.UserWebRepo;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.User;

/**
 * Login scene
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LoginScene extends Scene implements IResponseListener {
	/**
	 * Default constructor
	 */
	public LoginScene() {
		super(new LoginGui());

		((LoginGui) mGui).setLoginScene(this);
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
	public void onActivate(Outcomes outcome, Object message) {
		super.onActivate(outcome, message);

		login();
	}

	@Override
	public boolean onKeyDown(int keycode) {
		if (KeyHelper.isBackPressed(keycode)) {
			Gdx.app.exit();
			return true;
		}


		// Debug tests
		if (Config.Debug.isBuildOrBelow(Builds.DEV_SERVER)) {

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
			mUserWebRepo.login(this, userInfo.getUsername(), userInfo.getPrivateKey(), UserLocalRepo.getClientId());
			mGui.showWaitWindow("Auto logging in as " + userInfo.getUsername());
		}
		// Test offline
		else {
			((LoginGui) mGui).focusUsernameField();
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
				mGui.hideWaitWindow();
				mGui.showErrorMessage("Could not auto-login " + userInfo.getUsername());
				UserLocalRepo.removeLastUser();
				mAutoLogin = false;
				mUser.setOnline(false);
			} else {
				mGui.showErrorMessage("No username with that password exists");
			}
			((LoginGui) mGui).focusUsernameField();
			break;


		case FAILED_SERVER_ERROR:
		case FAILED_SERVER_CONNECTION:
			// Login offline if tried to auto-login
			if (mAutoLogin) {
				try {
					mUser.login(mLoggingInUser.getUsername(), mLoggingInUser.getServerKey(), false);
					setOutcome(Outcomes.LOGGED_IN);
				} catch (GdxRuntimeException e) {
					// Error with connection
					if (e.getCause() instanceof SQLiteGdxException) {
						mGui.showErrorMessage("Another instance with this user is already running");
					}
					((LoginGui) mGui).focusUsernameField();
				}
			} else {
				mGui.showErrorMessage("Could not connect to server");
				((LoginGui) mGui).focusUsernameField();
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
		mUserWebRepo.login(this, username, password, UserLocalRepo.getClientId());
		mGui.showWaitWindow("Logging in");
	}

	/**
	 * @return true if register option is available
	 */
	boolean isRegisterAvailable() {
		if (Config.Debug.isBuildOrAbove(Builds.BETA)) {
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
			mUser.login(mLoggingInUser.getUsername(), response.userKey, true);
			UserLocalRepo.setLastUser(mLoggingInUser.getUsername(), response.privateKey, response.userKey);
			UserLocalRepo.setAsRegistered();
			setOutcome(Outcomes.NOT_APPLICAPLE);

			break;


		case FAIL_EMAIL_EXISTS:
			((LoginGui) mGui).setRegisterEmailError("is already registered");
			break;


		case FAIL_USERNAME_EXISTS:
			((LoginGui) mGui).setRegisterUsernameError("name is occupied");
			break;

		case FAIL_USERNAME_TOO_SHORT:
			((LoginGui) mGui).setRegisterUsernameError("too short");
			break;

		case FAIL_PASSWORD_TOO_SHORT:
			((LoginGui) mGui).setRegisterPasswordError("too short");

			// Connection or server error, create a local user instead
		case FAIL_SERVER_CONNECTION:
		case FAIL_SERVER_ERROR:
			((LoginGui) mGui).showCouldNotCreateUser();
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
		// ((LoginGui) mGui).clearRegisterErrors();

		mLoggingInUser.setUsername(username);
		mLoggingInUser.setEmail(email);
		mLoggingInUser.setPassword(password);
		mUserWebRepo.register(this, username, password, email, UserLocalRepo.getClientId());
		mGui.showWaitWindow("Contacting server");
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
