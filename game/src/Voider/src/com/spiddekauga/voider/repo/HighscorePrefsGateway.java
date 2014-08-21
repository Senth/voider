package com.spiddekauga.voider.repo;

import java.util.Date;

/**
 * Preferences gateway for highscores
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class HighscorePrefsGateway extends PrefsGateway {
	/**
	 * Set last sync date of highscores
	 * @param lastSync date when synced highscores the last time
	 */
	void setSyncDate(Date lastSync) {
		mPreferences.putLong(SYNC_DATE, lastSync.getTime());
		mPreferences.flush();
	}

	/**
	 * @return last sync date of highscores
	 */
	Date getSyncDate() {
		long dateTime = mPreferences.getLong(SYNC_DATE, 0);
		return new Date(dateTime);
	}

	@Override
	protected String getPreferenceName() {
		return PREFERENCES_NAME;
	}

	/** Preferences name */
	private static final String PREFERENCES_NAME = "highscore";

	// Names
	// SYNC
	/** Last highscore sync date */
	private static final String SYNC_DATE = "sync_date";
}
