package com.spiddekauga.voider.repo.misc;

/**
 * Local repository for settings (both client settings and user settings)
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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
	public static SettingLocalRepo getInstance() {
		if (mInstance == null) {
			mInstance = new SettingLocalRepo();
		}
		return mInstance;
	}

	/**
	 * Local repository for network settings
	 */
	public class NetworkSettingLocalRepo {
		/**
		 * Hidden constructor
		 */
		private NetworkSettingLocalRepo() {
			// Does nothing
		}

		/**
		 * Set if the client should auto-connect when it goes offline
		 * @param autoConnect true if the client should auto connect when it goes offline
		 */
		public void setAutoConnect(boolean autoConnect) {
			mClientPrefsGateway.setAutoConnect(autoConnect);
		}

		/**
		 * @return true if the client should auto connect when it goes offline
		 */
		public boolean shouldAutoConnect() {
			return mClientPrefsGateway.shouldAutoConnect();
		}
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
			mClientPrefsGateway.setMasterVolume(volume);
		}

		/**
		 * @return master volume in range [0,1]
		 */
		public float getMasterVolume() {
			return mClientPrefsGateway.getMasterVolume();
		}

		/**
		 * Set the sound effects volume
		 * @param volume in range [0,1]
		 */
		public void setEffectsVolume(float volume) {
			mClientPrefsGateway.setEffectsVolume(volume);
		}

		/**
		 * @return sound effects volume in range [0,1]
		 */
		public float getEffectsVolume() {
			return mClientPrefsGateway.getEffectsVolume();
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
			mClientPrefsGateway.setMusicVolume(volume);
		}

		/**
		 * @return music volume in range [0,1]
		 */
		public float getMusicVolume() {
			return mClientPrefsGateway.getMusicVolume();
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
			mClientPrefsGateway.setUiVolume(volume);
		}

		/**
		 * @return UI effects volume in range [0,1]
		 */
		public float getUiVolume() {
			return mClientPrefsGateway.getUiVolume();
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
	/** Network setting repostiory */
	public NetworkSettingLocalRepo network = new NetworkSettingLocalRepo();

	private SettingClientPrefsGateway mClientPrefsGateway = new SettingClientPrefsGateway();

	private static SettingLocalRepo mInstance = null;
}
