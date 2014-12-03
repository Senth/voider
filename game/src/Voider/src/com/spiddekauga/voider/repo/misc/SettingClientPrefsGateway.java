package com.spiddekauga.voider.repo.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Preferences;
import com.spiddekauga.utils.Resolution;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Setting.IC_Display;
import com.spiddekauga.voider.config.IC_Setting.IC_Network;
import com.spiddekauga.voider.config.IC_Setting.IC_Sound;

/**
 * Game settings gateway
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class SettingClientPrefsGateway {
	/**
	 * Initializes the client preferences gateway
	 */
	SettingClientPrefsGateway() {
		mPreferences = Gdx.app.getPreferences(PREFERENCES_NAME);
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
	// Network settings
	// -----------------------
	/**
	 * Set if the client should auto-connect when it goes offline
	 * @param autoConnect true if the client should auto connect when it goes offline
	 */
	void setAutoConnect(boolean autoConnect) {
		mPreferences.putBoolean(NETWORK__AUTO_CONNECT, autoConnect);
		mPreferences.flush();
	}

	/**
	 * @return true if the client should auto connect when it goes offline
	 */
	boolean shouldAutoConnect() {
		IC_Network network = ConfigIni.getInstance().setting.network;
		return mPreferences.getBoolean(NETWORK__AUTO_CONNECT, network.isAutoConnectByDefault());
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

	private static final String DISPLAY__RESOLUTION_WINDOWED = "display_resolutionWindowed";
	private static final String DISPLAY__RESOLUTION_FULLSCREEN = "display_resolutionFullscreen";
	private static final String DISPLAY__FULLSCREEN = "display_fullscreen";

	private static final String SOUND__MASTER_VOLUME = "sound_masterVolume";
	private static final String SOUND__EFFECTS_VOLUME = "sound_effectsVolume";
	private static final String SOUND__UI_VOLUME = "sound_uiVolume";
	private static final String SOUND__MUSIC_VOLUME = "sound_musicVolume";

	private static final String NETWORK__AUTO_CONNECT = "network_autoConnect";

	private Preferences mPreferences;
	private static final String PREFERENCES_NAME = Config.File.PREFERENCE_PREFIX + "_setting";
}
