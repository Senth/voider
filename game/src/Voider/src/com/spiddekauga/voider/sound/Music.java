package com.spiddekauga.voider.sound;

import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalDeps;

/**
 * All various music types to play
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum Music {
	// !!!NEVER EVER remove or change order of these!!!
	/** Space theme */
	SPACE(InternalNames.MUSIC_SPACE, InternalDeps.MUSIC_SPACE),
	/** Surface theme */
	SURFACE(InternalNames.MUSIC_SURFACE, InternalDeps.MUSIC_SURFACE),
	/** Tunnels theme */
	TUNNELS(InternalNames.MUSIC_TUNNELS, InternalDeps.MUSIC_TUNNELS),
	/** Core theme */
	CORE(InternalNames.MUSIC_CORE, InternalDeps.MUSIC_CORE),
	/** Game over */
	GAME_OVER(InternalNames.MUSIC_GAME_OVER),
	/** Game completed */
	LEVEL_COMPLETED(InternalNames.MUSIC_LEVEL_COMPLETED),
	/** Main theme */
	TITLE(InternalNames.MUSIC_TITLE),

	;

	@Override
	public String toString() {
		return name().charAt(0) + name().substring(1).toLowerCase();
	}

	/**
	 * Sets the internal resource this music uses
	 * @param internalName the internal resource this music uses
	 */
	private Music(InternalNames internalName) {
		this(internalName, null);
	}

	/**
	 * Sets the internal resource this music uses
	 * @param internalName the internal resource this music uses
	 * @param internalDeps internal dependencies for level themes
	 */
	private Music(InternalNames internalName, InternalDeps internalDeps) {
		mInternalName = internalName;
		mInternalDeps = internalDeps;
	}


	/**
	 * @return the actual music track, null if not loaded
	 */
	com.badlogic.gdx.audio.Music getTrack() {
		return ResourceCacheFacade.get(mInternalName);
	}

	/**
	 * @return internal dependencies for the level themes
	 */
	public InternalDeps getDependency() {
		return mInternalDeps;
	}

	/**
	 * @return a list of all level themes
	 */
	public static Music[] getLevelThemes() {
		Music[] themes = new Music[4];
		themes[0] = SPACE;
		themes[1] = SURFACE;
		themes[2] = TUNNELS;
		themes[3] = CORE;
		return themes;
	}

	/** Internal dependencies for level themes */
	private InternalDeps mInternalDeps = null;
	private InternalNames mInternalName;
}
