package com.spiddekauga.voider.settings;

import com.spiddekauga.utils.Resolution;
import com.spiddekauga.voider.menu.MenuScene;
import com.spiddekauga.voider.settings.SettingRepo.IconSizes;

/**
 * Scene for game settings
 */
public class SettingsScene extends MenuScene {
private SettingRepo mSettingRepo = SettingRepo.getInstance();

/**
 * Creates a settings scene
 */
public SettingsScene() {
	super(new SettingsGui());

	getGui().setScene(this);
}

@Override
protected SettingsGui getGui() {
	return (SettingsGui) super.getGui();
}

/**
 * @return get master volume
 */
float getMasterVolume() {
	return mSettingRepo.sound().getMasterVolume() * 100;
}

/**
 * Set master sound
 */
void setMasterVolume(float volume) {
	mSettingRepo.sound().setMasterVolume(volume / 100f);
}

/**
 * @return get music volume
 */
float getMusicVolume() {
	return mSettingRepo.sound().getMusicVolume() * 100;
}

/**
 * Set music volume
 */
void setMusicVolume(float volume) {
	mSettingRepo.sound().setMusicVolume(volume / 100f);
}

/**
 * @return game volume
 */
float getGameVolume() {
	return mSettingRepo.sound().getEffectsVolume() * 100;
}

/**
 * Set game effects volume
 */
void setGameVolume(float volume) {
	mSettingRepo.sound().setEffectsVolume(volume / 100f);
}

/**
 * @return UI / Button volume
 */
float getUiVolume() {
	return mSettingRepo.sound().getUiVolume() * 100;
}

/**
 * Set UI volume
 */
void setUiVolume(float volume) {
	mSettingRepo.sound().setUiVolume(volume / 100f);
}

/**
 * @return true if time is shown in 24 hours format
 */
boolean is24HourFormat() {
	return mSettingRepo.date().is24h();
}

/**
 * Set 24 hours format
 * @param time24h true if the time should be set to 24 hours format
 */
void set24HourFormat(boolean time24h) {
	mSettingRepo.date().set24h(time24h);
}

/**
 * @return current date format
 */
String getDateFormat() {
	return mSettingRepo.date().getFormat(SettingRepo.SettingDateRepo.FormatTypes.DATE);
}

/**
 * Set date format
 * @param dateFormat the date format
 */
void setDateFormat(String dateFormat) {
	mSettingRepo.date().setDateFormat(dateFormat);
}

/**
 * @return true if fullscreen
 */
boolean isFullscreen() {
	return mSettingRepo.display().isFullscreen();
}

/**
 * Sets if the screen should be in fullscreen
 */
void setFullscreen(boolean fullscreen) {
	mSettingRepo.display().setFullscreen(fullscreen);
}

/**
 * @return fullscreen resolution of the game
 */
Resolution getResolutionFullscreen() {
	return mSettingRepo.display().getResolutionFullscreen();
}

/**
 * Sets the fullscreen resolution of the game
 */
void setResolutionFullscreen(Resolution resolution) {
	mSettingRepo.display().setResolutionFullscreen(resolution);
}

/**
 * @return windowed resolution of the game
 */
Resolution getResolutionWindowed() {
	return mSettingRepo.display().getResolutionWindowed();
}

/**
 * Sets the windowed resolution of the game
 */
void setResolutionWindowed(Resolution resolution) {
	mSettingRepo.display().setResolutionWindowed(resolution);
}

/**
 * @return current icon size
 */
IconSizes getIconSize() {
	return mSettingRepo.display().getIconSize();
}

/**
 * Sets the UI/icon size of the game
 */
void setIconSize(IconSizes iconSize) {
	mSettingRepo.display().setIconSize(iconSize);
}

/**
 * @return true if we are allowed to use mobile data connection
 */
boolean isMobileDataAllowed() {
	return mSettingRepo.network().isMobileDataAllowed();
}

/**
 * Sets if we're allowed to use mobile data connections
 * @param allow true if we're allowed to use mobile data
 */
void setMobileDataAllowed(boolean allow) {
	mSettingRepo.network().setMobileDataAllowed(allow);
}

/**
 * Clears the database, files and settings for the current logged in account and then logs out the
 * user
 */
public void clearData() {
	mSettingRepo.debug().clearData();
}
}
