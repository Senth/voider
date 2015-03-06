package com.spiddekauga.voider.resources;

import java.util.ArrayList;

import com.spiddekauga.utils.Collections;
import com.spiddekauga.voider.repo.resource.InternalNames;

/**
 * Internal dependencies for various resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum InternalDeps {
	// !!!NEVER EVER remove or change order of these!!!
	/** Space theme */
	THEME_SPACE(new ResolutionResource(ResolutionRange.RES_720, InternalNames.LEVEL_BACKGROUND_SPACE_720), new ResolutionResource(
			ResolutionRange.RES_1080, InternalNames.LEVEL_BACKGROUND_SPACE_1080)),
	/** Core theme */
	THEME_CORE(new ResolutionResource(ResolutionRange.RES_720, InternalNames.LEVEL_BACKGROUND_CORE_720), new ResolutionResource(ResolutionRange.RES_1080,
			InternalNames.LEVEL_BACKGROUND_CORE_1080)),
	/** Surface theme */
	THEME_SURFACE(new ResolutionResource(ResolutionRange.RES_720, InternalNames.LEVEL_BACKGROUND_SURFACE_720), new ResolutionResource(
			ResolutionRange.RES_1080, InternalNames.LEVEL_BACKGROUND_SURFACE_1080)),
	/** Tunnel theme */
	THEME_TUNNEL(new ResolutionResource(ResolutionRange.RES_720, InternalNames.LEVEL_BACKGROUND_TUNNELS_720), new ResolutionResource(
			ResolutionRange.RES_1080, InternalNames.LEVEL_BACKGROUND_TUNNELS_1080)),

	/** Music for space */
	MUSIC_SPACE(InternalNames.MUSIC_SPACE),
	/** Music for surface */
	MUSIC_SURFACE(InternalNames.MUSIC_SURFACE),
	/** Music for tunnels */
	MUSIC_TUNNEL(InternalNames.MUSIC_TUNNEL),
	/** Music for core */
	MUSIC_CORE(InternalNames.MUSIC_CORE),

	/** Easy access to all game music that needs to be loaded */
	GAME_MUSIC(InternalNames.MUSIC_GAME_OVER_INTRO, InternalNames.MUSIC_LEVEL_COMPLETED),
	/** Easy access to all game sound effects that will be loaded */
	GAME_SFX(InternalNames.SOUND_BULLET_HIT_PLAYER, InternalNames.SOUND_ENEMY_EXLODES, InternalNames.SOUND_SHIP_COLLIDE,
			InternalNames.SOUND_SHIP_LOST, InternalNames.SOUND_SHIP_LOW_HEALTH),
	/** Easy load/unload access to all music themes */
	MUSIC_LEVEL_THEMES(InternalNames.MUSIC_SPACE, InternalNames.MUSIC_SURFACE, InternalNames.MUSIC_TUNNEL, InternalNames.MUSIC_CORE),
	/** Easy load/unload access to all background themes */
	THEME_ALL((ResolutionResource) null),
	/** Easy load/unload for all UI sounds */
	UI_SFX(InternalNames.SOUND_UI_BUTTON_CLICK, InternalNames.SOUND_UI_BUTTON_HOVER),

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
	 * Constructor which takes several resolution specific resources
	 * @param resolutionResources all resource with resolutions
	 */
	private InternalDeps(ResolutionResource... resolutionResources) {
		mResolutionResources = resolutionResources;
	}

	/**
	 * @return all dependencies as internal names
	 */
	public InternalNames[] getDependencies() {
		if (mDependencies != null) {
			return mDependencies;
		} else {
			ArrayList<InternalNames> dependencies = new ArrayList<>();

			for (ResolutionResource resolutionResource : mResolutionResources) {
				if (resolutionResource.useResource()) {
					Collections.addAll(resolutionResource.getDependencise(), dependencies);
				}
			}

			InternalNames[] depArr = new InternalNames[dependencies.size()];
			return dependencies.toArray(depArr);
		}
	}

	/** All dependencies (if not resolution specific) */
	private InternalNames[] mDependencies = null;
	/** All resolution specific dependencies */
	private ResolutionResource[] mResolutionResources = null;


	static {
		// Add all level backgrounds
		ArrayList<ResolutionResource> resources = new ArrayList<>();

		// Add 1080p versions
		resources.add(new ResolutionResource(ResolutionRange.RES_ALL, InternalNames.LEVEL_BACKGROUND_CORE_1080));
		resources.add(new ResolutionResource(ResolutionRange.RES_ALL, InternalNames.LEVEL_BACKGROUND_SPACE_1080));
		resources.add(new ResolutionResource(ResolutionRange.RES_ALL, InternalNames.LEVEL_BACKGROUND_SURFACE_1080));
		resources.add(new ResolutionResource(ResolutionRange.RES_ALL, InternalNames.LEVEL_BACKGROUND_TUNNELS_1080));

		// Add 480p versions
		resources.add(new ResolutionResource(ResolutionRange.RES_ALL, InternalNames.LEVEL_BACKGROUND_CORE_480));
		resources.add(new ResolutionResource(ResolutionRange.RES_ALL, InternalNames.LEVEL_BACKGROUND_SPACE_480));
		resources.add(new ResolutionResource(ResolutionRange.RES_ALL, InternalNames.LEVEL_BACKGROUND_SURFACE_480));
		resources.add(new ResolutionResource(ResolutionRange.RES_ALL, InternalNames.LEVEL_BACKGROUND_TUNNELS_480));

		// Add 120p versions
		resources.add(new ResolutionResource(ResolutionRange.RES_ALL, InternalNames.LEVEL_BACKGROUND_CORE_120));
		resources.add(new ResolutionResource(ResolutionRange.RES_ALL, InternalNames.LEVEL_BACKGROUND_SPACE_120));
		resources.add(new ResolutionResource(ResolutionRange.RES_ALL, InternalNames.LEVEL_BACKGROUND_SURFACE_120));
		resources.add(new ResolutionResource(ResolutionRange.RES_ALL, InternalNames.LEVEL_BACKGROUND_TUNNELS_120));

		THEME_ALL.mResolutionResources = new ResolutionResource[resources.size()];
		resources.toArray(THEME_ALL.mResolutionResources);
	}
}
