package com.spiddekauga.voider.repo.user;

import java.util.UUID;

import com.spiddekauga.voider.repo.PrefsGateway;

/**
 * User specific preferences
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class UserPrefsGateway extends PrefsGateway {
	/**
	 * Default constructor
	 */
	UserPrefsGateway() {
		super(true);
	}

	@Override
	protected PreferenceNames getPreferenceName() {
		return PreferenceNames.USER;
	}

	/**
	 * @return get analytics user id for this client
	 */
	UUID getAnalyticsId() {
		String idString = mPreferences.getString(ANALYTICS_ID, "");
		UUID analyticsId = null;

		if (idString.isEmpty()) {
			analyticsId = UUID.randomUUID();
			mPreferences.putString(ANALYTICS_ID, analyticsId.toString());
			mPreferences.flush();
		} else {
			analyticsId = UUID.fromString(idString);
		}

		return analyticsId;
	}

	private static final String ANALYTICS_ID = "analytics_id";
}
