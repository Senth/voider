package com.spiddekauga.voider.repo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Base class for preference gateways.
 * @note This should only be the base class for preference gateways that store user specific
 * information. I.e. <strong>don't use this class for global preferences.</strong>
 */
public abstract class PrefsGateway implements IEventListener {
protected Preferences mPreferences;


/**
 * Opens a new empty (invalid) preferences gateway.
 * @param userPreferences true if user preferences, false for global prefernces
 */
protected PrefsGateway(boolean userPreferences) {
	// Observe/listen to when the user logs in and out to open the
	// appropriate file
	if (userPreferences) {
		User user = User.getGlobalUser();

		if (user != null && user.isLoggedIn()) {
			open();
		}

		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.connect(EventTypes.USER_LOGIN, this);
		eventDispatcher.connect(EventTypes.USER_LOGOUT, this);
	} else {
		open();
	}
}

/**
 * Open the preference file
 */
private void open() {
	mPreferences = Gdx.app.getPreferences(getPreferenceName().toString());
}

/**
 * @return name of preference file
 */
protected abstract PreferenceNames getPreferenceName();

/**
 * Clear all user preferences
 */
public static void clearUserPreferences() {
	for (PreferenceNames preferenceName : PreferenceNames.values()) {
		if (preferenceName.isUserPreferences()) {
			Preferences preferences = Gdx.app.getPreferences(preferenceName.toString());
			preferences.clear();
			preferences.flush();
		}
	}
}

@Override
public void handleEvent(GameEvent event) {
	switch (event.type) {
	case USER_LOGIN:
		open();
		break;

	case USER_LOGOUT:
		close();
		break;

	default:
		break;
	}
}

/**
 * Close the preference file
 */
private void close() {
	mPreferences = null;
}

/**
 * All preferences
 */
@SuppressWarnings("javadoc")
protected enum PreferenceNames {
	RESOURCE("resource", true),
	HIGHSCORE("highscore", true),
	STATS("stats", true),
	SETTING_GLOBAL("setting", false),
	SETTING_USER("setting", true),
	USER("user", true),
	USERS("users", false),;

	private String mName;
	/** True if user preferences, i.e. not global for the app */
	private boolean mUser;

	/**
	 * Constructor for preference names
	 * @param name name of the preference
	 * @param user true if user preferences, false for global
	 */
	private PreferenceNames(String name, boolean user) {
		mUser = user;
		mName = name;
	}

	@Override
	public String toString() {
		if (mUser) {
			return Config.File.getUserPreferencesPrefix() + mName;
		} else {
			return Config.File.PREFERENCE_PREFIX + "." + mName;
		}
	}

	/**
	 * @return true if the user preferences, false if global preferences
	 */
	public boolean isUserPreferences() {
		return mUser;
	}
}
}
