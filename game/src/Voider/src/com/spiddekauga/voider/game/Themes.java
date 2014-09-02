package com.spiddekauga.voider.game;

import com.spiddekauga.voider.repo.resource.InternalNames;

/**
 * All the different themes for the game
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum Themes {
	/** Space theme */
	SPACE(InternalNames.THEME_SPACE),
	/** Surface of the red planet */
	RED_PLANET_SURFACE(InternalNames.THEME_RED_PLANET_SURFACE),
	/** Tunnels of the red planet */
	RED_PLANET_TUNNELS(InternalNames.THEME_RED_PLANET_TUNNELS),
	/** Core of the red planet */
	RED_PLANET_CORE(InternalNames.THEME_RED_PLANET_CORE),

	;

	/**
	 * Constructor that sets the skin used with the theme
	 * @param skin the skin used with the theme
	 */
	Themes(InternalNames skin) {
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
	public InternalNames getSkin() {
		return mSkin;
	}

	/** The skin used with the theme */
	InternalNames mSkin;
	/** Human-readable name of the theme */
	String mName;
}
