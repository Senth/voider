package com.spiddekauga.voider.repo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Base class for preference gateways.
 * @note This should only be the base class for preference gateways that store user
 *       specific information. I.e. <strong>don't use this class for global
 *       preferences.</strong>
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class PrefsGateway implements IEventListener {
	/**
	 * Opens a new empty (invalid) preferences gateway.
	 */
	protected PrefsGateway() {
		// Observe/listen to when the user logs in and out to open the
		// appropriate file
		User user = User.getGlobalUser();

		if (user != null && user.isLoggedIn()) {
			open();
		}

		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.connect(EventTypes.USER_LOGIN, this);
		eventDispatcher.connect(EventTypes.USER_LOGOUT, this);
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
	 * Open the preference file
	 */
	private void open() {
		mPreferences = Gdx.app.getPreferences(Config.File.getUserPreferencesPrefix() + getPreferenceName());
	}

	/**
	 * Close the preference file
	 */
	private void close() {
		mPreferences = null;
	}

	/**
	 * @return name of preference file
	 */
	protected abstract String getPreferenceName();

	/** Preferences */
	protected Preferences mPreferences;
}
