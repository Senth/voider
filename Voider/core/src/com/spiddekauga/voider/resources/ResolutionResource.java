package com.spiddekauga.voider.resources;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;

/**
 * Wrapper for when these resolutions should be loaded used depending on what the game's
 * current resolution is

 */
public class ResolutionResource implements IInternalResource {
	/**
	 * @param range the resolution range
	 * @param internalNames resources bound to this
	 */
	public ResolutionResource(ResolutionRange range, InternalNames... internalNames) {
		mRange = range;
		mInternalNames = internalNames;
	}

	@Override
	public boolean useResource() {
		return useResource(Gdx.graphics.getHeight());
	}

	/**
	 * @param height
	 * @return true if this resource should be used for the specified height
	 */
	public boolean useResource(int height) {
		return mRange.isWithinRange(height);
	}

	/**
	 * Checks how far away the resource is from it's optimal height
	 * @param height
	 * @return how far away the resource is from it's optimal height, 0 if equal to the
	 *         optimal height, always positive.
	 */
	public int getOptimalDifference(int height) {
		return mRange.getOptimalDifference(height);
	}

	@Override
	public InternalNames[] getDependencies() {
		return mInternalNames;
	}

	/**
	 * @return true if all dependencies are loaded
	 */
	public boolean isLoaded() {
		for (InternalNames name : mInternalNames) {
			if (!ResourceCacheFacade.isLoaded(name)) {
				return false;
			}
		}
		return true;
	}

	private InternalNames[] mInternalNames;
	private ResolutionRange mRange;
}
