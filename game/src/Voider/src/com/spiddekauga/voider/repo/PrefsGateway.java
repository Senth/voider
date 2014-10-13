package com.spiddekauga.voider.repo;

import java.util.Observable;
import java.util.Observer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.GameEvent;
import com.spiddekauga.voider.utils.User;

/**
 * Base class for preference gateways.
 * @note This should only be the base class for preference gateways that store user
 *       specific information. I.e. <strong>don't use this class for global
 *       preferences.</strong>
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class PrefsGateway implements Observer {
	/**
	 * Opens a new empty (invalid) preferences gateway.
	 */
	protected PrefsGateway() {
		// Observe/listen to when the user logs in and out to open the
		// appropriate file
		User user = User.getGlobalUser();
		user.addObserver(this);

		if (user.isLoggedIn()) {
			open();
		}
	}

	@Override
	public void update(Observable object, Object arg) {
		if (object instanceof User) {
			if (arg instanceof GameEvent) {
				switch (((GameEvent) arg).type) {
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
