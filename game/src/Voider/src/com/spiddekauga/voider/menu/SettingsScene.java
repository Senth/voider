package com.spiddekauga.voider.menu;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.Resolution;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.scene.Scene;

/**
 * Scene for game settings
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SettingsScene extends Scene {
	/**
	 * Creates a settings scene
	 */
	public SettingsScene() {
		super(new SettingsGui());

		((SettingsGui) mGui).setScene(this);
	}

	@Override
	protected boolean onKeyDown(int keycode) {
		if (KeyHelper.isBackPressed(keycode)) {
			setOutcome(Outcomes.NOT_APPLICAPLE);
		}

		return super.onKeyDown(keycode);
	}

	/**
	 * Set master sound
	 * @param volume
	 */
	void setMasterVolume(float volume) {
		mSettingRepo.sound().setMasterVolume(volume / 100f);
	}

	/**
	 * @return get master volume
	 */
	float getMasterVolume() {
		return mSettingRepo.sound().getMasterVolume() * 100;
	}

	/**
	 * Set music volume
	 * @param volume
	 */
	void setMusicVolume(float volume) {
		mSettingRepo.sound().setMusicVolume(volume / 100f);
	}

	/**
	 * @return get music volume
	 */
	float getMusicVolume() {
		return mSettingRepo.sound().getMusicVolume() * 100;
	}

	/**
	 * Set game effects volume
	 * @param volume
	 */
	void setGameVolume(float volume) {
		mSettingRepo.sound().setEffectsVolume(volume / 100f);
	}

	/**
	 * @return game volume
	 */
	float getGameVolume() {
		return mSettingRepo.sound().getEffectsVolume() * 100;
	}

	/**
	 * Set UI volume
	 * @param volume
	 */
	void setUiVolume(float volume) {
		mSettingRepo.sound().setUiVolume(volume / 100f);
	}

	/**
	 * @return UI / Button volume
	 */
	float getUiVolume() {
		return mSettingRepo.sound().getUiVolume() * 100;
	}

	/**
	 * Set 24 hours format
	 * @param time24h true if the time should be set to 24 hours format
	 */
	void set24HourFormat(boolean time24h) {
		mSettingRepo.date().set24h(time24h);
	}

	/**
	 * @return true if time is shown in 24 hours format
	 */
	boolean is24HourFormat() {
		return mSettingRepo.date().is24h();
	}

	/**
	 * Set date format
	 * @param dateFormat the date format
	 */
	void setDateFormat(String dateFormat) {
		mSettingRepo.date().setDateFormat(dateFormat);
	}

	/**
	 * @return current date format
	 */
	String getDateFormat() {
		return mSettingRepo.date().getDateFormat();
	}

	/**
	 * Sets if the screen should be in fullscreen
	 * @param fullscreen
	 */
	void setFullscreen(boolean fullscreen) {
		mSettingRepo.display().setFullscreen(fullscreen);
	}

	/**
	 * @return true if fullscreen
	 */
	boolean isFullscreen() {
		return mSettingRepo.display().isFullscreen();
	}

	/**
	 * Sets the fullscreen resolution of the game
	 * @param resolution
	 */
	void setResolutionFullscreen(Resolution resolution) {
		mSettingRepo.display().setResolutionFullscreen(resolution);
	}

	/**
	 * @return fullscreen resolution of the game
	 */
	Resolution getResolutionFullscreen() {
		return mSettingRepo.display().getResolutionFullscreen();
	}

	/**
	 * Sets the windowed resolution of the game
	 * @param resolution
	 */
	void setResolutionWindowed(Resolution resolution) {
		mSettingRepo.display().setResolutionWindowed(resolution);
	}

	/**
	 * @return windowed resolution of the game
	 */
	Resolution getResolutionWindowed() {
		return mSettingRepo.display().getResolutionWindowed();
	}

	private SettingRepo mSettingRepo = SettingRepo.getInstance();
}
