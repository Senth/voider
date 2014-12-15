package com.spiddekauga.voider.repo.misc;

import com.spiddekauga.voider.ClientVersions;
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

	/**
	 * Updates the client version to the latest client version
	 */
	void updateClientVersion() {
		mPreferences.putInteger(CLIENT__LAST_VERSION, ClientVersions.getLatest().getId());
		mPreferences.flush();
	}

	/**
	 * @return the last client version this client used
	 */
	ClientVersions getLatestClientVersion() {
		int latestVersion = ClientVersions.getLatest().getId();
		int versionId = mPreferences.getInteger(CLIENT__LAST_VERSION, latestVersion);

		if (versionId > latestVersion || versionId < 0) {
			versionId = latestVersion;
		}

		return ClientVersions.fromId(versionId);
	}

	private static final String CLIENT__LAST_VERSION = "client_lastVersion";
	private static final String DATE__DATE_TIME_FORMAT = "date_dateTimeFormat";
}
