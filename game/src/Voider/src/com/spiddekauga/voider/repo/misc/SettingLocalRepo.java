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


	/**
	 * Local repository for the sound settings
	 */
	public class SoundSettingLocalRepo {
		/**
		 * Hidden constructor
		 */
		private SoundSettingLocalRepo() {
			// Does nothing
		}

		/**
		 * Set the master volume
		 * @param volume in range [0,1]
		 */
		public void setMasterVolume(float volume) {
			mPrefsGateway.setMasterVolume(volume);
		}

		/**
		 * @return master volume in range [0,1]
		 */
		public float getMasterVolume() {
			return mPrefsGateway.getMasterVolume();
		}

		/**
		 * Set the sound effects volume
		 * @param volume in range [0,1]
		 */
		public void setEffectsVolume(float volume) {
			mPrefsGateway.setEffectsVolume(volume);
		}

		/**
		 * @return sound effects volume in range [0,1]
		 */
		public float getEffectsVolume() {
			return mPrefsGateway.getEffectsVolume();
		}

		/**
		 * Get the real sound effects out volume.
		 * @return sound effects volume multiplied with the master volume
		 */
		public float getEffectsVolumeOut() {
			return getEffectsVolume() * getMasterVolume();
		}

		/**
		 * Set the music volume
		 * @param volume in range [0,1]
		 */
		public void setMusicVolume(float volume) {
			mPrefsGateway.setMusicVolume(volume);
		}

		/**
		 * @return music volume in range [0,1]
		 */
		public float getMusicVolume() {
			return mPrefsGateway.getMusicVolume();
		}

		/**
		 * Get the real music out volume
		 * @return music volume multiplied with the master volume
		 */
		public float getMusicVolumeOut() {
			return getMusicVolume() * getMasterVolume();
		}

		/**
		 * Set the UI effects volume
		 * @param volume in range [0,1]
		 */
		public void setUiVolume(float volume) {
			mPrefsGateway.setUiVolume(volume);
		}

		/**
		 * @return UI effects volume in range [0,1]
		 */
		public float getUiVolume() {
			return mPrefsGateway.getUiVolume();
		}

		/**
		 * Get the real UI out volume
		 * @return UI volume multiplied with the master volume
		 */
		public float getUiVolumeOut() {
			return getUiVolume() * getMasterVolume();
		}
	}

	/** Sound setting repository */
	public SoundSettingLocalRepo sound = new SoundSettingLocalRepo();

	private SettingPrefsGateway mPrefsGateway = new SettingPrefsGateway();

	private static SettingLocalRepo mInstance = null;
}
