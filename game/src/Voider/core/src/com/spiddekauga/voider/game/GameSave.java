package com.spiddekauga.voider.game;

import java.util.ArrayList;
import java.util.UUID;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResourceHasDef;
import com.spiddekauga.voider.resources.IResourceRevision;
import com.spiddekauga.voider.resources.Resource;

/**
 * All information from a saved game
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class GameSave extends Resource implements IResourceRevision, IResourceHasDef, KryoSerializable {
	/**
	 * Constructs a game save with all the necessary information
	 * @param level the level to save
	 * @param playerActor ship of the player
	 * @param bulletDestroyer all bullets currently in-game
	 * @param gameTime current game time to be restored later
	 */
	public GameSave(Level level, PlayerActor playerActor, BulletDestroyer bulletDestroyer, GameTime gameTime) {
		mUniqueId = UUID.randomUUID();
		mLevel = level;
		mPlayerActor = playerActor;
		mBulletDestroyer = bulletDestroyer;
		mGameTime = gameTime;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <DefType extends Def> DefType getDef() {
		return (DefType) mDef;
	}

	@Override
	public int getRevision() {
		return mDef.getRevision();
	}

	@Override
	public void setRevision(int revision) {
		// Does nothing, game save def sets the revision
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

	/**
	 * Sets the game definition
	 * @param def the game save definition
	 */
	public void setDef(GameSaveDef def) {
		mDef = def;
	}

	/**
	 * Reinitialize the game save after a load. Creates bodies etc.
	 */
	public void reinitialize() {
		// Create bullet bodies
		ArrayList<BulletActor> bullets = mBulletDestroyer.getBullets();
		for (BulletActor bulletActor : bullets) {
			bulletActor.createBody();
		}

		// Create bodies for the rest
		ArrayList<Actor> actors = mLevel.getResources(Actor.class);
		for (Actor actor : actors) {
			if (actor.hasSavedBody()) {
				actor.createBody();
			}
		}
	}

	@Override
	public void write(Kryo kryo, Output output) {
		// Class structure revision
		output.writeInt(CLASS_REVISION, true);

		// Def
		kryo.writeObject(output, mDef.getId());
	}

	@SuppressWarnings("unused")
	@Override
	public void read(Kryo kryo, Input input) {
		int classRevision = input.readInt(true);

		// Def
		UUID levelDefId = kryo.readObject(input, UUID.class);
		mDef = ResourceCacheFacade.get(levelDefId);
	}

	/** Definition */
	private GameSaveDef mDef = null;
	/** Saved game time */
	@Tag(107) private GameTime mGameTime;
	/** Saved level */
	@Tag(10) private Level mLevel;
	/** Saved player ship */
	@Tag(11) private PlayerActor mPlayerActor;
	/** Saved bullets */
	@Tag(12) private BulletDestroyer mBulletDestroyer;
	/** Class revision */
	protected static final int CLASS_REVISION = 1;
}
