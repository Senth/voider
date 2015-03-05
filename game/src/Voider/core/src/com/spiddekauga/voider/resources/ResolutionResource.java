package com.spiddekauga.voider.resources;

import com.spiddekauga.voider.repo.resource.InternalNames;

/**
 * Wrapper for when these resolutions should be loaded used depending on what the game's
 * current resolution is
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class ResolutionResource {
	/**
	 * @param resolution the resolution range
	 * @param internalNames resources bound to this
	 */
	ResolutionResource(ResolutionDef resolution, InternalNames... internalNames) {
		mResolutionDef = resolution;
		mInternalNames = internalNames;
	}

	/**
	 * @return true if this resource should be used
	 */
	boolean useResource() {
		return mResolutionDef.isResolutionWithinRange();
	}

	/**
	 * @return all resource names
	 */
	InternalNames[] getDependencise() {
		return mInternalNames;
	}

	private InternalNames[] mInternalNames;
	private ResolutionDef mResolutionDef;
}
