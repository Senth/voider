package com.spiddekauga.voider.repo.user;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.scene.ui.NotificationShower;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.menu.LoginScene;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.user.AccountChangeResponse;
import com.spiddekauga.voider.network.user.AccountChangeResponse.AccountChangeStatuses;
import com.spiddekauga.voider.network.user.LoginResponse;
import com.spiddekauga.voider.network.user.LoginResponse.VersionInformation.Statuses;
import com.spiddekauga.voider.network.user.RegisterUserResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.analytics.AnalyticsRepo;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;
import com.spiddekauga.voider.utils.event.MotdEvent;
import com.spiddekauga.voider.utils.event.ServerRestoreEvent;
import com.spiddekauga.voider.utils.event.UpdateEvent;

import java.util.UUID;

/**
 * User information class
 */
public class User {
private static NotificationShower mNotification = NotificationShower.getInstance();
private static EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
private static UserRepo mUserRepo = UserRepo.getInstance();
/** Global user */
private static User mGlobalUser = new User();
private String mUsername = "(None)";
private String mServerKey = null;
private boolean mOnline = false;
/** Password, usually not used or stored */
private String mPassword = null;
/** Email, usually not used or stored */
private String mEmail = null;
/** Register key for the beta */
private String mRegisterKey = null;
/** Private login key */
private UUID mPrivateKey = null;
private boolean mLoggedIn = false;
private boolean mAskToGoOnline = true;
private IEventListener mDisconnectListener = new IEventListener() {
	@Override
	public void handleEvent(GameEvent event) {
		if (User.this == mGlobalUser) {
			mEventDispatcher.disconnect(EventTypes.SERVER_MAINTENANCE, this);
			mNotification.showHighlight("You have been disconnected from the server");
			disconnect();
		}
	}
};
private IResponseListener mResponseListener = new IResponseListener() {
	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		// Login
		if (response instanceof LoginResponse) {
			handleLoginResponse((LoginResponse) response);
		}
		// Register
		else if (response instanceof RegisterUserResponse) {
			handleRegisterResponse((RegisterUserResponse) response);
		}
		// Account change
		else if (response instanceof AccountChangeResponse) {
			handleAccountChangeResponse((AccountChangeResponse) response);
		}
	}

	private void handleLoginResponse(LoginResponse response) {
		if (response.isSuccessful()) {
			handleLoginSuccess(response);
		}
		// Send error message
		else {
			handleLoginFail(response);
		}

		handleLoginExtra(response);
	}

	private void handleRegisterResponse(RegisterUserResponse response) {
		if (response.isSuccessful()) {
			// Registered and logged in
			if (User.this != mGlobalUser) {
				mPrivateKey = response.privateKey;
				mServerKey = response.userKey;
			}
			mNotification.showSuccess("User registered");
		} else {
			mNotification.showError("Failed to register user");
		}
	}

	private void handleAccountChangeResponse(AccountChangeResponse response) {
		switch (response.status) {
		case SUCCESS:
		case SUCCESS_PARTIAL:
			for (AccountChangeStatuses changeStatus : response.changeStatuses) {
				if (changeStatus == AccountChangeStatuses.PASSWORD_SUCCESS) {
					mPrivateKey = response.privateKey;
					mNotification.showSuccess("Password changed successfully!");
					mEventDispatcher.fire(new GameEvent(EventTypes.USER_PASSWORD_CHANGED));
				} else if (changeStatus == AccountChangeStatuses.PASSWORD_NEW_TOO_SHORT) {
					mEventDispatcher.fire(new GameEvent(EventTypes.USER_PASSWORD_CHANGE_TOO_SHORT));
				} else if (changeStatus == AccountChangeStatuses.PASSWORD_OLD_MISMATCH) {
					mEventDispatcher.fire(new GameEvent(EventTypes.USER_PASSWORD_CHANGE_MISMATCH));
				}
			}
			break;

		case FAILED_SERVER_ERROR:
		case FAILED_SERVER_CONNECTION:
			mNotification.showError("Failed to connect to the server");
			break;

		case FAILED_USER_NOT_LOGGED_IN:
			mNotification.showError("User has been logged out on server");
			break;

		}
	}

	private void handleLoginSuccess(LoginResponse response) {
		// Was already logged in -> Only connected
		if (User.this == mGlobalUser && mLoggedIn) {
			// Update required
			if (response.versionInfo.status == Statuses.UPDATE_REQUIRED) {
				mNotification.showHighlight("Update required to go online");
			}
			// Server restored since last login
			else if (response.restoreDate != null) {
				// Does nothing
			}
			// Connect
			else {
				connectGlobalUser();
			}
		}
		// Logged in
		else if (User.this != mGlobalUser) {
			mUsername = response.username;
			mPrivateKey = response.privateKey;
			mServerKey = response.userKey;
			mEmail = response.email;

			// Login online
			if (response.isServerLoginAvailable()) {
				loginGlobalUser(User.this, true);
			}
			// Login offline
			else {
				loginGlobalUser(User.this, false);
			}
		}
	}

	private void handleLoginFail(LoginResponse response) {
		switch (response.status) {
		case FAILED_SERVER_CONNECTION:
		case FAILED_SERVER_ERROR:
			// Login offline
			if (User.this != mGlobalUser && mPrivateKey != null) {
				loginGlobalUser(User.this, false);
			} else {
				mNotification.showError("Could not connect to server");
				mEventDispatcher.fire(new GameEvent(EventTypes.USER_LOGIN_FAILED));
			}
			break;

		case FAILED_USERNAME_PASSWORD_MISMATCH:
			// Auto-login
			if (mPrivateKey != null) {
				mNotification.showError("Could not auto-login " + mUsername + ". Password has been changed");
			} else {
				mNotification.showError("No username with that password exists");
			}
			mEventDispatcher.fire(new GameEvent(EventTypes.USER_LOGIN_FAILED));
			break;

		case FAILED_SERVER_MAINTENANCE:
			// Login offline
			if (User.this != mGlobalUser && mPrivateKey != null) {
				mNotification.showHighlight("Logged in offline due to server maintenance");
				loginGlobalUser(User.this, false);
			} else {
				mNotification.showHighlight("Could not login due to server maintenance");
				mEventDispatcher.fire(new GameEvent(EventTypes.USER_LOGIN_FAILED));
			}

			break;

		case SUCCESS:
			// Does nothing
			break;
		}
	}

	private void handleLoginExtra(LoginResponse response) {
		// MOTD
		if (!response.motds.isEmpty()) {
			mEventDispatcher.fire(new MotdEvent(EventTypes.MOTD_CURRENT, response.motds));
		}


		// New version
		if (!response.versionInfo.newVersions.isEmpty()) {
			EventTypes updateType = null;
			switch (response.versionInfo.status) {
			case UPDATE_REQUIRED:
				updateType = EventTypes.UPDATE_REQUIRED;
				break;

			case NEW_VERSION_AVAILABLE:
				updateType = EventTypes.UPDATE_AVAILABLE;
				break;

			default:
				break;
			}

			String downloadLocation = null;
			switch (Gdx.app.getType()) {
			case Android:
				// TODO download location for android
				break;
			case Desktop:
				downloadLocation = response.versionInfo.downloadLocationDesktop;
				break;

			default:
				break;
			}

			mEventDispatcher.fire(new UpdateEvent(updateType, response.versionInfo.newVersions, downloadLocation));
		}


		// Server reverted its DB
		if (response.restoreDate != null) {
			mEventDispatcher.fire(new ServerRestoreEvent(response.restoreDate.from, response.restoreDate.to));
		}
	}
};

/**
 * @return global instance of this user, logged in or logged out.
 * @note Users can still be created, this is not a singleton class
 */
public static User getGlobalUser() {
	return mGlobalUser;
}

/**
 * Sets all necessary variables when logging in
 * @param user the user to login
 * @param online true if the user is online
 */
private static void loginGlobalUser(User user, boolean online) {
	mGlobalUser.mUsername = user.getUsername();
	mGlobalUser.mPrivateKey = user.getPrivateKey();
	mGlobalUser.mServerKey = user.getServerKey();
	mGlobalUser.mEmail = user.getEmail();
	mGlobalUser.mOnline = online;
	mGlobalUser.mLoggedIn = true;

	// Update user path
	Config.File.setUserPaths(mGlobalUser.mUsername);

	mEventDispatcher.fire(new GameEvent(EventTypes.USER_LOGIN));

	if (online) {
		mEventDispatcher.fire(new GameEvent(EventTypes.USER_CONNECTED));
		mEventDispatcher.connect(EventTypes.SERVER_MAINTENANCE, mGlobalUser.mDisconnectListener);
		mNotification.showSuccess(user.getUsername() + " is now online!");
	} else {
		mNotification.showHighlight(user.getUsername() + " is offline!");
	}
}

/**
 * @return current username
 */
public String getUsername() {
	return mUsername;
}

/**
 * Sets the username. Does not work for the global user.
 * @param username set the name of the user
 */
public void setUsername(String username) {
	if (this != mGlobalUser) {
		mUsername = username;
	}
}

/**
 * @return private login key
 */
public UUID getPrivateKey() {
	return mPrivateKey;
}

/**
 * @return user key on the server, null if unknown
 */
public String getServerKey() {
	return mServerKey;
}

/**
 * Sets the user id on the server. Does not work for the global user.
 * @param serverKey user id of the server
 */
public void setServerKey(String serverKey) {
	if (this != mGlobalUser) {
		mServerKey = serverKey;
	}
}

/**
 * @return the Email
 */
public String getEmail() {
	return mEmail;
}

/**
 * Does not work for the global user.
 * @param email the Email to set
 */
public void setEmail(String email) {
	if (this != mGlobalUser) {
		mEmail = email;
	}
}

/**
 * Does not work for the global user.
 * @param privateKey private login key to set
 */
public void setPrivateKey(UUID privateKey) {
	if (this != mGlobalUser) {
		mPrivateKey = privateKey;
	}
}

/**
 * Global user reconnected
 */
private static void connectGlobalUser() {
	if (mGlobalUser.mLoggedIn && !mGlobalUser.mOnline) {
		mGlobalUser.mOnline = true;

		mEventDispatcher.fire(new GameEvent(EventTypes.USER_CONNECTED));
		mEventDispatcher.connect(EventTypes.SERVER_MAINTENANCE, mGlobalUser.mDisconnectListener);
		mNotification.showSuccess(mGlobalUser.mUsername + " is now online!");
	}
}

/**
 * Tries to change the password of the user. Can fire game events: USER_PASSWORD_CHANGE_MISMATCH,
 * USER_PASSWORD_CHANGE_TOO_SHORT, USER_PASSWORD_CHANGED
 * @param oldPassword
 * @param newPassword
 */
public void changePassword(String oldPassword, String newPassword) {
	if (this == mGlobalUser) {
		if (isOnline()) {
			mUserRepo.changePassword(oldPassword, newPassword, mResponseListener);
		} else {
			mNotification.showHighlight("Cannot change password while offline");
		}
	}
}

/**
 * @return true if user is online, false if in offline mode
 */
public boolean isOnline() {
	return mOnline;
}

/**
 * Sets the user to online/offline. Does not work for the global user
 * @param online true if online mode, false if offline mode
 */
public void setOnline(boolean online) {
	if (this != mGlobalUser) {
		mOnline = online;
	}
}

/**
 * Logs out the user, only works for the global user
 */
public void logout() {
	// Update user path
	if (this == mGlobalUser) {
		UserRepo.getInstance().logout(false);

		mEmail = null;
		mOnline = false;
		mPassword = null;
		mPrivateKey = null;
		mServerKey = null;
		mLoggedIn = false;
		mUsername = "(None)";

		Config.File.setUserPaths(mUsername);
		AnalyticsRepo analyticsRepo = AnalyticsRepo.getInstance();
		analyticsRepo.endSession();
		analyticsRepo.newSession();
		SceneSwitcher.clearScenes();

		mEventDispatcher.fire(new GameEvent(EventTypes.USER_LOGOUT));
		mEventDispatcher.fire(new GameEvent(EventTypes.USER_DISCONNECTED));

		SceneSwitcher.switchTo(new LoginScene());
	}
}

/**
 * Disconnects the user
 */
public void disconnect() {
	if (this == mGlobalUser) {
		mOnline = false;
		mAskToGoOnline = true;
		mEventDispatcher.fire(new GameEvent(EventTypes.USER_DISCONNECTED));
	}
}

/**
 * Set a user from another user. Does not work for global user
 * @param from the other user to set from
 */
public void set(User from) {
	if (this != mGlobalUser) {
		mEmail = from.mEmail;
		mOnline = from.mOnline;
		mPassword = from.mPassword;
		mPrivateKey = from.mPrivateKey;
		mServerKey = from.mServerKey;
		mLoggedIn = from.mLoggedIn;
		mUsername = from.mUsername;
	}
}

/**
 * Tries to login this user, either offline or online. If the login is successful this will
 * automatically set the global user. If the user is already offline it will try to connect to the
 * server again.
 */
public void login() {
	login(null);
}

/**
 * Tries to login this user, either offline or online. If the login is successful this will
 * automatically set the global user. If the user is already offline it will try to connect to the
 * server again.
 * @param responseListener listens to the web response
 */
public void login(IResponseListener responseListener) {
	IResponseListener[] listeners = null;
	if (responseListener != null) {
		listeners = new IResponseListener[2];
		listeners[1] = responseListener;
	} else {
		listeners = new IResponseListener[1];
	}
	listeners[0] = mResponseListener;


	// Login || Connect if offline
	if (mGlobalUser != this || (mGlobalUser == this && mLoggedIn && !mOnline)) {
		mUserRepo.login(this, listeners);
	}
	// Error
	else if (mGlobalUser == this) {
		Gdx.app.error("User", "this user is the global user and it's already logged in and online");
	}
}

/**
 * Tries to register this user
 * @param responseListener listens to the web response
 */
public void register(IResponseListener responseListener) {
	if (mGlobalUser != this) {
		mUserRepo.register(this, mResponseListener, responseListener);
	}
}

/**
 * @return true if the user is logged in
 */
public boolean isLoggedIn() {
	return mLoggedIn;
}

/**
 * @return the Password
 */
public String getPassword() {
	return mPassword;
}

/**
 * Does not work for the global user.
 * @param password the Password to set
 */
public void setPassword(String password) {
	if (this != mGlobalUser) {
		mPassword = password;
	}
}

/**
 * @return true if we should ask the player to go online again
 */
public boolean isAskToGoOnline() {
	return mAskToGoOnline;
}

/**
 * Sets if we should ask to go online this session
 * @param ask true if we should ask, false to not ask
 */
public void setAskToGoOnline(boolean ask) {
	mAskToGoOnline = ask;
}

/**
 * @return the register (beta) key
 */
public String getRegisterKey() {
	return mRegisterKey;
}

/**
 * Set register (beta) key
 * @param registerKey
 */
public void setRegisterKey(String registerKey) {
	mRegisterKey = registerKey;
}
}
