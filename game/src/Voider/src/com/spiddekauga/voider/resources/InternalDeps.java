package com.spiddekauga.voider.resources;

import com.spiddekauga.voider.game.Themes;
import com.spiddekauga.voider.repo.resource.InternalNames;

/**
 * Internal dependencies for various resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum InternalDeps {
	// !!!NEVER EVER remove or change order of these!!!
	/** Space theme */
	THEME_SPACE(Themes.SPACE.getBottomLayer(), Themes.SPACE.getTopLayer()),
	/** Core theme */
	THEME_CORE(Themes.CORE.getBottomLayer(), Themes.CORE.getTopLayer()),
	/** Surface theme */
	THEME_SURFACE(Themes.SURFACE.getBottomLayer(), Themes.SURFACE.getTopLayer()),
	/** Tunnel theme */
	THEME_TUNNEL(Themes.TUNNELS.getBottomLayer(), Themes.TUNNELS.getTopLayer()),

	;

	/**
	 * Constructor which takes several internal dependencies which the dependency is
	 * dependent on
	 * @param dependencies all dependencies
	 */
	private InternalDeps(InternalNames... dependencies) {
		mDependencies = dependencies;
	}

	/**
	 * @return all dependencies as internal names
	 */
	public InternalNames[] getDependencies() {
		return mDependencies;
	}

	/** All dependencies */
	private InternalNames[] mDependencies;
}
