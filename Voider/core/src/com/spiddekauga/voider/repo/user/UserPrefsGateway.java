package com.spiddekauga.voider.repo.user;

import com.spiddekauga.voider.repo.PrefsGateway;

import java.util.UUID;

/**
 * User specific preferences
 */
class UserPrefsGateway extends PrefsGateway {
private static final String ANALYTICS_ID = "analytics_id";

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
}
