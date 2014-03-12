package com.spiddekauga.voider.game;

import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.resources.Resource;

/**
 * All information from a saved game
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class GameSave extends Resource {
	/**
	 * Constructs a game save with all the necessary information
	 * @param level the level to save
	 * @param playerActor ship of the player
	 * @param bulletDestroyer all bullets currently in-game
	 * @param gameTime current game time to be restored lator
	 */
	public GameSave(Level level, PlayerActor playerActor, BulletDestroyer bulletDestroyer, GameTime gameTime) {
		mUniqueId = UUID.randomUUID();
		mLevel = level;
		mPlayerActor = playerActor;
		mBulletDestroyer = bulletDestroyer;
		mGameTime = gameTime;
	}

	/**
	 * Default constructor for Kryo
	 */
	protected GameSave() {
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

	/**
	 * @return saved game time
	 */
	public GameTime getGameTime() {
		return mGameTime;
	}

	/** Saved game time */
	@Tag(107) private GameTime mGameTime;
	/** Saved level */
	@Tag(10) private Level mLevel;
	/** Saved player ship */
	@Tag(11) private PlayerActor mPlayerActor;
	/** Saved bullets */
	@Tag(12) private BulletDestroyer mBulletDestroyer;
}
