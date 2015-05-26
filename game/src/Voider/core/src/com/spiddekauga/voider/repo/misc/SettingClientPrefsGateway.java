package com.spiddekauga.voider.repo.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.spiddekauga.utils.Resolution;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Setting.IC_Display;
import com.spiddekauga.voider.config.IC_Setting.IC_Sound;
import com.spiddekauga.voider.repo.PrefsGateway;

/**
 * Game settings gateway
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class SettingClientPrefsGateway extends PrefsGateway {
	/**
	 * Initializes the client preferences gateway
	 */
	SettingClientPrefsGateway() {
		super(false);
	}

	// -----------------------
	// Display settings
	// -----------------------
	/**
	 * Sets if game should start in fullscreen mode
	 * @param fullscreen true for fullscreen
	 */
	void setFullscreen(boolean fullscreen) {
		mPreferences.putBoolean(DISPLAY__FULLSCREEN, fullscreen);
		mPreferences.flush();
	}

	/**
	 * @return true if game should start in fullscreen mode
	 */
	boolean isFullscreen() {
		IC_Display icDisplay = ConfigIni.getInstance().setting.display;
		return mPreferences.getBoolean(DISPLAY__FULLSCREEN, icDisplay.isFullscreen());
	}

	/**
	 * Sets the startup resolution of the game
	 * @param resolution
	 */
	void setResolutionWindowed(Resolution resolution) {
		mPreferences.putString(DISPLAY__RESOLUTION_WINDOWED, resolution.toString());
		mPreferences.flush();
	}

	/**
	 * @return startup resolution of the game
	 */
	Resolution getResolutionWindowed() {
		Resolution resolution = null;
		String resolutionString = mPreferences.getString(DISPLAY__RESOLUTION_WINDOWED);
		if (resolutionString != null) {
			try {
				resolution = new Resolution(resolutionString);
			} catch (IllegalArgumentException e) {
				// Does nothing
			}
		}

		// Use default resolution
		if (resolution == null) {
			IC_Display icDisplay = ConfigIni.getInstance().setting.display;
			resolution = new Resolution(icDisplay.getResolutionWidth(), icDisplay.getResolutionHeight());
		}

		return resolution;
	}

	/**
	 * Sets the fullscreen resolution of the game
	 * @param resolution
	 */
	void setResolutionFullscreen(Resolution resolution) {
		mPreferences.putString(DISPLAY__RESOLUTION_FULLSCREEN, resolution.toString());
		mPreferences.flush();
	}

	/**
	 * @return fullscreen resolution of the game
	 */
	Resolution getResolutionFullscreen() {
		Resolution resolution = null;
		String resolutionString = mPreferences.getString(DISPLAY__RESOLUTION_FULLSCREEN);
		if (resolutionString != null) {
			try {
				resolution = new Resolution(resolutionString);
			} catch (IllegalArgumentException e) {
				// Does nothing
			}
		}

		// Get default resolution
		if (resolution == null) {
			DisplayMode displayMode = Gdx.graphics.getDesktopDisplayMode();
			resolution = new Resolution(displayMode);
		}

		return resolution;
	}

	// -----------------------
	// Sound settings
	// -----------------------

	/**
	 * Set the master volume
	 * @param volume in range [0,1]
	 */
	void setMasterVolume(float volume) {
		mPreferences.putFloat(SOUND__MASTER_VOLUME, volume);
		mPreferences.flush();
	}

	/**
	 * @return master volume in range [0,1]
	 */
	float getMasterVolume() {
		IC_Sound icSound = ConfigIni.getInstance().setting.sound;
		return mPreferences.getFloat(SOUND__MASTER_VOLUME, icSound.getMaster());
	}

	/**
	 * Set the sound effects volume
	 * @param volume in range [0,1]
	 */
	void setEffectsVolume(float volume) {
		mPreferences.putFloat(SOUND__EFFECTS_VOLUME, volume);
		mPreferences.flush();
	}

	/**
	 * @return sound effects volume in range [0,1]
	 */
	float getEffectsVolume() {
		IC_Sound icSound = ConfigIni.getInstance().setting.sound;
		return mPreferences.getFloat(SOUND__EFFECTS_VOLUME, icSound.getEffects());
	}

	/**
	 * Set the music volume
	 * @param volume in range [0,1]
	 */
	void setMusicVolume(float volume) {
		mPreferences.putFloat(SOUND__MUSIC_VOLUME, volume);
		mPreferences.flush();
	}

	/**
	 * @return music volume in range [0,1]
	 */
	float getMusicVolume() {
		IC_Sound icSound = ConfigIni.getInstance().setting.sound;
		return mPreferences.getFloat(SOUND__MUSIC_VOLUME, icSound.getMusic());
	}

	/**
	 * Set the UI effects volume
	 * @param volume in range [0,1]
	 */
	void setUiVolume(float volume) {
		mPreferences.putFloat(SOUND__UI_VOLUME, volume);
		mPreferences.flush();
	}

	/**
	 * @return UI effects volume in range [0,1]
	 */
	float getUiVolume() {
		IC_Sound icSound = ConfigIni.getInstance().setting.sound;
		return mPreferences.getFloat(SOUND__UI_VOLUME, icSound.getUi());
	}

	// ----------------
	// Network
	// ----------------
	/**
	 * Sets if we're allowed to use mobile data connections
	 * @param allow true if we're allowed to use mobile data
	 */
	void setNetworkWifiOnly(boolean allow) {
		mPreferences.putBoolean(NETWORK__MOBILE_DATA_ALLOW, allow);
		mPreferences.flush();
	}

	/**
	 * @return true if we are allowed to use mobile data connection
	 */
	boolean isNetworkWifiOnly() {
		return mPreferences.getBoolean(NETWORK__MOBILE_DATA_ALLOW, true);
	}

	@Override
	protected PreferenceNames getPreferenceName() {
		return PreferenceNames.SETTING_GLOBAL;
	}

	private static final String DISPLAY__RESOLUTION_WINDOWED = "display_resolutionWindowed";
	private static final String DISPLAY__RESOLUTION_FULLSCREEN = "display_resolutionFullscreen";
	private static final String DISPLAY__FULLSCREEN = "display_fullscreen";

	private static final String SOUND__MASTER_VOLUME = "sound_masterVolume";
	private static final String SOUND__EFFECTS_VOLUME = "sound_effectsVolume";
	private static final String SOUND__UI_VOLUME = "sound_uiVolume";
	private static final String SOUND__MUSIC_VOLUME = "sound_musicVolume";

	private static final String NETWORK__MOBILE_DATA_ALLOW = "network_mobileDataAllow";
}
