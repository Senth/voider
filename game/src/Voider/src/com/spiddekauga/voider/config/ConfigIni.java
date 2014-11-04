package com.spiddekauga.voider.config;

import org.ini4j.Ini;

import com.spiddekauga.utils.IniClass;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;

/**
 * Config file for the ini. Inner classes start with IC (short for ini class). But they
 * use this naming schema mostly so they remain invisible for outside classes, or don't
 * conflict with their names.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ConfigIni {
	/** Editor options */
	public IC_Editor editor = null;
	/** Game options */
	public IC_Game game = null;

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
			ResourceCacheFacade.load(mFileName);
			ResourceCacheFacade.finishLoading();
		}

		// Set variables
		Ini ini = ResourceCacheFacade.get(mFileName);
		IniClass.setJavaClassPrefix("IC_");
		editor = new IC_Editor(ini, ini.get("Editor"));
		game = new IC_Game(ini, ini.get("Game"));
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


	private static InternalNames mFileName = InternalNames.INI_CONFIG;
	private static ConfigIni mInstance = null;
}
