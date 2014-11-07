package com.spiddekauga.voider.repo.misc;

/**
 * Local repository for settings
 * @author Matteus Magnusson <matteus.magnusso@spiddekauga.com>
 */
public class SettingLocalRepo {
	/**
	 * Private constructor to enforce singleton usage
	 */
	private SettingLocalRepo() {
		// Does nothing
	}

	/**
	 * @return instance of this class
	 */
	public SettingLocalRepo getInstance() {
		if (mInstance == null) {
			mInstance = new SettingLocalRepo();
		}
		return mInstance;
	}


	private static SettingLocalRepo mInstance = null;
}
