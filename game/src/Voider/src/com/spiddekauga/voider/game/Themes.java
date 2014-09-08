package com.spiddekauga.voider.game;

import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.resources.InternalDeps;

/**
 * All the different themes for the game
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum Themes {
	// !!!NEVER EVER remove or change order of these!!!
	/** Space theme */
	SPACE(InternalDeps.THEME_SPACE),
	/** Surface of the red planet */
	SURFACE(InternalDeps.THEME_SURFACE),
	/** Tunnels of the red planet */
	TUNNELS(InternalDeps.THEME_TUNNEL),
	/** Core of the red planet */
	CORE(InternalDeps.THEME_CORE),

	;

	/**
	 * Constructor that sets the dependency of the theme
	 * @param dependency dependency of the level
	 */
	Themes(InternalDeps dependency) {
		mDependency = dependency;
		createHumanReadableName();
	}

	/**
	 * Creates a human readable name
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
	 * @return top layer of theme
	 */
	public InternalNames getTopLayer() {
		return mDependency.getDependencies()[1];
	}

	/**
	 * @return bottom layer of theme
	 */
	public InternalNames getBottomLayer() {
		return mDependency.getDependencies()[0];
	}

	/**
	 * @return dependency of the theme
	 */
	public InternalDeps getDependency() {
		return mDependency;
	}

	/** Dependency */
	private InternalDeps mDependency;
	/** Human-readable name of the theme */
	private String mName;
}
