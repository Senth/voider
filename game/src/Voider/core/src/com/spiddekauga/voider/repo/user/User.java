package com.spiddekauga.voider.repo.user;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.scene.ui.NotificationShower;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.menu.LoginScene;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.user.AccountChangeResponse;
import com.spiddekauga.voider.network.user.AccountChangeResponse.AccountChangeStatuses;
import com.spiddekauga.voider.network.user.LoginResponse;
import com.spiddekauga.voider.network.user.LoginResponse.ClientVersionStatuses;
import com.spiddekauga.voider.network.user.RegisterUserResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.analytics.AnalyticsRepo;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.MotdEvent;
import com.spiddekauga.voider.utils.event.UpdateEvent;

/**
 * User information class
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class User {
	/**
	 * @return global instance of this user, logged in or logged out.
	 * @note Users can still be created, this is not a singleton class
	 */
	public static User getGlobalUser() {
		return mGlobalUser;
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
	 * Tries to change the password of the user. Can fire game events:
	 * USER_PASSWORD_CHANGE_MISMATCH, USER_PASSWORD_CHANGE_TOO_SHORT,
	 * USER_PASSWORD_CHANGED
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
	 * Logs out the user, only works for the global user
	 */
	public void logout() {
		// Update user path
		if (this == mGlobalUser) {
			if (isOnline()) {
				UserRepo.getInstance().logout();
			}

			mEmail = null;
			mOnline = false;
			mPassword = null;
			mPrivateKey = null;
			mServerKey = null;
			mLoggedIn = false;
			mUsername = "(None)";

			Config.File.setUserPaths(mUsername);
			SceneSwitcher.dispose();
			AnalyticsRepo analyticsRepo = AnalyticsRepo.getInstance();
			analyticsRepo.endSession();
			analyticsRepo.newSession();
			SceneSwitcher.clearScenes();
			SceneSwitcher.switchTo(new LoginScene());

			EventDispatcher.getInstance().fire(new GameEvent(EventTypes.USER_LOGOUT));
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
	 * Sets all necessary variables when logging in
	 * @param username the username
	 * @param privateKey used for auto login
	 * @param serverKey user key on the server, can be null
	 * @param online true if the user is online
	 */
	private static void loginGlobalUser(String username, UUID privateKey, String serverKey, boolean online) {
		mGlobalUser.mUsername = username;
		mGlobalUser.mPrivateKey = privateKey;
		mGlobalUser.mServerKey = serverKey;
		mGlobalUser.mOnline = online;
		mGlobalUser.mLoggedIn = true;

		// Update user path
		Config.File.setUserPaths(mGlobalUser.mUsername);

		mEventDispatcher.fire(new GameEvent(EventTypes.USER_LOGIN));

		if (online) {
			mEventDispatcher.fire(new GameEvent(EventTypes.USER_CONNECTED));
			mNotification.showSuccess(username + " is now online!");
		} else {
			mNotification.showHighlight(username + " is now offline!");
		}
	}

	/**
	 * Global user reconnected
	 */
	private static void connectGlobalUser() {
		if (mGlobalUser.mLoggedIn && !mGlobalUser.mOnline) {
			mGlobalUser.mOnline = true;

			mEventDispatcher.fire(new GameEvent(EventTypes.USER_CONNECTED));
			mNotification.showSuccess(mGlobalUser.mUsername + " is now online!");
		}
	}

	/**
	 * Tries to login this user, either offline or online. If the login is successful this
	 * will automatically set the global user. If the user is already offline it will try
	 * to connect to the server again.
	 */
	public void login() {
		login(null);
	}

	/**
	 * Tries to login this user, either offline or online. If the login is successful this
	 * will automatically set the global user. If the user is already offline it will try
	 * to connect to the server again.
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
	 * Make the user offline
	 */
	public void makeOffline() {
		mOnline = false;
		mAskToGoOnline = true;

		mEventDispatcher.fire(new GameEvent(EventTypes.USER_DISCONNECTED));
	}

	/**
	 * @return true if the user is logged in
	 */
	public boolean isLoggedIn() {
		return mLoggedIn;
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
	 * @return user key on the server, null if unknown
	 */
	public String getServerKey() {
		return mServerKey;
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
	 * @return true if user is online, false if in offline mode
	 */
	public boolean isOnline() {
		return mOnline;
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
	 * @return the Password
	 */
	public String getPassword() {
		return mPassword;
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
	 * @return private login key
	 */
	public UUID getPrivateKey() {
		return mPrivateKey;
	}

	/**
	 * Sets if we should ask to go online this session
	 * @param ask true if we should ask, false to not ask
	 */
	public void setAskToGoOnline(boolean ask) {
		mAskToGoOnline = ask;
	}

	/**
	 * @return true if we should ask the player to go online again
	 */
	public boolean isAskToGoOnline() {
		return mAskToGoOnline;
	}

	/**
	 * Set register (beta) key
	 * @param registerKey
	 */
	public void setRegisterKey(String registerKey) {
		mRegisterKey = registerKey;
	}

	/**
	 * @return the register (beta) key
	 */
	public String getRegisterKey() {
		return mRegisterKey;
	}

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
				// Was already logged in -> Only connected
				if (User.this == mGlobalUser && mLoggedIn) {
					if (response.clientVersionStatus != ClientVersionStatuses.UPDATE_REQUIRED) {
						connectGlobalUser();
					} else {
						mNotification.showHighlight("Update required to go online");
					}
				}
				// Logged in
				else if (User.this != mGlobalUser) {
					mUsername = response.username;
					mPrivateKey = response.privateKey;
					mServerKey = response.userKey;
					// No update is required
					if (response.clientVersionStatus != ClientVersionStatuses.UPDATE_REQUIRED) {
						loginGlobalUser(mUsername, mPrivateKey, mServerKey, true);
					}
					// Update required login offline
					else {
						loginGlobalUser(mUsername, mPrivateKey, mServerKey, false);
					}
				}

				// MOTD
				if (!response.motds.isEmpty()) {
					mEventDispatcher.fire(new MotdEvent(EventTypes.MOTD_CURRENT, response.motds));
				}
			}
			// Send error message
			else {
				switch (response.status) {
				case FAILED_SERVER_CONNECTION:
				case FAILED_SERVER_ERROR:
					// Login offline
					if (User.this != mGlobalUser && mPrivateKey != null) {
						loginGlobalUser(mUsername, mPrivateKey, mServerKey, false);
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

				case SUCCESS:
					// Does nothing
					break;
				}
			}

			switch (response.clientVersionStatus) {
			case NEW_VERSION_AVAILABLE:
				mEventDispatcher.fire(new UpdateEvent(EventTypes.UPDATE_AVAILABLE, response.latestClientVersion, response.changeLogMessage));
				break;

			case UPDATE_REQUIRED:
				mEventDispatcher.fire(new UpdateEvent(EventTypes.UPDATE_REQUIRED, response.latestClientVersion, response.changeLogMessage));
				break;

			case UNKNOWN:
			case UP_TO_DATE:
				break;
			}
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
	};

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
}
