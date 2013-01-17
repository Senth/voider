package com.spiddekauga.voider.game;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;

/**
 * A game level
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Level extends Resource implements ITriggerListener, Json.Serializable, Disposable {
	/**
	 * Constructor which creates an new empty level with the bound
	 * level definition
	 * @param levelDef the level definition of this level
	 */
	public Level(LevelDef levelDef) {
		mLevelDef = levelDef;
		mUniqueId = levelDef.getLevelId();
		mActors = new ArrayList<Actor>();
		mTriggerInformation = new TriggerContainer();
		mSpeed = mLevelDef.getBaseSpeed();
		mCompletedLevel = false;
	}

	/**
	 * @return true if the player has completed the level
	 */
	public boolean hasCompletedLevel() {
		return mCompletedLevel;
	}

	/**
	 * Updates all the actors in the level.
	 * This shall always be run, even in the editor
	 * @param run set this to true if you want to play the level
	 * as this will make the screen move, the units move etc
	 */
	public void update(boolean run) {
		// Make the map move forward
		if (run) {
			mXCoord += mSpeed * Gdx.graphics.getDeltaTime();

			if (mPlayerActor != null && mPlayerActor.getBody() != null) {
				mPlayerActor.getBody().setLinearVelocity(mSpeed, 0.0f);
			}

			if (!mCompletedLevel) {
				if (mXCoord >= mLevelDef.getEndXCoord()) {
					mCompletedLevel = true;
				}
			}
		}
	}

	/**
	 * @return a copy of this level.
	 * @pre world has to been changed to the new world before copy is called()
	 */
	public Level copy() {
		Json json = new Json();
		String jsonString = json.toJson(this);
		return json.fromJson(Level.class, jsonString);
	}

	/**
	 * Sets the player ship, also determines its location
	 * @param playerActor
	 */
	public void setPlayer(PlayerActor playerActor) {
		mPlayerActor = playerActor;

		resetPlayerPosition();
	}

	/**
	 * Sets the x-coordinate of the level. This makes the level jump to or start
	 * at the specific place. Also updates the player if one exists
	 * @param x the current x-coordinate of the level
	 */
	public void setXCoord(float x) {
		mXCoord = x;

		resetPlayerPosition();
	}

	/**
	 * @return current x-coordinate of the map
	 */
	public float getXCoord() {
		return mXCoord;
	}

	/**
	 * @return current speed of the level
	 */
	public float getSpeed() {
		return mSpeed;
	}

	/**
	 * Renders the level
	 * @param spriteBatch the SpriteBatch to use for rendering
	 */
	public void render(SpriteBatch spriteBatch) {
		for (Actor actor : mActors) {
			actor.render(spriteBatch);
		}
		mPlayerActor.render(spriteBatch);
	}

	/**
	 * Renders extra information from actors when level editor is active
	 * @param spriteBatch the SpriteBatch to use for rendering
	 */
	public void renderEditor(SpriteBatch spriteBatch) {
		for (Actor actor : mActors) {
			actor.renderEditor(spriteBatch);
		}
	}

	/**
	 * Adds an actor to the level
	 * @param actor the actor to add to the level
	 */
	public void addActor(Actor actor) {
		mActors.add(actor);
		actor.createBody();

		// Add to dependency, if it doesn't load its own def
		if (!actor.savesDef()) {
			mLevelDef.addDependency(actor.getDef());
		}
	}

	/**
	 * Checks if the actor exists inside this level
	 * @param actor the actor check if it exist
	 * @return true if this level contains the specified actor
	 */
	public boolean containsActor(Actor actor) {
		return mActors.contains(actor);
	}

	/**
	 * Removes an actor from the level
	 * @param actorId the actor to remove
	 */
	public void removeActor(UUID actorId) {
		Actor actor = null;
		for (int i = 0; i < mActors.size(); ++i) {
			if (mActors.get(i).equals(actorId)) {
				actor = mActors.remove(i);
				break;
			}
		}

		if (actor != null) {
			actor.destroyBody();
		} else {
			Gdx.app.error("Level", "Could not find the actor to remove");
		}
	}

	@Override
	public void dispose() {
		for (Actor actor : mActors) {
			actor.dispose();
		}
	}

	/**
	 * @return the level definition
	 */
	public LevelDef getDef() {
		return mLevelDef;
	}

	/** All actors in the level */
	private ArrayList<Actor> mActors = null;
	/** All trigger information in the level, needed for duplication saving/loading and binding */
	private TriggerContainer mTriggerInformation = null;
	/** Current x coordinate (of the screen's left edge) */
	private float mXCoord = 0.0f;
	/** Level definition for this level */
	private LevelDef mLevelDef = null;
	/** Current speed of the level */
	private float mSpeed;
	/** If the level has been completed */
	private boolean mCompletedLevel;
	/** The player actor */
	private PlayerActor mPlayerActor = null;


	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#write(com.badlogic.gdx.utils.Json)
	 */
	@Override
	public void write(Json json) {
		super.write(json);

		json.writeValue("mActors", mActors);
		json.writeValue("mLevelDefId", mLevelDef.getId());
		json.writeValue("mXCoord", mXCoord);
		json.writeValue("mSpeed", mSpeed);
		json.writeValue("mCompletedLevel", mCompletedLevel);
		json.writeValue("mTriggerInformation", mTriggerInformation);
	}


	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#read(com.badlogic.gdx.utils.Json, com.badlogic.gdx.utils.OrderedMap)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		super.read(json, jsonData);

		mXCoord = json.readValue("mXCoord", float.class, jsonData);
		mSpeed = json.readValue("mSpeed", float.class, jsonData);
		mCompletedLevel = json.readValue("mCompletedLevel", boolean.class, jsonData);

		// Actors
		mActors = json.readValue("mActors", ArrayList.class, jsonData);

		// Get the actual LevelDef
		UUID levelDefId = json.readValue("mLevelDefId", UUID.class, jsonData);
		try {
			mLevelDef = ResourceCacheFacade.get(levelDefId, LevelDef.class);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("Level", "Could not get level def when loading level");
		}


		// Triggers
		mTriggerInformation = json.readValue("mTriggerInformation", TriggerContainer.class, jsonData);

		// Bind triggers
		ObjectMap<UUID, ITriggerListener> actorsMap = new ObjectMap<UUID, ITriggerListener>();
		for (Actor actor : mActors) {
			actorsMap.put(actor.getId(), actor);
		}
		mTriggerInformation.bindTriggers(actorsMap);
	}

	@Override
	public void onTriggered(String action) {
		/** @TODO Auto-generated method stub */
	}

	/**
	 * Default constructor, used when loading levels.
	 */
	protected Level() {
		// Does nothing
	}

	/**
	 * Resets the player position
	 */
	private void resetPlayerPosition() {
		if (mPlayerActor != null && mPlayerActor.getBody() != null) {
			Vector2 playerPosition = Pools.obtain(Vector2.class);
			playerPosition.set(mXCoord, 0);

			// Get radius of player and offset it with the width
			ArrayList<Fixture> playerFixtures = mPlayerActor.getBody().getFixtureList();

			if (playerFixtures.size() > 0) {
				float radius = playerFixtures.get(0).getShape().getRadius();
				playerPosition.x += radius * 2;

				mPlayerActor.getBody().setTransform(playerPosition, 0.0f);
			}
			Pools.free(playerPosition);
		}
	}
}
