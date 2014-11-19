package com.spiddekauga.voider.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.scene.ui.NotificationShower;
import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.user.LoginMethodResponse;
import com.spiddekauga.voider.network.entities.user.LoginMethodResponse.ClientVersionStatuses;
import com.spiddekauga.voider.network.entities.user.RegisterUserMethodResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.user.UserLocalRepo;
import com.spiddekauga.voider.repo.user.UserWebRepo;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
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
	 * Logs out the user
	 */
	public void logout() {
		mEmail = null;
		mOnline = false;
		mPassword = null;
		mPrivateKey = null;
		mServerKey = null;
		mLoggedIn = false;
		mUsername = "(None)";
		mDateFormat = DATE_FORMAT_DEFAULT;

		// Update user path
		if (this == mGlobalUser) {
			Config.File.setUserPaths(mUsername);
		}


		EventDispatcher.getInstance().fire(new GameEvent(EventTypes.USER_LOGOUT));
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
			mDateFormat = from.mDateFormat;
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
			UserLocalRepo.setLastUser(username, privateKey, serverKey);
			mEventDispatcher.fire(new GameEvent(EventTypes.USER_CONNECTED));
			mNotification.show(NotificationTypes.SUCCESS, username + " is now online!");
		} else {
			mNotification.show(NotificationTypes.HIGHLIGHT, username + " is now offline!");
		}
	}

	/**
	 * Global user reconnected
	 */
	private static void connectGlobalUser() {
		if (mGlobalUser.mLoggedIn && !mGlobalUser.mOnline) {
			mGlobalUser.mOnline = true;

			mEventDispatcher.fire(new GameEvent(EventTypes.USER_CONNECTED));
			mNotification.show(NotificationTypes.SUCCESS, mGlobalUser.mUsername + " is now online!");
		}
	}

	/**
	 * Tries to login this user, either offline or online. If the login is successful this
	 * will automatically set the global user. If the user is already offline it will try
	 * to connect to the server again.
	 */
	public void login() {
		// Connect if offline
		if (mGlobalUser == this && mLoggedIn && !mOnline) {
			mWebRepo.login(this, UserLocalRepo.getClientId(), mResponseListener);
		}
		// Login
		else if (mGlobalUser != this) {
			mWebRepo.login(this, UserLocalRepo.getClientId(), mResponseListener);
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
			mWebRepo.register(this, UserLocalRepo.getClientId(), mResponseListener, responseListener);
		}
	}

	/**
	 * Make the user offline
	 */
	public void makeOffline() {
		mOnline = false;

		mEventDispatcher.fire(new GameEvent(EventTypes.USER_DISCONNECTED));
	}

	/**
	 * @return true if the user is logged in
	 */
	public boolean isLoggedIn() {
		return mLoggedIn;
	}

	/**
	 * Sets the date format for the user
	 * @param dateFormat preferred date format for the user
	 */
	public void setDateFormat(String dateFormat) {
		mDateFormat = new SimpleDateFormat(dateFormat);
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
	 * Converts a date to the user's preferred date format
	 * @param date the date to convert to string
	 * @return date as string in the user's preferred date format
	 */
	public synchronized String dateToString(Date date) {
		return mDateFormat.format(date);
	}

	private IResponseListener mResponseListener = new IResponseListener() {
		@Override
		public void handleWebResponse(IMethodEntity method, IEntity response) {
			// Login
			if (response instanceof LoginMethodResponse) {
				handleLoginResponse((LoginMethodResponse) response);
			}
			// Register
			if (response instanceof RegisterUserMethodResponse) {
				handleRegisterResponse((RegisterUserMethodResponse) response);
			}
		}

		private void handleLoginResponse(LoginMethodResponse response) {
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

			if (response.isSuccessful()) {
				// Was already logged in -> Only connected
				if (User.this == mGlobalUser && mLoggedIn) {
					if (response.clientVersionStatus != ClientVersionStatuses.UPDATE_REQUIRED) {
						connectGlobalUser();
					} else {
						mNotification.show(NotificationTypes.HIGHLIGHT, "Update required to go online");
					}
				}
				// Logged in
				else if (User.this != mGlobalUser) {
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
						mNotification.show(NotificationTypes.ERROR, "Could not connect to server");
						mEventDispatcher.fire(new GameEvent(EventTypes.USER_LOGIN_FAILED));
					}
					break;

				case FAILED_USERNAME_PASSWORD_MISMATCH:
					// Auto-login
					if (mPrivateKey != null) {
						mNotification.show(NotificationTypes.ERROR, "Could not auto-login " + mUsername + ". Password has been changed");
					} else {
						mNotification.show(NotificationTypes.ERROR, "No username with that password exists");
					}
					mEventDispatcher.fire(new GameEvent(EventTypes.USER_LOGIN_FAILED));
					break;

				case SUCCESS:
					// Does nothing
					break;
				}
			}
		}

		private void handleRegisterResponse(RegisterUserMethodResponse response) {
			if (response.isSuccessful()) {
				// Registered and logged in
				if (User.this != mGlobalUser) {
					mPrivateKey = response.privateKey;
					mServerKey = response.userKey;
					UserLocalRepo.setLastUser(mUsername, response.privateKey, response.userKey);
					UserLocalRepo.setAsRegistered();
				}
				mNotification.show(NotificationTypes.SUCCESS, "User registered");
			} else {
				mNotification.show(NotificationTypes.ERROR, "Failed to register user");
			}
		}
	};

	private final static NotificationShower mNotification = NotificationShower.getInstance();
	private final static EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
	private final static UserWebRepo mWebRepo = UserWebRepo.getInstance();
	/** Default datetime format */
	private final static SimpleDateFormat DATE_FORMAT_DEFAULT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	/** Date to string format */
	private SimpleDateFormat mDateFormat = DATE_FORMAT_DEFAULT;
	/** Global user */
	private static User mGlobalUser = new User();
	private String mUsername = "(None)";
	private String mServerKey = null;
	private boolean mOnline = false;
	/** Password, usually not used or stored */
	private String mPassword = null;
	/** Email, usually not used or stored */
	private String mEmail = null;
	/** Private login key */
	private UUID mPrivateKey = null;
	private boolean mLoggedIn = false;
}
