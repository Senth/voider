package com.spiddekauga.voider.game;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
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

			if (!mCompletedLevel) {
				if (mXCoord >= mLevelDef.getEndXCoord()) {
					mCompletedLevel = true;
				}
			}
		}
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
}
