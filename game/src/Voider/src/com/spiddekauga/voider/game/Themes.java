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

		createHumanReadableName();
	}

	/**
	 * Creates a human reabale name
	 */
	private void createHumanReadableName() {
		mName = name().replaceAll("_", " ").toLowerCase();

		// Make first letter upper case
		mName = Character.toUpperCase(mName.charAt(0)) + mName.substring(1);
	}

	/**
	 * @return a human readable name
	 */
	@Override
	public String toString() {
		return mName;
	}

	/**
	 * @return skin used with the theme
	 */
	public ResourceNames getSkin() {
		return mSkin;
	}

	/** The skin used with the theme */
	ResourceNames mSkin;
	/** Human-readable name of the theme */
	String mName;
}
