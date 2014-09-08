package com.spiddekauga.voider.resources;

import com.spiddekauga.voider.repo.resource.InternalNames;

/**
 * Internal dependencies for various resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum InternalDeps {
	// !!!NEVER EVER remove or change order of these!!!
	/** Space theme */
	THEME_SPACE(InternalNames.LEVEL_THEME_SPACE_BOTTOM, InternalNames.LEVEL_THEME_SPACE_TOP),
	/** Core theme */
	THEME_CORE(InternalNames.LEVEL_THEME_CORE_BOTTOM, InternalNames.LEVEL_THEME_CORE_TOP),
	/** Surface theme */
	THEME_SURFACE(InternalNames.LEVEL_THEME_SURFACE_BOTTOM, InternalNames.LEVEL_THEME_SURFACE_TOP),
	/** Tunnel theme */
	THEME_TUNNEL(InternalNames.LEVEL_THEME_TUNNELS_BOTTOM, InternalNames.LEVEL_THEME_TUNNELS_TOP),

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
