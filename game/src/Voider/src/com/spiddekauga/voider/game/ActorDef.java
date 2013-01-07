package com.spiddekauga.voider.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.Textures;

/**
 * Definition of the actor. This include common attribute for a common type of actor.
 * E.g. A specific enemy will have the same variables here. The only thing changed during
 * it's life is the variables in the Actor class.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class ActorDef extends Def implements Json.Serializable, Disposable {
	/**
	 * Constructor that sets all variables
	 * @param maxLife maximum life of the actor, also starting amount of life
	 * @param textureTypes all the texture types that are used for the actor
	 * @param name name of the actor
	 * @param fixtureDef physical representation of the object
	 */
	public ActorDef(
			float maxLife,
			Textures.Types[] textureTypes,
			String name,
			FixtureDef fixtureDef
			)
	{
		setName(name);
		mMaxLife = maxLife;
		mTextureTypes = textureTypes;
		if (fixtureDef != null) {
			addFixtureDef(fixtureDef);
		}
		mBodyDef = new BodyDef();
	}

	/**
	 * Default constructor
	 */
	public ActorDef() {
	}

	/**
	 * Maximum life of the actor, usually starting amount of life
	 * @return maximum life
	 */
	public float getMaxLife() {
		return mMaxLife;
	}

	/**
	 * Checks how many textures this actor has
	 * @return number of textures
	 */
	public int getTextureCount() {
		return mTextureTypes == null ? 0 : mTextureTypes.length;
	}

	/**
	 * Gets the texture region (not the type) with the current id.
	 * @param index texture's index
	 * @return texture region if index was valid, null if index is out of bounds
	 */
	public TextureRegion getTextureRegion(int index) {
		if (index >= 0 && index < mTextureTypes.length) {
			return Textures.getTexture(mTextureTypes[index]);
		} else {
			return null;
		}
	}

	/**
	 * Adds another fixture definition
	 * @param fixtureDef
	 */
	public void addFixtureDef(FixtureDef fixtureDef) {
		mFixtureDefs.add(fixtureDef);
		setFilterCollisionData(fixtureDef);
	}

	/**
	 * Clears all existing fixtures
	 */
	public void clearFixtures() {
		for (FixtureDef fixtureDef : mFixtureDefs) {
			if (fixtureDef.shape != null) {
				fixtureDef.shape.dispose();
			}
		}

		mFixtureDefs.clear();
	}

	/**
	 * Returns the fixture definition. Includes shape, mass, etc.
	 * @return fixture definition.
	 */
	public final List<FixtureDef> getFixtureDefs() {
		return mFixtureDefs;
	}

	/**
	 * @return body definition of the actor
	 */
	public final BodyDef getBodyDef() {
		return mBodyDef;
	}

	/**
	 * @return a copy of the body definition
	 */
	public BodyDef getBodyDefCopy() {
		BodyDef copy = new BodyDef();
		copy.active = mBodyDef.active;
		copy.allowSleep = mBodyDef.allowSleep;
		copy.angularDamping = mBodyDef.angularDamping;
		copy.awake = mBodyDef.awake;
		copy.bullet = mBodyDef.bullet;
		copy.fixedRotation = mBodyDef.fixedRotation;
		copy.gravityScale = mBodyDef.gravityScale;
		copy.linearDamping = mBodyDef.linearDamping;
		copy.type = mBodyDef.type;
		return copy;
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Disposable#dispose()
	 */
	@Override
	public void dispose() {
		clearFixtures();
	}

	/**
	 * @return the filter category this actor belongs to
	 */
	protected abstract short getFilterCategory();

	/**
	 * @return the mask bit used for determening who the actor
	 * should collide with
	 */
	protected abstract short getFilterCollidingCategories();

	/**
	 * Sets the filter information based on derived information
	 * @param fixtureDef the fixture def to set the collision data for
	 */
	private void setFilterCollisionData(FixtureDef fixtureDef) {
		if (fixtureDef != null) {
			fixtureDef.filter.categoryBits = getFilterCategory();
			fixtureDef.filter.maskBits = getFilterCollidingCategories();
		}
	}


	/** Defines the mass, shape, etc. */
	private ArrayList<FixtureDef> mFixtureDefs = new ArrayList<FixtureDef>();
	/** Maximum life of the actor, usually starting amount of life */
	private float mMaxLife = 0;
	/** All textures for the actor */
	private Textures.Types[] mTextureTypes = null;
	/** The body definition of the actor */
	private BodyDef mBodyDef = null;

	/**
	 * @TODO weapon type
	 * @TODO move type (for enemies)
	 */

	/** For serialization */
	private static final long VERSION = 100;


	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#write(com.badlogic.gdx.utils.Json)
	 */
	@Override
	public void write(Json json) {
		json.writeValue("VERSION", VERSION);

		json.writeObjectStart("Def");
		super.write(json);
		json.writeObjectEnd();

		// Write ActorDef's variables first
		json.writeValue("mMaxLife", mMaxLife);
		json.writeValue("mTextureTypes", mTextureTypes);
		json.writeValue("mBodyDef", mBodyDef);
		json.writeValue("mFixtureDefs", mFixtureDefs);
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#read(com.badlogic.gdx.utils.Json, com.badlogic.gdx.utils.OrderedMap)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		long version = json.readValue("VERSION", long.class, jsonData);

		/** @TODO do something when another version... */
		if (version != VERSION) {
			//...
		}

		// Superclass
		OrderedMap<String, Object> superMap = json.readValue("Def", OrderedMap.class, jsonData);
		if (superMap != null) {
			super.read(json, superMap);
		}


		// Our variables
		mMaxLife = json.readValue("mMaxLife", float.class, jsonData);
		mTextureTypes = json.readValue("mTextureTypes", Textures.Types[].class, jsonData);
		mBodyDef = json.readValue("mBodyDef", BodyDef.class, jsonData);
		mFixtureDefs = json.readValue("mFixtureDefs", ArrayList.class, jsonData);
	}
}
