package com.spiddekauga.voider.sound;

import com.spiddekauga.utils.EventBus;
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
	/** Game over fanfare */
	GAME_OVER_INTRO(InternalNames.MUSIC_GAME_OVER_INTRO, false),
	/** Game over loop */
	GAME_OVER_LOOP(InternalNames.MUSIC_GAME_OVER_LOOP),
	/** Game completed fanfare */
	LEVEL_COMPLETED_INTRO(InternalNames.MUSIC_LEVEL_COMPLETED_INTRO, false),
	/** Main theme */
	TITLE(InternalNames.MUSIC_TITLE),
	/** Game completed music loop */
	LEVEL_COMPLETED_LOOP(InternalNames.MUSIC_LEVEL_COMPLETED_LOOP);

private static final EventBus mEventBus = EventBus.getInstance();
private InternalDeps mInternalDeps = null;
private InternalNames mInternalName;
/** This track might be out of date and unloaded! */
private com.badlogic.gdx.audio.Music mTrack = null;
private boolean mLoop = true;
private boolean mPlaying = false;
private boolean mInUse = false;
private OnCompletionListener mOnCompletionListener = new OnCompletionListener();


/**
 * Sets the internal resource this music uses
 * @param internalName the internal resource this music uses
 */
Music(InternalNames internalName) {
	this(internalName, null, true);
}

/**
 * Sets the internal resource this music uses
 * @param internalName the internal resource this music uses
 * @param internalDeps internal dependencies for level themes
 * @param loop true if the track should be looping (default: true)
 */
Music(InternalNames internalName, InternalDeps internalDeps, boolean loop) {
	mInternalName = internalName;
	mInternalDeps = internalDeps;
	mLoop = loop;
}

/**
 * Sets the internal resource this music uses
 * @param internalName the internal resource this music uses
 * @param internalDeps internal dependencies for level themes
 */
Music(InternalNames internalName, InternalDeps internalDeps) {
	this(internalName, internalDeps, true);
}

/**
 * Sets the internal resource this music uses
 * @param internalName the internal resource this music uses
 * @param loop true if the track should be looping (default: true)
 */
Music(InternalNames internalName, boolean loop) {
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

/**
 * Get the connected music enumeration from a track
 * @param track the track to get the connected music enumeration from
 * @return the music enumeration from the specified track, null if not found
 */
public static Music fromTrack(com.badlogic.gdx.audio.Music track) {
	for (Music music : values()) {
		if (music.mTrack == track) {
			return music;
		}
	}

	return null;
}

@Override
public String toString() {
	return name().charAt(0) + name().substring(1).toLowerCase();
}

/**
 * @return the actual music track, null if not loaded
 */
com.badlogic.gdx.audio.Music getTrack() {
	if (mTrack == null) {
		updateTrack();
	}

	return mTrack;
}

private void updateTrack() {
	mTrack = ResourceCacheFacade.get(mInternalName);
	if (mTrack != null) {
		mTrack.setOnCompletionListener(mOnCompletionListener);
		mTrack.setLooping(mLoop);
	}
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

/**
 * Return true if this music piece is currently used. This differs from {@link #isPlaying()} since
 * this method can return true even if a piece isn't playing at the moment
 * @return true if this music piece is currently in use.
 */
public boolean isInUse() {
	return mInUse || isPlaying();
}

/**
 * @return true if the track is playing
 */
public boolean isPlaying() {
	return mPlaying;
}

/**
 * Set the music as playing. Automatically sets the in use when this is called
 * @param playing set true for playing, false for stopped. If true piece is set to in use.
 */
void setPlaying(boolean playing) {
	mPlaying = playing;
	setInUse(playing);
}

/**
 * Set the music as in use
 * @param inUse true if the piece is currently in use, false if it's not in use
 */
void setInUse(boolean inUse) {
	mInUse = inUse;

	if (mInUse) {
		updateTrack();
	}
}

public class OnCompletionListener implements com.badlogic.gdx.audio.Music.OnCompletionListener {
	@Override
	public void onCompletion(com.badlogic.gdx.audio.Music music) {
		setPlaying(false);
		mEventBus.post(new MusicCompleteEvent(Music.this));
	}
}


}
