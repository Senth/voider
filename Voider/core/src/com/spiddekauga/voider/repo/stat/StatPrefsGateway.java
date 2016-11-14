package com.spiddekauga.voider.repo.stat;

import com.spiddekauga.voider.repo.PrefsGateway;

import java.util.Date;

/**
 * Preferences gateway for statistics
 */
class StatPrefsGateway extends PrefsGateway {
/** Last sync date */
private static final String SYNC_DATE = "sync_date";

/**
 * Default constructor
 */
StatPrefsGateway() {
	super(true);
}

/**
 * @return last sync date of highscores
 */
Date getSyncDate() {
	long dateTime = mPreferences.getLong(SYNC_DATE, 0);
	return new Date(dateTime);
}

/**
 * Set last sync date of highscores
 * @param lastSync date when synced highscores the last time
 */
void setSyncDate(Date lastSync) {
	mPreferences.putLong(SYNC_DATE, lastSync.getTime());
	mPreferences.flush();
}

// Names
// SYNC

@Override
protected PreferenceNames getPreferenceName() {
	return PreferenceNames.STATS;
}
}
