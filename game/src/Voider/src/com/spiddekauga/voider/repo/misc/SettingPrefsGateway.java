package com.spiddekauga.voider.repo.misc;

import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Setting.IC_Sound;
import com.spiddekauga.voider.repo.PrefsGateway;

/**
 * @author Matteus Magnusson <matteus.magnusso@spiddekauga.com>
 */
class SettingPrefsGateway extends PrefsGateway {
	/**
	 * Private constructor to enforce singleton usage
	 */
	SettingPrefsGateway() {
		// Does nothing
	}

	@Override
	protected String getPreferenceName() {
		return "settings";
	}

	/**
	 * Set the master volume
	 * @param volume in range [0,1]
	 */
	void setMasterVolume(float volume) {
		mPreferences.putFloat(SOUND__MASTER_VOLUME, volume);
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
	}

	/**
	 * @return UI effects volume in range [0,1]
	 */
	float getUiVolume() {
		IC_Sound icSound = ConfigIni.getInstance().setting.sound;
		return mPreferences.getFloat(SOUND__UI_VOLUME, icSound.getUi());
	}

	private static final String SOUND__MASTER_VOLUME = "sound_masterVolume";
	private static final String SOUND__EFFECTS_VOLUME = "sound_effectsVolume";
	private static final String SOUND__UI_VOLUME = "sound_uiVolume";
	private static final String SOUND__MUSIC_VOLUME = "sound_musicVolume";

}
