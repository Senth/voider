package com.spiddekauga.voider.sound;

import com.badlogic.gdx.audio.Sound;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;


/**
 * All the sound effects for Voider
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum Sounds {
	// Game
	/** When the player ship has low health */
	SHIP_LOW_HEALTH(InternalNames.SOUND_SHIP_LOW_HEALTH, EffectCategory.GAME, ConfigIni.getInstance().sound.effect.getLowHealthTime()),
	/** Bullet hits a player */
	BULLET_HIT_PLAYER(InternalNames.SOUND_BULLET_HIT_PLAYER, EffectCategory.GAME),
	/** Ship collision with terrain or enemy */
	SHIP_COLLIDE(InternalNames.SOUND_SHIP_COLLIDE, EffectCategory.GAME, -1),
	/** Enemy ship explodes on player */
	ENEMY_EXPLODES(InternalNames.SOUND_ENEMY_EXLODES, EffectCategory.GAME),
	/** Player lost a ship/life */
	SHIP_LOST(InternalNames.SOUND_SHIP_LOST, EffectCategory.GAME),

	// UI
	/** Hover over button */
	UI_BUTTON_HOVER(InternalNames.SOUND_UI_BUTTON_HOVER, EffectCategory.UI),
	/** Button click */
	UI_BUTTON_CLICK(InternalNames.SOUND_UI_BUTTON_CLICK, EffectCategory.UI),

	;

	/**
	 * Constructor for sounds. This usually only contains one sound, but can hold a chain
	 * of sounds
	 * @param internalName sound file name
	 * @param category the effect category for the sound
	 */
	private Sounds(InternalNames internalName, EffectCategory category) {
		this(internalName, category, 0);
	}


	/**
	 * Constructor for sounds. This usually only contains one sound, but can hold a chain
	 * of sounds
	 * @param internalName sound file name
	 * @param category the effect category for the sound
	 * @param loopTime if 0: doesn't loop, if -1 loops until stopped, if positive loops
	 *        for X seconds (or until stopped)
	 */
	private Sounds(InternalNames internalName, EffectCategory category, float loopTime) {
		mEffectCategory = category;
		mInternalName = internalName;
		mLoopTime = loopTime;
		if (mLoopTime != 0) {
			mLooping = true;
		}
	}

	/**
	 * Effect types, used for determining the volume of the sounds
	 */
	enum EffectCategory {
		/** UI effects */
		UI,
		/** In-game sound effects */
		GAME,
	}

	/**
	 * @return sound effects category
	 */
	EffectCategory getEffectCategory() {
		return mEffectCategory;
	}

	/**
	 * @return true if the sound effect should loop
	 */
	boolean isLoopable() {
		return mLooping;
	}

	/**
	 * @return loop time. If greater than 0 the time to loop, if less than 0 loops forever
	 *         until stopped
	 */
	float getLoopTime() {
		return mLoopTime;
	}

	/**
	 * @return the actual sound track, null if not loaded
	 */
	Sound getTrack() {
		return ResourceCacheFacade.get(mInternalName);
	}

	/**
	 * Set the loop id
	 * @param id loop id
	 */
	synchronized void setLoopId(long id) {
		if (isLoopable()) {
			mLoopId = id;
		}
	}

	/**
	 * @return true if a loop track is currently playing
	 */
	synchronized boolean isPlaying() {
		return mLoopId != INVALID_ID;
	}

	/**
	 * @return current loop id
	 */
	synchronized long getLoopId() {
		return mLoopId;
	}

	/**
	 * @return true if the loopable track loops forever
	 */
	boolean isLoopingForever() {
		return mLoopTime == LOOP_FOREVER;
	}

	private static final float LOOP_FOREVER = -1;
	/** Invalid loop id */
	public static final long INVALID_ID = -1;
	/** Resource file */
	private InternalNames mInternalName;
	/** Loop time of the sound (if mLooping is true) */
	private float mLoopTime = 0;
	/** If the sound should loop */
	private boolean mLooping = false;
	/** Id for loopable tracks, -1 if it's not playing */
	private long mLoopId = INVALID_ID;
	private EffectCategory mEffectCategory;
}
