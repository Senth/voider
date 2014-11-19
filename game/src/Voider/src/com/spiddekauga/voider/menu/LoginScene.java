package com.spiddekauga.voider.menu;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.user.RegisterUserMethodResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.user.UserLocalRepo;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.sound.Interpolations;
import com.spiddekauga.voider.sound.Music;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;
import com.spiddekauga.voider.utils.event.UpdateEvent;

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
		ResourceCacheFacade.load(InternalNames.MUSIC_TITLE);

		super.loadResources();
	}

	@Override
	protected void unloadResources() {
		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
		ResourceCacheFacade.unload(InternalNames.MUSIC_TITLE);

		super.unloadResources();
	}

	@Override
	public void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.connect(EventTypes.USER_LOGIN, mLoginListener);
		eventDispatcher.connect(EventTypes.USER_LOGIN_FAILED, mLoginListener);
		eventDispatcher.connect(EventTypes.UPDATE_AVAILABLE, mUpdateListener);
		eventDispatcher.connect(EventTypes.UPDATE_REQUIRED, mUpdateListener);

		mMusicPlayer.play(Music.TITLE, Interpolations.FADE_IN);

		login();
	}

	@Override
	protected void onDispose() {
		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.disconnect(EventTypes.USER_LOGIN, mLoginListener);
		eventDispatcher.disconnect(EventTypes.USER_LOGIN_FAILED, mLoginListener);
		eventDispatcher.disconnect(EventTypes.UPDATE_AVAILABLE, mUpdateListener);
		eventDispatcher.disconnect(EventTypes.UPDATE_REQUIRED, mUpdateListener);

		super.onDispose();
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

		return super.onKeyDown(keycode);
	}

	/**
	 * Try to login using stored username and private key
	 */
	void login() {
		User userInfo = UserLocalRepo.getLastUser();

		if (userInfo != null && userInfo.isOnline()) {
			mLoggingInUser.set(userInfo);
			mLoggingInUser.login();
			mGui.showWaitWindow("Auto logging in as " + userInfo.getUsername());
		}
		// Test offline
		else {
			((LoginGui) mGui).focusUsernameField();
		}
	}

	// /**
	// * Handles a login response
	// * @param response the login response
	// */
	// void handleLoginResopnse(LoginMethodResponse response) {
	// User userInfo = UserLocalRepo.getLastUser();
	//
	// switch (response.status) {
	// case SUCCESS:
	// switch (response.clientVersionStatus) {
	// case NEW_VERSION_AVAILABLE:
	// case UP_TO_DATE:
	// UserLocalRepo.setLastUser(response.username, response.privateKey,
	// response.userKey);
	// mUser.login(response.username, response.userKey, true);
	// setOutcome(Outcomes.LOGGED_IN, response);
	// break;
	//
	// case UNKNOWN:
	// case UPDATE_REQUIRED:
	// loginOffline(response, "");
	// break;
	// }
	//
	// break;
	//
	// case FAILED_USERNAME_PASSWORD_MISMATCH:
	// if (mAutoLogin) {
	// mGui.hideWaitWindow();
	// mNotification.show(NotificationTypes.ERROR, "Could not auto-login " +
	// userInfo.getUsername());
	// UserLocalRepo.removeLastUser();
	// mAutoLogin = false;
	// mUser.setOnline(false);
	// } else {
	// mNotification.show(NotificationTypes.ERROR,
	// "No username with that password exists");
	// }
	// ((LoginGui) mGui).focusUsernameField();
	//
	// // Show update available windows
	// switch (response.clientVersionStatus) {
	// case NEW_VERSION_AVAILABLE:
	// ((LoginGui) mGui).showUpdateAvailable(response.latestClientVersion,
	// response.changeLogMessage);
	// break;
	//
	// case UPDATE_REQUIRED:
	// ((LoginGui) mGui).showUpdateNeeded(response.latestClientVersion,
	// response.changeLogMessage);
	// break;
	//
	// case UNKNOWN:
	// case UP_TO_DATE:
	// // Does nothing
	// break;
	// }
	// break;
	//
	//
	// case FAILED_SERVER_ERROR:
	// case FAILED_SERVER_CONNECTION:
	// loginOffline(response, "Could not connect to server");
	// break;
	// }
	//
	// mGui.hideWaitWindow();
	// }

	// /**
	// * Login offline (if available and after server response)
	// * @param response
	// * @param failMessage the message to cannot login offline
	// */
	// private void loginOffline(LoginMethodResponse response, String failMessage) {
	// // Login offline if tried to auto-login
	// if (mAutoLogin) {
	// try {
	// setOutcome(Outcomes.NOT_APPLICAPLE, response);
	// } catch (GdxRuntimeException e) {
	// // Error with connection
	// if (e.getCause() instanceof SQLiteGdxException) {
	// mNotification.show(NotificationTypes.ERROR,
	// "Another instance with this user is already running");
	// }
	// ((LoginGui) mGui).focusUsernameField();
	// }
	// } else {
	// if (failMessage != null && !failMessage.isEmpty()) {
	// mNotification.show(NotificationTypes.ERROR, failMessage);
	// }
	// ((LoginGui) mGui).focusUsernameField();
	// }
	// }

	/**
	 * Try to login with the specified username and password
	 * @param username the username to login with
	 * @param password the password to login with
	 */
	void login(String username, String password) {
		mLoggingInUser.setUsername(username);
		mLoggingInUser.setPassword(password);
		mLoggingInUser.login();
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
		mGui.hideWaitWindow();

		switch (response.status) {
		case SUCCESS:
			login();
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
		mLoggingInUser.register(this);
		mGui.showWaitWindow("Registering...");
	}

	@Override
	protected Scene getNextScene() {
		return new MainMenu();
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		// Register
		if (response instanceof RegisterUserMethodResponse) {
			handleRegisterResponse((RegisterUserMethodResponse) response);
		}
	}

	private IEventListener mLoginListener = new IEventListener() {
		@Override
		public void handleEvent(GameEvent event) {
			switch (event.type) {
			case USER_LOGIN:
				mGui.hideWaitWindow();
				setOutcome(Outcomes.LOGGED_IN, mUpdateInfo);
				break;

			case USER_LOGIN_FAILED:
				mGui.hideWaitWindow();

				// Show client update information
				if (mUpdateInfo != null) {
					switch (mUpdateInfo.type) {
					case UPDATE_AVAILABLE:
						((LoginGui) mGui).showUpdateAvailable(mUpdateInfo.latestClientVersion, mUpdateInfo.changeLog);
						break;

					case UPDATE_REQUIRED:
						((LoginGui) mGui).showUpdateRequired(mUpdateInfo.latestClientVersion, mUpdateInfo.changeLog);
						break;

					default:
						break;
					}
				}
				break;

			default:
				// Does nothing
				break;
			}

		}
	};

	private IEventListener mUpdateListener = new IEventListener() {
		@Override
		public void handleEvent(GameEvent event) {
			if (event instanceof UpdateEvent) {
				mUpdateInfo = (UpdateEvent) event;
			}
		}
	};

	/** Update information for outcome message */
	private UpdateEvent mUpdateInfo = null;
	/** Logging in user */
	private User mLoggingInUser = new User();
}
