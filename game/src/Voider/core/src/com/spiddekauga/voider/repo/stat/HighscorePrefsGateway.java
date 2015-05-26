package com.spiddekauga.voider.repo.stat;

import java.util.Date;

import com.spiddekauga.voider.repo.PrefsGateway;

/**
 * Preferences gateway for highscores
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class HighscorePrefsGateway extends PrefsGateway {
	HighscorePrefsGateway() {
		super(true);
	}

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
	protected PreferenceNames getPreferenceName() {
		return PreferenceNames.HIGHSCORE;
	}

	// Names
	// SYNC
	/** Last highscore sync date */
	private static final String SYNC_DATE = "sync_date";
}
