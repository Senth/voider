package com.spiddekauga.voider.resources;

import com.spiddekauga.utils.Collections;
import com.spiddekauga.voider.repo.resource.InternalNames;

import java.util.ArrayList;

/**
 * Internal dependencies for various resources
 */
public enum InternalDeps {
// !!!NEVER EVER remove or change order of these!!!
	/** Space theme */
	THEME_SPACE(new ResolutionResource(ResolutionRange.RES_720, InternalNames.LEVEL_BACKGROUND_SPACE_720), new ResolutionResource(
			ResolutionRange.RES_1080, InternalNames.LEVEL_BACKGROUND_SPACE_1080)),
	/** Core theme */
	THEME_CORE(new ResolutionResource(ResolutionRange.RES_720, InternalNames.LEVEL_BACKGROUND_CORE_720), new ResolutionResource(
			ResolutionRange.RES_1080, InternalNames.LEVEL_BACKGROUND_CORE_1080)),
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
	GAME_MUSIC(InternalNames.MUSIC_GAME_OVER_INTRO, InternalNames.MUSIC_GAME_OVER_LOOP, InternalNames.MUSIC_LEVEL_COMPLETED_INTRO, InternalNames.MUSIC_LEVEL_COMPLETED_LOOP),
	/** Easy access to all game sound effects that will be loaded */
	GAME_SFX(InternalNames.SOUND_BULLET_HIT_PLAYER, InternalNames.SOUND_ENEMY_EXLODES, InternalNames.SOUND_SHIP_COLLIDE,
			InternalNames.SOUND_SHIP_LOST, InternalNames.SOUND_SHIP_LOW_HEALTH),
	/** Easy load/unload access to all music themes */
	MUSIC_LEVEL_THEMES(InternalNames.MUSIC_SPACE, InternalNames.MUSIC_SURFACE, InternalNames.MUSIC_TUNNEL, InternalNames.MUSIC_CORE),
	/** Easy load/unload access to all background themes */
	THEME_ALL((ResolutionResource) null),
	/** Easy load/unload for all UI sounds */
	UI_SFX(InternalNames.SOUND_UI_BUTTON_CLICK, InternalNames.SOUND_UI_BUTTON_HOVER),

	/** General UI. Loads the correct density */
	UI_GENERAL(new DpiResource(DensityBuckets.MEDIUM, InternalNames.UI_GENERAL_MDPI), new DpiResource(DensityBuckets.HIGH,
			InternalNames.UI_GENERAL_HDPI), new DpiResource(DensityBuckets.XHIGH, InternalNames.UI_GENERAL_XHDPI)),
	/** Editor UI. Loads the correct density */
	UI_EDITOR(new DpiResource(DensityBuckets.MEDIUM, InternalNames.UI_EDITOR_MDPI),
			new DpiResource(DensityBuckets.HIGH, InternalNames.UI_EDITOR_HDPI), new DpiResource(DensityBuckets.XHIGH, InternalNames.UI_EDITOR_XHDPI)),
	/** Game UI. Loads the correct density */
	UI_GAME(new DpiResource(DensityBuckets.MEDIUM, InternalNames.UI_GAME_MDPI), new DpiResource(DensityBuckets.HIGH, InternalNames.UI_GAME_HDPI),
			new DpiResource(DensityBuckets.XHIGH, InternalNames.UI_GAME_XHDPI)),
	/** Credit UI */
	UI_CREDITS(InternalNames.UI_CREDITS),;

/** All UI elements */
public final static InternalDeps[] UI_ALL = {UI_GENERAL, UI_GAME, UI_EDITOR, UI_CREDITS};

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

	THEME_ALL.mResources = new ResolutionResource[resources.size()];
	resources.toArray(THEME_ALL.mResources);
}

/** All dependencies (if not resolution specific) */
private InternalNames[] mDependencies = null;
/** All resolution specific dependencies */
private IInternalResource[] mResources = null;

/**
 * Constructor which takes several internal dependencies which the dependency is dependent on
 * @param dependencies all dependencies
 */
InternalDeps(InternalNames... dependencies) {
	mDependencies = dependencies;
}

/**
 * Constructor which takes several resources that should only be used depending on some settings...
 */
InternalDeps(IInternalResource... resources) {
	mResources = resources;
}

/**
 * @param dependencies all dependencies
 * @return all dependencies for multiple internal dependencies
 */
public static InternalNames[] getDependencies(InternalDeps[] dependencies) {
	ArrayList<InternalNames> allDependencies = new ArrayList<>();


	for (InternalDeps internalDeps : dependencies) {
		Collections.addAll(internalDeps.getDependencies(), allDependencies);
	}

	InternalNames[] depArr = new InternalNames[allDependencies.size()];
	return allDependencies.toArray(depArr);
}

/**
 * @return all dependencies as internal names
 */
public InternalNames[] getDependencies() {
	if (mDependencies != null) {
		return mDependencies;
	} else {
		ArrayList<InternalNames> dependencies = new ArrayList<>();

		for (IInternalResource resource : mResources) {
			if (resource.useResource()) {
				Collections.addAll(resource.getDependencies(), dependencies);
			}
		}

		InternalNames[] depArr = new InternalNames[dependencies.size()];
		return dependencies.toArray(depArr);
	}
}
}
