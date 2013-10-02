package com.spiddekauga.voider.game;

import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.resources.Resource;

/**
 * All information from a saved game
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class GameSave extends Resource {
	/**
	 * Constructs a game save with all the necessary information
	 * @param level the level to save
	 * @param playerActor ship of the player
	 * @param bulletDestroyer all bullets currently in-game
	 */
	public GameSave(Level level, PlayerActor playerActor, BulletDestroyer bulletDestroyer) {
		mUniqueId = UUID.randomUUID();
		mLevel = level;
		mPlayerActor = playerActor;
		mBulletDestroyer = bulletDestroyer;
	}

	/**
	 * Default constructor for JSON
	 * @note needs to be public for reflect on android
	 */
	public GameSave() {
		// Does nothing
	}

	/**
	 * @return saved level
	 */
	public Level getLevel() {
		return mLevel;
	}

	/**
	 * @return saved player ship
	 */
	public PlayerActor getPlayerActor() {
		return mPlayerActor;
	}

	/**
	 * @return saved bullets
	 */
	public BulletDestroyer getBulletDestroyer() {
		return mBulletDestroyer;
	}

	/** Saved level */
	@Tag(10) private Level mLevel;
	/** Saved player ship */
	@Tag(11) private PlayerActor mPlayerActor;
	/** Saved bullets */
	@Tag(12) private BulletDestroyer mBulletDestroyer;
}
