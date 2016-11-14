package com.spiddekauga.voider.repo.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.spiddekauga.utils.Resolution;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Setting.IC_Display;
import com.spiddekauga.voider.config.IC_Setting.IC_Sound;
import com.spiddekauga.voider.repo.PrefsGateway;
import com.spiddekauga.voider.repo.misc.SettingRepo.IconSizes;

/**
 * Game settings gateway
 */
class SettingClientPrefsGateway extends PrefsGateway {
private static final String DISPLAY__RESOLUTION_WINDOWED = "display_resolutionWindowed";

// -----------------------
// Display settings
// -----------------------
private static final String DISPLAY__RESOLUTION_FULLSCREEN = "display_resolutionFullscreen";
private static final String DISPLAY__FULLSCREEN = "display_fullscreen";
private static final String DISPLAY__ICON_SIZE = "display_iconSize";
private static final String SOUND__MASTER_VOLUME = "sound_masterVolume";
private static final String SOUND__EFFECTS_VOLUME = "sound_effectsVolume";
private static final String SOUND__UI_VOLUME = "sound_uiVolume";
private static final String SOUND__MUSIC_VOLUME = "sound_musicVolume";
private static final String NETWORK__MOBILE_DATA_ALLOW = "network_mobileDataAllow";


// -----------------------
// Sound settings
// -----------------------

/**
 * Initializes the client preferences gateway
 */
SettingClientPrefsGateway() {
	super(false);
}

/**
 * @return true if game should start in fullscreen mode
 */
boolean isFullscreen() {
	IC_Display icDisplay = ConfigIni.getInstance().setting.display;
	return mPreferences.getBoolean(DISPLAY__FULLSCREEN, icDisplay.isFullscreen());
}

/**
 * Sets if game should start in fullscreen mode
 * @param fullscreen true for fullscreen
 */
void setFullscreen(boolean fullscreen) {
	mPreferences.putBoolean(DISPLAY__FULLSCREEN, fullscreen);
	mPreferences.flush();
}

/**
 * @return current iconSize
 */
IconSizes getIconSize() {
	IC_Display icDisplay = ConfigIni.getInstance().setting.display;
	String iconSizeString = mPreferences.getString(DISPLAY__ICON_SIZE, icDisplay.getIconSize());
	return IconSizes.fromName(iconSizeString);
}

/**
 * Set icon/UI size
 * @param iconSize set the icon size
 */
void setIconSize(IconSizes iconSize) {
	mPreferences.putString(DISPLAY__ICON_SIZE, iconSize.toString());
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
 * Sets the startup resolution of the game
 * @param resolution
 */
void setResolutionWindowed(Resolution resolution) {
	mPreferences.putString(DISPLAY__RESOLUTION_WINDOWED, resolution.toString());
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

// ----------------
// Network
// ----------------

/**
 * Sets the fullscreen resolution of the game
 * @param resolution
 */
void setResolutionFullscreen(Resolution resolution) {
	mPreferences.putString(DISPLAY__RESOLUTION_FULLSCREEN, resolution.toString());
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
 * Set the master volume
 * @param volume in range [0,1]
 */
void setMasterVolume(float volume) {
	mPreferences.putFloat(SOUND__MASTER_VOLUME, volume);
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
 * Set the sound effects volume
 * @param volume in range [0,1]
 */
void setEffectsVolume(float volume) {
	mPreferences.putFloat(SOUND__EFFECTS_VOLUME, volume);
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
 * Set the music volume
 * @param volume in range [0,1]
 */
void setMusicVolume(float volume) {
	mPreferences.putFloat(SOUND__MUSIC_VOLUME, volume);
	mPreferences.flush();
}

/**
 * @return UI effects volume in range [0,1]
 */
float getUiVolume() {
	IC_Sound icSound = ConfigIni.getInstance().setting.sound;
	return mPreferences.getFloat(SOUND__UI_VOLUME, icSound.getUi());
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
 * @return true if we are allowed to use mobile data connection
 */
boolean isNetworkWifiOnly() {
	return mPreferences.getBoolean(NETWORK__MOBILE_DATA_ALLOW, true);
}

/**
 * Sets if we're allowed to use mobile data connections
 * @param allow true if we're allowed to use mobile data
 */
void setNetworkWifiOnly(boolean allow) {
	mPreferences.putBoolean(NETWORK__MOBILE_DATA_ALLOW, allow);
	mPreferences.flush();
}

@Override
protected PreferenceNames getPreferenceName() {
	return PreferenceNames.SETTING_GLOBAL;
}
}
