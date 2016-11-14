package com.spiddekauga.voider.sound;

import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalDeps;

/**
 * All various music types to play
 */
public enum Music {
// !!!NEVER EVER remove or change order of these!!!
	/** Space theme */
	SPACE(InternalNames.MUSIC_SPACE, InternalDeps.MUSIC_SPACE),
	/** Surface theme */
	SURFACE(InternalNames.MUSIC_SURFACE, InternalDeps.MUSIC_SURFACE),
	/** Tunnels theme */
	TUNNELS(InternalNames.MUSIC_TUNNEL, InternalDeps.MUSIC_TUNNEL),
	/** Core theme */
	CORE(InternalNames.MUSIC_CORE, InternalDeps.MUSIC_CORE),
	/** Game over */
	GAME_OVER_INTRO(InternalNames.MUSIC_GAME_OVER_INTRO, false),
	/** Game over loop */
	GAME_OVER_LOOP(InternalNames.MUSIC_GAME_OVER_LOOP),
	/** Game completed */
	LEVEL_COMPLETED(InternalNames.MUSIC_LEVEL_COMPLETED),
	/** Main theme */
	TITLE(InternalNames.MUSIC_TITLE),;

/** Internal dependencies for level themes */
private InternalDeps mInternalDeps = null;
private InternalNames mInternalName;
private boolean mLoop = true;


/**
 * Sets the internal resource this music uses
 * @param internalName the internal resource this music uses
 */
private Music(InternalNames internalName) {
	this(internalName, null, true);
}

/**
 * Sets the internal resource this music uses
 * @param internalName the internal resource this music uses
 * @param internalDeps internal dependencies for level themes
 * @param loop true if the track should be looping (default: true)
 */
private Music(InternalNames internalName, InternalDeps internalDeps, boolean loop) {
	mInternalName = internalName;
	mInternalDeps = internalDeps;
	mLoop = loop;
}

/**
 * Sets the internal resource this music uses
 * @param internalName the internal resource this music uses
 * @param internalDeps internal dependencies for level themes
 */
private Music(InternalNames internalName, InternalDeps internalDeps) {
	this(internalName, internalDeps, true);
}

/**
 * Sets the internal resource this music uses
 * @param internalName the internal resource this music uses
 * @param loop true if the track should be looping (default: true)
 */
private Music(InternalNames internalName, boolean loop) {
	this(internalName, null, loop);
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

@Override
public String toString() {
	return name().charAt(0) + name().substring(1).toLowerCase();
}

/**
 * @return the actual music track, null if not loaded
 */
com.badlogic.gdx.audio.Music getTrack() {
	com.badlogic.gdx.audio.Music track = ResourceCacheFacade.get(mInternalName);
	if (track != null) {
		track.setLooping(mLoop);
	}
	return track;
}

/**
 * @return internal dependencies for the level themes
 */
public InternalDeps getDependency() {
	return mInternalDeps;
}

/**
 * @return true if the track should be looped
 */
public boolean isLoop() {
	return mLoop;
}
}
