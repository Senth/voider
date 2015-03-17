package com.spiddekauga.voider.menu;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.user.PasswordResetMethod;
import com.spiddekauga.voider.network.user.PasswordResetResponse;
import com.spiddekauga.voider.network.user.PasswordResetSendTokenResponse;
import com.spiddekauga.voider.network.user.RegisterUserResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.user.UserLocalRepo;
import com.spiddekauga.voider.repo.user.UserWebRepo;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.sound.Music;
import com.spiddekauga.voider.sound.MusicInterpolations;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;
import com.spiddekauga.voider.utils.event.MotdEvent;
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
		ResourceCacheFacade.load(InternalDeps.UI_SFX);

		super.loadResources();
	}

	@Override
	protected void unloadResources() {
		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
		ResourceCacheFacade.unload(InternalNames.MUSIC_TITLE);
		ResourceCacheFacade.unload(InternalDeps.UI_SFX);

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
		eventDispatcher.connect(EventTypes.MOTD_CURRENT, mMotdListener);

		mMusicPlayer.play(Music.TITLE, MusicInterpolations.FADE_IN);

		login();
	}

	@Override
	protected void onDispose() {
		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.disconnect(EventTypes.USER_LOGIN, mLoginListener);
		eventDispatcher.disconnect(EventTypes.USER_LOGIN_FAILED, mLoginListener);
		eventDispatcher.disconnect(EventTypes.UPDATE_AVAILABLE, mUpdateListener);
		eventDispatcher.disconnect(EventTypes.UPDATE_REQUIRED, mUpdateListener);
		eventDispatcher.disconnect(EventTypes.MOTD_CURRENT, mMotdListener);

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
		User userInfo = mUserLocalRepo.getLastUser();

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
	 * Handle register response
	 * @param response the register response
	 */
	private void handleRegisterResponse(RegisterUserResponse response) {
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

		case FAIL_SERVER_CONNECTION:
		case FAIL_SERVER_ERROR:
			((LoginGui) mGui).showConnectionError();
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

	/**
	 * Try to send a password reset token to the specified email
	 * @param email the email to send the token to
	 */
	void passwordResetSendToken(String email) {
		UserWebRepo.getInstance().passwordResetSendToken(email, this);
		mGui.showWaitWindow("Sending reset token to email...");
	}

	/**
	 * Try and reset the user's password
	 * @param email
	 * @param password
	 * @param token
	 */
	void resetPassword(String email, String password, String token) {
		UserWebRepo.getInstance().passwordReset(email, password, token, this);
		mGui.showWaitWindow("Resetting password");
	}

	/**
	 * Handle password reset token
	 * @param response web response
	 */
	private void handlePasswordResetSendToken(PasswordResetSendTokenResponse response) {
		mGui.hideWaitWindow();

		switch (response.status) {
		case FAILED_EMAIL:
			((LoginGui) mGui).setPasswordResetSendError("does not exist");
			break;
		case FAILED_SERVER_CONNECTION:
		case FAILED_SERVER_ERROR:
			((LoginGui) mGui).showConnectionError();
			break;
		case SUCCESS:
			((LoginGui) mGui).showPasswordResetWindow();
			mNotification.showSuccess("Password token sent to email. Please check your email :)");
			break;
		}
	}

	/**
	 * Handle password reset
	 * @param method server parameters
	 * @param response web response
	 */
	private void handlePasswordReset(PasswordResetMethod method, PasswordResetResponse response) {
		mGui.hideWaitWindow();

		switch (response.status) {
		case FAILED_EXPIRED:
			((LoginGui) mGui).setPasswordResetTokenError("expired");
			break;
		case FAILED_SERVER_CONNECTION:
		case FAILED_SERVER_ERROR:
			((LoginGui) mGui).showConnectionError();
			break;
		case FAILED_TOKEN:
			((LoginGui) mGui).setPasswordResetTokenError("invalid");
			break;
		case FAILED_PASSWORD_TOO_SHORT:
			((LoginGui) mGui).setPasswordResetPasswordError("too short");
			break;
		case SUCCESS:
			((LoginGui) mGui).showLoginWindow();
			mNotification.showSuccess("Password changed!");
			login(method.email, method.password);
			break;
		}
	}

	@Override
	protected Scene getNextScene() {
		return new MainMenu();
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		// Register
		if (response instanceof RegisterUserResponse) {
			handleRegisterResponse((RegisterUserResponse) response);
		}
		// Password reset (send token)
		else if (response instanceof PasswordResetSendTokenResponse) {
			handlePasswordResetSendToken((PasswordResetSendTokenResponse) response);
		}
		// Password reset (actual reset)
		else if (response instanceof PasswordResetResponse) {
			handlePasswordReset((PasswordResetMethod) method, (PasswordResetResponse) response);
		}
	}

	private IEventListener mLoginListener = new IEventListener() {
		@Override
		public void handleEvent(GameEvent event) {
			switch (event.type) {
			case USER_LOGIN:
				mGui.hideWaitWindow();
				setOutcome(Outcomes.LOGGED_IN, mLoginInfo);
				break;

			case USER_LOGIN_FAILED:
				mGui.hideWaitWindow();

				// Show client update information
				if (mLoginInfo.updateInfo != null) {
					switch (mLoginInfo.updateInfo.type) {
					case UPDATE_AVAILABLE:
						((LoginGui) mGui).showUpdateAvailable(mLoginInfo.updateInfo.latestClientVersion, mLoginInfo.updateInfo.changeLog);
						break;

					case UPDATE_REQUIRED:
						((LoginGui) mGui).showUpdateRequired(mLoginInfo.updateInfo.latestClientVersion, mLoginInfo.updateInfo.changeLog);
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
				mLoginInfo.updateInfo = (UpdateEvent) event;
			}
		}
	};

	private IEventListener mMotdListener = new IEventListener() {
		@Override
		public void handleEvent(GameEvent event) {
			if (event instanceof MotdEvent) {
				mLoginInfo.motds = ((MotdEvent) event).motds;
			}
		}
	};

	private LoginInfo mLoginInfo = new LoginInfo();
	/** Logging in user */
	private User mLoggingInUser = new User();
	private UserLocalRepo mUserLocalRepo = UserLocalRepo.getInstance();
}
