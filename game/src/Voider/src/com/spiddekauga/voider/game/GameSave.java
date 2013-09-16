package com.spiddekauga.voider.game;

import java.util.UUID;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
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

	@Override
	public void write(Json json) {
		super.write(json);

		mLevel.removeResource(mLevel.getId());
		json.writeValue("mLevel", mLevel);
		json.writeValue("mPlayerActor", mPlayerActor);
		json.writeValue("mBulletDestroyer", mBulletDestroyer);
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		super.read(json, jsonData);

		mLevel = json.readValue("mLevel", Level.class, jsonData);
		mPlayerActor = json.readValue("mPlayerActor", PlayerActor.class, jsonData);
		mBulletDestroyer = json.readValue("mBulletDestroyer", BulletDestroyer.class, jsonData);
	}

	/** Saved level */
	@Tag(10) private Level mLevel;
	/** Saved player ship */
	@Tag(11) private PlayerActor mPlayerActor;
	/** Saved bullets */
	@Tag(12) private BulletDestroyer mBulletDestroyer;
}
