package com.spiddekauga.voider.game;

import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.resources.ResolutionRange;
import com.spiddekauga.voider.resources.ResolutionResource;

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
	 * Get the specified
	 */

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
	 * Create a level background depending on the loaded theme
	 * @return new level background if any were loaded, null if none was loaded
	 */
	public LevelBackground createBackground() {
		InternalNames[] names = mDependency.getDependencies();

		if (names != null && names.length == 1) {
			return new LevelBackground(names[0]);
		} else {
			return null;
		}
	}

	/**
	 * Checks if the specified background is the correct choice depending on the height
	 * @param background
	 * @param height
	 * @return same background if it's still valid, a new background resolution if a
	 *         better one could be used
	 */
	public LevelBackground updateBackground(LevelBackground background, int height) {
		ResolutionResource resolution = getClosestLoadedResolutionResource(height);

		if (resolution.getDependencise()[0] != background.getDependency()) {
			return new LevelBackground(resolution.getDependencise()[0]);
		} else {
			return background;
		}
	}

	/**
	 * Creates a new background
	 * @param height
	 * @return a new background resolution if a better one could be used
	 */
	public LevelBackground createBackground(int height) {
		ResolutionResource resolution = getClosestLoadedResolutionResource(height);
		return new LevelBackground(resolution.getDependencise()[0]);
	}

	/**
	 * Get the resolution resource that closest matches the height (and is loaded)
	 * @param height
	 * @return the resolution range that closest matches the height
	 */
	private ResolutionResource getClosestLoadedResolutionResource(int height) {
		ResolutionResource foundResolution = null;
		for (ResolutionResource resolution : mResolutions) {
			if (resolution.useResource(height)) {
				if (resolution.isLoaded()) {
					foundResolution = resolution;
				} else {
					break;
				}
			}
		}

		if (foundResolution == null) {
			int minDist = Integer.MAX_VALUE;
			for (ResolutionResource resolution : mResolutions) {
				if (resolution.isLoaded()) {
					int optimalDiff = resolution.getOptimalDifference(height);
					if (optimalDiff < minDist) {
						minDist = optimalDiff;
						foundResolution = resolution;
					}
				}
			}
		}

		return foundResolution;
	}

	/**
	 * @return dependency of the theme
	 */
	public InternalDeps getDependency() {
		return mDependency;
	}

	private InternalDeps mDependency;
	private String mName;
	private ResolutionResource[] mResolutions = new ResolutionResource[4];

	private static final ResolutionRange RES_120 = new ResolutionRange(0, 240, 120);
	private static final ResolutionRange RES_480 = new ResolutionRange(241, 600, 480);
	private static final ResolutionRange RES_720 = new ResolutionRange(601, 900, 720);
	private static final ResolutionRange RES_1080 = new ResolutionRange(901, Integer.MAX_VALUE, 1080);

	// Set the resolutions
	static {
		// Space
		SPACE.mResolutions[0] = new ResolutionResource(RES_120, InternalNames.LEVEL_BACKGROUND_SPACE_120);
		SPACE.mResolutions[1] = new ResolutionResource(RES_480, InternalNames.LEVEL_BACKGROUND_SPACE_480);
		SPACE.mResolutions[2] = new ResolutionResource(RES_720, InternalNames.LEVEL_BACKGROUND_SPACE_720);
		SPACE.mResolutions[3] = new ResolutionResource(RES_1080, InternalNames.LEVEL_BACKGROUND_SPACE_1080);

		// Surface
		SURFACE.mResolutions[0] = new ResolutionResource(RES_120, InternalNames.LEVEL_BACKGROUND_SURFACE_120);
		SURFACE.mResolutions[1] = new ResolutionResource(RES_480, InternalNames.LEVEL_BACKGROUND_SURFACE_480);
		SURFACE.mResolutions[2] = new ResolutionResource(RES_720, InternalNames.LEVEL_BACKGROUND_SURFACE_720);
		SURFACE.mResolutions[3] = new ResolutionResource(RES_1080, InternalNames.LEVEL_BACKGROUND_SURFACE_1080);

		// Tunnels
		TUNNELS.mResolutions[0] = new ResolutionResource(RES_120, InternalNames.LEVEL_BACKGROUND_TUNNELS_120);
		TUNNELS.mResolutions[1] = new ResolutionResource(RES_480, InternalNames.LEVEL_BACKGROUND_TUNNELS_480);
		TUNNELS.mResolutions[2] = new ResolutionResource(RES_720, InternalNames.LEVEL_BACKGROUND_TUNNELS_720);
		TUNNELS.mResolutions[3] = new ResolutionResource(RES_1080, InternalNames.LEVEL_BACKGROUND_TUNNELS_1080);

		// Core
		CORE.mResolutions[0] = new ResolutionResource(RES_120, InternalNames.LEVEL_BACKGROUND_CORE_120);
		CORE.mResolutions[1] = new ResolutionResource(RES_480, InternalNames.LEVEL_BACKGROUND_CORE_480);
		CORE.mResolutions[2] = new ResolutionResource(RES_720, InternalNames.LEVEL_BACKGROUND_CORE_720);
		CORE.mResolutions[3] = new ResolutionResource(RES_1080, InternalNames.LEVEL_BACKGROUND_CORE_1080);
	}
}
