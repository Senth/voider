package com.spiddekauga.voider.config;

import com.spiddekauga.utils.IniClass;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;

import org.ini4j.Ini;

/**
 * Config file for the ini. Inner classes start with IC (short for ini class). But they use this
 * naming schema mostly so they remain invisible for outside classes, or don't conflict with their
 * names.
 */
public class ConfigIni {
private static InternalNames mFileName = InternalNames.INI_CONFIG;
private static ConfigIni mInstance = null;
/** Editor options */
public IC_Editor editor = null;
/** Game options */
public IC_Game game = null;
/** Sound and music options */
public IC_Sound sound = null;
/** Default user settings */
public IC_Setting setting = null;
/** Menu configuration */
public IC_Menu menu = null;

/**
 * Private constructor to enforce singleton pattern
 */
private ConfigIni() {
	reload();
}

/**
 * Reloads the config file
 */
public void reload() {
	if (ResourceCacheFacade.isLoaded(mFileName)) {
		ResourceCacheFacade.reload(mFileName);
	} else {
		ResourceCacheFacade.load(null, mFileName);
		ResourceCacheFacade.finishLoading();
	}

	// Set variables
	Ini ini = ResourceCacheFacade.get(mFileName);
	IniClass.setJavaClassPrefix("IC_");
	editor = new IC_Editor(ini, ini.get("Editor"));
	game = new IC_Game(ini, ini.get("Game"));
	sound = new IC_Sound(ini, ini.get("Sound"));
	setting = new IC_Setting(ini, ini.get("Setting"));
	menu = new IC_Menu(ini, ini.get("Menu"));
}

/**
 * @return instance of this class
 */
public static ConfigIni getInstance() {
	if (mInstance == null) {
		mInstance = new ConfigIni();
	}
	return mInstance;
}
}
