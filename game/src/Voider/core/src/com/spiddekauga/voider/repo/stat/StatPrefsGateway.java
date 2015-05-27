package com.spiddekauga.voider.repo.stat;

import java.util.Date;

import com.spiddekauga.voider.repo.PrefsGateway;

/**
 * Preferences gateway for statistics
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class StatPrefsGateway extends PrefsGateway {
	/**
	 * Default constructor
	 */
	StatPrefsGateway() {
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
		return PreferenceNames.STATS;
	}

	// Names
	// SYNC
	/** Last sync date */
	private static final String SYNC_DATE = "sync_date";
}
