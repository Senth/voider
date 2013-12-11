package com.spiddekauga.voider.game;

import com.spiddekauga.voider.resources.ResourceNames;

/**
 * All the different themes for the game
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public enum Themes {
	/** Space theme */
	SPACE(ResourceNames.THEME_SPACE),
	/** Surface of the red planet */
	RED_PLANET_SURFACE(ResourceNames.THEME_RED_PLANET_SURFACE),
	/** Tunnels of the red planet */
	RED_PLANET_TUNNELS(ResourceNames.THEME_RED_PLANET_TUNNELS),
	/** Core of the red planet */
	RED_PLANET_CORE(ResourceNames.THEME_RED_PLANET_CORE),

	;

	/**
	 * Constructor that sets the skin used with the theme
	 * @param skin the skin used with the theme
	 */
	Themes(ResourceNames skin) {
		mSkin = skin;
	}

	/**
	 * @return skin used with the theme
	 */
	ResourceNames getSkin() {
		return mSkin;
	}

	/** The skin used with the theme */
	ResourceNames mSkin;
}
