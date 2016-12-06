package com.spiddekauga.voider.settings;

import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Setting.IC_Network;
import com.spiddekauga.voider.repo.PrefsGateway;

import java.util.Date;

/**
 * User settings
 */
class SettingUserPrefsGateway extends PrefsGateway {

private static final String MOTD__LATEST_DATE = "motd_latestDate";
private static final String CLIENT__LAST_USED_VERSION = "client_lastUsedVersion";
private static final String DATE__DATE_TIME_FORMAT = "date_dateTimeFormat";
private static final String TERMS__LENGTH = "terms_length";
private static final String NETWORK__SEND_BUG_REPORT_ANONYMOUSLY = "network_sendBugReportAnonymously";

/**
 * Default constructor
 */
SettingUserPrefsGateway() {
	super(true);
}

@Override
protected PreferenceNames getPreferenceName() {
	return PreferenceNames.SETTING_USER;
}

/**
 * @return DateTime format the user uses.
 */
String getDateTime() {
	if (mPreferences != null) {
		return mPreferences.getString(DATE__DATE_TIME_FORMAT, ConfigIni.getInstance().setting.general.getDateTimeFormatDefault());
	} else {
		return ConfigIni.getInstance().setting.general.getDateTimeFormatDefault();
	}
}

/**
 * Sets DateTime format that the user uses
 * @param dateTime format of the date time
 */
void setDateTime(String dateTime) {
	mPreferences.putString(DATE__DATE_TIME_FORMAT, dateTime);
	mPreferences.flush();
}

/**
 * Updates the client gameVersion to the latest client gameVersion
 * @param version last used gameVersion string
 */
void updateLastUsedVersion(String version) {
	mPreferences.putString(CLIENT__LAST_USED_VERSION, version);
	mPreferences.flush();
}

// ----------------
// Network
// ----------------

/**
 * @return the last client gameVersion this client used, null if not found
 */
String getLastUsedVersion() {
	return mPreferences.getString(CLIENT__LAST_USED_VERSION, null);
}

/**
 * @return get latest message of the day date
 */
Date getLatestMotdDate() {
	long dateTime = mPreferences.getLong(MOTD__LATEST_DATE, 0);
	return new Date(dateTime);
}

/**
 * Update the latest message of the day date
 * @param date latest date
 */
void setLatestMotdDate(Date date) {
	mPreferences.putLong(MOTD__LATEST_DATE, date.getTime() + 1);
	mPreferences.flush();
}

/**
 * @return length of the terms file
 */
long getTermsLength() {
	return mPreferences.getLong(TERMS__LENGTH, 0);
}

/**
 * Set the length of the current terms document
 */
void setTermsLength(long length) {
	mPreferences.putLong(TERMS__LENGTH, length);
	mPreferences.flush();
}

/**
 * @return true if bug reports should be sent anonymously by default
 */
boolean isBugReportSentAnonymously() {
	IC_Network icNetwork = ConfigIni.getInstance().setting.network;
	if (mPreferences != null) {
		return mPreferences.getBoolean(NETWORK__SEND_BUG_REPORT_ANONYMOUSLY, icNetwork.isSendBugReportAnonymouslyByDefault());
	} else {
		return false;
	}
}

/**
 * Set if bug reports should be sent anonymously by default
 * @param anonymously true if they should be sent anonymously
 */
void setBugReportSendAnonymously(boolean anonymously) {
	if (mPreferences != null) {
		mPreferences.putBoolean(NETWORK__SEND_BUG_REPORT_ANONYMOUSLY, anonymously);
		mPreferences.flush();
	}
}
}
