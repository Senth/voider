package com.spiddekauga.voider.repo.misc;

import com.spiddekauga.voider.repo.PrefsGateway;

/**
 * User settings
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class SettingUserPrefsGateway extends PrefsGateway {

	@Override
	protected String getPreferenceName() {
		return "settings";
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
	 * @return DateTime format the user uses.
	 */
	String getDateTime() {
		return mPreferences.getString(DATE__DATE_TIME_FORMAT, "");
	}

	private static final String DATE__DATE_TIME_FORMAT = "date_dateTimeFormat";
}
