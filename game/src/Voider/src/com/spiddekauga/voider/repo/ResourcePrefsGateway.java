package com.spiddekauga.voider.repo;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.User.UserEvents;

/**
 * Gateway for reading and writing to the resource preferences file
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class ResourcePrefsGateway implements Observer {
	/**
	 * Opens a new empty (invalid) preferences gateway.
	 */
	ResourcePrefsGateway() {
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
			if (arg instanceof UserEvents) {
				switch ((UserEvents) arg) {
				case LOGIN:
					open();
					break;

				case LOGOUT:
					close();
					break;
				}
			}
		}
	}

	/**
	 * Open the preference file
	 */
	private void open() {
		mPreferences = Gdx.app.getPreferences(Config.File.getUserPreferencesPrefix() + PREFERENCES_NAME);
	}

	/**
	 * Close the preference file
	 */
	private void close() {
		mPreferences = null;
	}

	/**
	 * Set last sync date of published/downloaded resources
	 * @param lastSync date when synced published/downloaded resources the last time
	 */
	void setSyncDownloadDate(Date lastSync) {
		mPreferences.putLong(SYNC_DOWNLOADED, lastSync.getTime());
		mPreferences.flush();
	}

	/**
	 * @return last sync date of published/downloaded resources
	 */
	Date getSyncDownloadDate() {
		long dateTime = mPreferences.getLong(SYNC_DOWNLOADED, 0);
		return new Date(dateTime);
	}

	/**
	 * Set last sync date of user resource revisions
	 * @param lastSync date when synced user resource revisions
	 */
	void setSyncUserResourceDate(Date lastSync) {
		mPreferences.putLong(SYNC_USER_RESOURCES, lastSync.getTime());
		mPreferences.flush();
	}

	/**
	 * @return last sync date of user resource revisions
	 */
	Date getSyncUserResourceDate() {
		long dateTime = mPreferences.getLong(SYNC_USER_RESOURCES, 0);
		return new Date(dateTime);
	}

	/** Preferences */
	private Preferences mPreferences;
	/** Preferences name */
	private static final String PREFERENCES_NAME = "resources";

	// Names
	// SYNC
	/** Downloaded sync date */
	private static final String SYNC_DOWNLOADED = "sync_downloaded";
	/** User resource revision sync date */
	private static final String SYNC_USER_RESOURCES = "sync_user_resources";
}
