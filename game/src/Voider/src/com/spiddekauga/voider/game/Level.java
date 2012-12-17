package com.spiddekauga.voider.game;

import java.util.Arrays;
import java.util.UUID;
import java.util.Vector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;

/**
 * A game level
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Level implements Json.Serializable, IResource {
	/**
	 * Constructor which creates an new empty level with the bound
	 * level definition
	 * @param levelDef the level definition of this level
	 */
	public Level(LevelDef levelDef) {
		mLevelDef = levelDef;
		mUniqueId = levelDef.getLevelId();
		mActors = new Vector<Actor>();
		mTriggers = new Vector<Trigger>();
		mSpeed = mLevelDef.getBaseSpeed();
		mCompletedLevel = false;
	}

	/**
	 * Tests whether two levels are equal. This is done by the unique id
	 * @param object the object to test if it's equal
	 */
	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		} else if (object == null) {
			return false;
		} else if (object instanceof Level) {
			return ((Level)object).mUniqueId.equals(mUniqueId);
		} else if (object instanceof UUID) {
			return mUniqueId.equals(object);
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.resources.IResource#getId()
	 */
	@Override
	public UUID getId() {
		return mUniqueId;
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
	 * Renders the level
	 */
	public void render() {
		// TODO
	}


	/** Unique id for the level */
	private UUID mUniqueId = null;
	/** All actors in the level */
	private Vector<Actor> mActors = null;
	/** All triggers in the level */
	private Vector<Trigger> mTriggers = null;
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
		json.writeValue("mUniqueId", mUniqueId.toString());
		json.writeValue("mActors", mActors);
		json.writeValue("mTriggers", mTriggers);
		json.writeValue("mLevelDefId", mLevelDef.getId().toString());
		json.writeValue("mXCoord", mXCoord);
		json.writeValue("mSpeed", mSpeed);
		json.writeValue("mCompletedLevel", mCompletedLevel);
	}


	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#read(com.badlogic.gdx.utils.Json, com.badlogic.gdx.utils.OrderedMap)
	 */
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		mUniqueId = UUID.fromString(json.readValue("mUniqueId", String.class, jsonData));
		mXCoord = json.readValue("mXCoord", float.class, jsonData);
		mSpeed = json.readValue("mSpeed", float.class, jsonData);
		mCompletedLevel = json.readValue("mCompletedLevel", boolean.class, jsonData);

		// Actors
		Actor[] actors = json.readValue("mActors", Actor[].class, jsonData);
		mActors = new Vector<Actor>(Arrays.asList(actors));

		// Trigger
		Trigger[] triggers = json.readValue("mTriggers", Trigger[].class, jsonData);
		mTriggers = new Vector<Trigger>(Arrays.asList(triggers));

		// Get the actual LevelDef
		UUID levelDefId = UUID.fromString(json.readValue("mLevelDefId", String.class, jsonData));
		try {
			mLevelDef = ResourceCacheFacade.get(levelDefId, LevelDef.class);
		} catch (UndefinedResourceTypeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Default constructor, used for when loading levels.
	 */
	@SuppressWarnings("unused")
	private Level() {
		// Does nothing
	}
}
