package com.spiddekauga.voider.repo.misc;

import com.spiddekauga.utils.Resolution;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;

/**
 * Local repository for settings (both client settings and user settings)
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class SettingLocalRepo {
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
	 * General Settings
	 */
	class SettingGeneralLocalRepo {
		/**
		 * Hidden constructor
		 */
		private SettingGeneralLocalRepo() {
			// Does nothing
		}

		/**
		 * Sets DateTime format that the user uses
		 * @param dateTime format of the date time
		 */
		void setDateTime(String dateTime) {
			mUserPrefsGateway.setDateTime(dateTime);
		}

		/**
		 * @return DateTime format the user uses.
		 */
		String getDateTime() {
			return mUserPrefsGateway.getDateTime();
		}
	}

	/**
	 * Sound settings
	 */
	class SettingSoundLocalRepo {
		/**
		 * Hidden constructor
		 */
		private SettingSoundLocalRepo() {
			// Does nothing
		}

		/**
		 * Set the master volume
		 * @param volume in range [0,1]
		 */
		void setMasterVolume(float volume) {
			mClientPrefsGateway.setMasterVolume(volume);
			mEventDispatcher.fire(new GameEvent(EventTypes.SOUND_MASTER_VOLUME_CHANGED));
		}

		/**
		 * @return master volume in range [0,1]
		 */
		float getMasterVolume() {
			return mClientPrefsGateway.getMasterVolume();
		}

		/**
		 * Set the sound effects volume
		 * @param volume in range [0,1]
		 */
		void setEffectsVolume(float volume) {
			mClientPrefsGateway.setEffectsVolume(volume);
			mEventDispatcher.fire(new GameEvent(EventTypes.SOUND_EFFECTS_VOLUME_CHANGED));
		}

		/**
		 * @return sound effects volume in range [0,1]
		 */
		float getEffectsVolume() {
			return mClientPrefsGateway.getEffectsVolume();
		}

		/**
		 * Get the real sound effects out volume.
		 * @return sound effects volume multiplied with the master volume
		 */
		float getEffectsVolumeOut() {
			return getEffectsVolume() * getMasterVolume();
		}

		/**
		 * Set the music volume
		 * @param volume in range [0,1]
		 */
		void setMusicVolume(float volume) {
			mClientPrefsGateway.setMusicVolume(volume);
			mEventDispatcher.fire(new GameEvent(EventTypes.SOUND_MUSIC_VOLUME_CHANGED));
		}

		/**
		 * @return music volume in range [0,1]
		 */
		float getMusicVolume() {
			return mClientPrefsGateway.getMusicVolume();
		}

		/**
		 * Get the real music out volume
		 * @return music volume multiplied with the master volume
		 */
		float getMusicVolumeOut() {
			return getMusicVolume() * getMasterVolume();
		}

		/**
		 * Set the UI effects volume
		 * @param volume in range [0,1]
		 */
		void setUiVolume(float volume) {
			mClientPrefsGateway.setUiVolume(volume);
			mEventDispatcher.fire(new GameEvent(EventTypes.SOUND_UI_VOLUME_CHANGED));
		}

		/**
		 * @return UI effects volume in range [0,1]
		 */
		float getUiVolume() {
			return mClientPrefsGateway.getUiVolume();
		}

		/**
		 * Get the real UI out volume
		 * @return UI volume multiplied with the master volume
		 */
		float getUiVolumeOut() {
			return getUiVolume() * getMasterVolume();
		}
	}

	/**
	 * Display settings
	 */
	class SettingDisplayLocalRepo {
		/**
		 * Sets if game should start in fullscreen mode
		 * @param fullscreen true for fullscreen
		 */
		void setFullscreen(boolean fullscreen) {
			mClientPrefsGateway.setFullscreen(fullscreen);
		}

		/**
		 * @return true if game should start in fullscreen mode
		 */
		boolean isFullscreen() {
			return mClientPrefsGateway.isFullscreen();
		}

		/**
		 * Sets the startup resolution of the game
		 * @param resolution
		 */
		void setResolutionWindowed(Resolution resolution) {
			mClientPrefsGateway.setResolutionWindowed(resolution);
		}

		/**
		 * @return startup resolution of the game
		 */
		Resolution getResolutionWindowed() {
			return mClientPrefsGateway.getResolutionWindowed();
		}

		/**
		 * Sets the fullscreen resolution of the game
		 * @param resolution
		 */
		void setResolutionFullscreen(Resolution resolution) {
			mClientPrefsGateway.setResolutionFullscreen(resolution);
		}

		/**
		 * @return fullscreen resolution of the game
		 */
		Resolution getResolutionFullscreen() {
			return mClientPrefsGateway.getResolutionFullscreen();
		}

	}

	/** Display setting repository */
	SettingDisplayLocalRepo display = new SettingDisplayLocalRepo();
	/** Sound setting repository */
	SettingSoundLocalRepo sound = new SettingSoundLocalRepo();
	/** general setting repository */
	SettingGeneralLocalRepo general = new SettingGeneralLocalRepo();

	private EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
	private SettingUserPrefsGateway mUserPrefsGateway = new SettingUserPrefsGateway();
	private SettingClientPrefsGateway mClientPrefsGateway = new SettingClientPrefsGateway();

	private static SettingLocalRepo mInstance = null;
}
