package com.spiddekauga.voider.repo.misc;

import com.spiddekauga.voider.repo.PrefsGateway;

/**
 * @author Matteus Magnusson <matteus.magnusso@spiddekauga.com>
 */
public class SettingPrefsGateway extends PrefsGateway {
	/**
	 * Private constructor to enforce singleton usage
	 */
	private SettingPrefsGateway() {
		// Does nothing
	}

	/**
	 * @return instance of this class
	 */
	public SettingPrefsGateway getInstance() {
		if (mInstance == null) {
			mInstance = new SettingPrefsGateway();
		}
		return mInstance;
	}

	@Override
	protected String getPreferenceName() {
		return "settings";
	}


	private static SettingPrefsGateway mInstance = null;
}
