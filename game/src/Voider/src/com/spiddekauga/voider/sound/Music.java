package com.spiddekauga.voider.sound;

import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;

/**
 * All various music types to play
 * @author Matteus Magnusson <matteus.magnusso@spiddekauga.com>
 */
public enum Music {
	/** Space theme */
	SPACE(InternalNames.MUSIC_SPACE),
	/** Surface theme */
	SURFACE(InternalNames.MUSIC_SURFACE),
	/** Tunnels theme */
	TUNNELS(InternalNames.MUSIC_TUNNELS),
	/** Core theme */
	CORE(InternalNames.MUSIC_CORE),
	/** Game over */
	GAME_OVER(InternalNames.MUSIC_GAME_OVER),
	/** Game completed */
	LEVEL_COMPLETED(InternalNames.MUSIC_LEVEL_COMPLETED),
	/** Main theme */
	TITLE(InternalNames.MUSIC_TITLE),

	;

	/**
	 * Sets the internal resource this music uses
	 * @param internalName the internal resource this music uses
	 */
	private Music(InternalNames internalName) {
		mInternalName = internalName;
	}

	/**
	 * @return the actual music track, null if not loaded
	 */
	com.badlogic.gdx.audio.Music getTrack() {
		return ResourceCacheFacade.get(mInternalName);
	}

	private InternalNames mInternalName;
}
