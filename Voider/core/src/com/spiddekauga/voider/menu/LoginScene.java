package com.spiddekauga.voider.menu;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.user.PasswordResetMethod;
import com.spiddekauga.voider.network.user.PasswordResetResponse;
import com.spiddekauga.voider.network.user.PasswordResetSendTokenResponse;
import com.spiddekauga.voider.network.user.RegisterUserResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.repo.user.UserRepo;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.utils.scene.ui.Scene;
import com.spiddekauga.voider.sound.Music;
import com.spiddekauga.voider.sound.MusicInterpolations;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Login scene
 */
public class LoginScene extends Scene implements IResponseListener {
private IEventListener mLoginListener = new IEventListener() {

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.type) {
		case USER_LOGIN:
			getGui().hideWaitWindow();
			setOutcome(Outcomes.LOGGED_IN);
			break;

		case USER_LOGIN_FAILED:
			getGui().hideWaitWindow();
			getGui().focusPasswordField();
			break;

		default:
			// Does nothing
			break;
		}

	}
};
/** Logging in user */
private User mLoggingInUser = new User();
private UserRepo mUserRepo = UserRepo.getInstance();

/**
 * Default constructor
 */
public LoginScene() {
	super(new LoginGui());

	getGui().setLoginScene(this);
}

@Override
public boolean onKeyDown(int keycode) {
	if (KeyHelper.isBackPressed(keycode)) {
		Gdx.app.exit();
		return true;
	}

	return super.onKeyDown(keycode);
}

@Override
protected void loadResources() {
	ResourceCacheFacade.load(this, InternalDeps.UI_GENERAL);
	ResourceCacheFacade.load(this, InternalNames.MUSIC_TITLE);
	ResourceCacheFacade.load(this, InternalDeps.UI_SFX);
	ResourceCacheFacade.load(this, InternalNames.TXT_TERMS);

	super.loadResources();
}

@Override
public void onResume(Outcomes outcome, Object message, Outcomes loadingOutcome) {
	super.onResume(outcome, message, loadingOutcome);

	EventDispatcher eventDispatcher = EventDispatcher.getInstance();
	eventDispatcher.connect(EventTypes.USER_LOGIN, mLoginListener);
	eventDispatcher.connect(EventTypes.USER_LOGIN_FAILED, mLoginListener);

	mMusicPlayer.play(Music.TITLE, MusicInterpolations.FADE_IN);

	login();
}

@Override
protected void onDestroy() {
	EventDispatcher eventDispatcher = EventDispatcher.getInstance();
	eventDispatcher.disconnect(EventTypes.USER_LOGIN, mLoginListener);
	eventDispatcher.disconnect(EventTypes.USER_LOGIN_FAILED, mLoginListener);

	super.onDestroy();
}

@Override
protected Scene getNextScene() {
	return new MainMenu();
}

@Override
protected LoginGui getGui() {
	return (LoginGui) super.getGui();
}

/**
 * Try to login using stored username and private key
 */
private void login() {
	User userInfo = mUserRepo.getLastUser();

	if (userInfo != null && userInfo.isOnline()) {
		mLoggingInUser.set(userInfo);
		mLoggingInUser.login();
		getGui().showWaitWindow("Auto logging in as " + userInfo.getUsername());
	}
	// Test offline
	else {
		getGui().focusUsernameField();
	}
}

/**
 * Try to register a new user with the specified username, password, and email
 * @param username the new username
 * @param password the new password
 * @param email the email to register with the username
 * @param registerKey beta key for registering, null if not used
 */
void register(String username, String password, String email, String registerKey) {
	mLoggingInUser.setUsername(username);
	mLoggingInUser.setEmail(email);
	mLoggingInUser.setPassword(password);
	mLoggingInUser.setBetaKey(registerKey);
	mLoggingInUser.register(this);
	getGui().showWaitWindow("Registering...");
}

/**
 * Try to send a password reset token to the specified email
 * @param email the email to send the token to
 */
void passwordResetSendToken(String email) {
	mUserRepo.passwordResetSendToken(email, this);
	getGui().showWaitWindow("Sending reset token to email...");
}

/**
 * Try and reset the user's password
 * @param email user email or username
 * @param password new password
 * @param token password token gotten from the email
 */
void resetPassword(String email, String password, String token) {
	mUserRepo.passwordReset(email, password, token, this);
	getGui().showWaitWindow("Resetting password");
}

/**
 * @return terms for registering
 */
String getTerms() {
	return ResourceCacheFacade.get(InternalNames.TXT_TERMS);
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

/**
 * Handle register response
 * @param response the register response
 */
private void handleRegisterResponse(RegisterUserResponse response) {
	getGui().hideWaitWindow();

	switch (response.status) {
	case SUCCESS:
		login();
		break;

	case FAIL_EMAIL_EXISTS:
		getGui().setRegisterEmailError("is already registered");
		break;

	case FAIL_USERNAME_EXISTS:
		getGui().setRegisterUsernameError("name is occupied");
		break;

	case FAIL_USERNAME_TOO_SHORT:
		getGui().setRegisterUsernameError("too short");
		break;

	case FAIL_PASSWORD_TOO_SHORT:
		getGui().setRegisterPasswordError("too short");
		break;

	case FAIL_REGISTER_KEY_INVALID:
		getGui().setRegisterKeyError("invalid");
		break;

	case FAIL_REGISTER_KEY_USED:
		getGui().setRegisterKeyError("has been used");
		break;

	case FAIL_SERVER_CONNECTION:
	case FAIL_SERVER_ERROR:
		getGui().showConnectionError();
		break;
	}
}

/**
 * Handle password reset token
 * @param response web response
 */
private void handlePasswordResetSendToken(PasswordResetSendTokenResponse response) {
	getGui().hideWaitWindow();

	switch (response.status) {
	case FAILED_EMAIL:
		getGui().setPasswordResetSendError("does not exist");
		break;
	case FAILED_SERVER_CONNECTION:
	case FAILED_SERVER_ERROR:
		getGui().showConnectionError();
		break;
	case SUCCESS:
		getGui().showPasswordResetWindow();
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
	getGui().hideWaitWindow();

	switch (response.status) {
	case FAILED_EXPIRED:
		getGui().setPasswordResetTokenError("expired");
		break;
	case FAILED_SERVER_CONNECTION:
	case FAILED_SERVER_ERROR:
		getGui().showConnectionError();
		break;
	case FAILED_TOKEN:
		getGui().setPasswordResetTokenError("invalid");
		break;
	case FAILED_PASSWORD_TOO_SHORT:
		getGui().setPasswordResetPasswordError("too short");
		break;
	case SUCCESS:
		getGui().showLoginWindow();
		mNotification.showSuccess("Password changed!");
		login(method.email, method.password);
		break;
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
	getGui().showWaitWindow("Logging in");
}
}
