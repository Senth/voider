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

	/** Music for space */
	MUSIC_SPACE(InternalNames.MUSIC_SPACE),
	/** Music for surface */
	MUSIC_SURFACE(InternalNames.MUSIC_SURFACE),
	/** Music for tunnels */
	MUSIC_TUNNELS(InternalNames.MUSIC_TUNNELS),
	/** Music for core */
	MUSIC_CORE(InternalNames.MUSIC_CORE),

	/** Easy access to all game music that needs to be loaded */
	GAME_MUSIC(InternalNames.MUSIC_GAME_OVER_INTRO, InternalNames.MUSIC_LEVEL_COMPLETED),
	/** Easy access to all game sound effects that will be loaded */
	GAME_SFX(InternalNames.SOUND_BULLET_HIT_PLAYER, InternalNames.SOUND_ENEMY_EXLODES, InternalNames.SOUND_SHIP_COLLIDE,
			InternalNames.SOUND_SHIP_LOST, InternalNames.SOUND_SHIP_LOW_HEALTH),
	/** Easy load/unload access to all music themes */
	MUSIC_LEVEL_THEMES(InternalNames.MUSIC_SPACE, InternalNames.MUSIC_SURFACE, InternalNames.MUSIC_TUNNELS, InternalNames.MUSIC_CORE),
	/** Easy load/unload access to all background themes */
	THEME_ALL(InternalNames.LEVEL_THEME_CORE_BOTTOM, InternalNames.LEVEL_THEME_CORE_TOP, InternalNames.LEVEL_THEME_SPACE_BOTTOM,
			InternalNames.LEVEL_THEME_SPACE_TOP, InternalNames.LEVEL_THEME_SURFACE_BOTTOM, InternalNames.LEVEL_THEME_SURFACE_TOP,
			InternalNames.LEVEL_THEME_TUNNELS_BOTTOM, InternalNames.LEVEL_THEME_TUNNELS_TOP),
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
	 * @return all dependencies as internal names
	 */
	public InternalNames[] getDependencies() {
		return mDependencies;
	}

	/** All dependencies */
	private InternalNames[] mDependencies;
}
