package com.spiddekauga.voider.repo.resource;

import java.util.Date;

import com.spiddekauga.voider.repo.PrefsGateway;

/**
 * Gateway for reading and writing to the resource preferences file
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class ResourcePrefsGateway extends PrefsGateway {
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

	@Override
	protected String getPreferenceName() {
		return PREFERENCES_NAME;
	}

	/** Preferences name */
	private static final String PREFERENCES_NAME = "resources";

	// Names
	// SYNC
	/** Downloaded sync date */
	private static final String SYNC_DOWNLOADED = "sync_downloaded";
	/** User resource revision sync date */
	private static final String SYNC_USER_RESOURCES = "sync_user_resources";
}
