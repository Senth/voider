package com.spiddekauga.voider.game;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;

/**
 * The abstract base class for all actors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Actor implements ITriggerListener, Json.Serializable {
	/**
	 * Sets the texture of the actor including the actor definition.
	 * Automatically creates a body for the actor.
	 * @param def actor definition
	 */
	public Actor(ActorDef def) {
		if (def.getTextureCount() > 0) {
			TextureRegion region = def.getTextureRegion(0);
			if (region != null) {
				mSprite = new Sprite(region);
			}
		}

		mDef = def;
		mLife = def.getMaxLife();

		createBody();
	}

	/**
	 * Updates the actor
	 * @param deltaTime seconds elapsed since last call
	 */
	public abstract void update(float deltaTime);

	/**
	 * @return the definition of the actor
	 */
	public ActorDef getDef() {
		return mDef;
	}

	/**
	 * Renders the actor
	 * @param spriteBatch the current sprite batch for the scene
	 */
	@SuppressWarnings("unused")
	public void render(SpriteBatch spriteBatch) {
		if (mSprite != null && !Config.Graphics.USE_DEBUG_RENDERER) {
			mSprite.draw(spriteBatch);
		}
	}

	/**
	 * @return current life of the actor
	 */
	public float getLife() {
		return mLife;
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#write(com.badlogic.gdx.utils.Json)
	 */
	@Override
	public void write(Json json) {
		json.writeValue("VERSION", VERSION);
		json.writeValue("mLife", mLife);
		json.writeValue("mDefId", mDef.getId().toString());
		json.writeValue("mDefType", mDef.getClass().getName());


		if (mBody != null) {
			json.writeObjectStart("mBody");
			json.writeValue("angle", mBody.getAngle());
			json.writeValue("angular_velocity", mBody.getAngularVelocity());
			json.writeValue("linear_velocity", mBody.getLinearVelocity());
			json.writeValue("position", mBody.getPosition());
			json.writeValue("awake", mBody.isAwake());
			json.writeValue("active", mBody.isActive());
			json.writeObjectEnd();
		} else {
			json.writeValue("mBody", (String) null);
		}
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#read(com.badlogic.gdx.utils.Json, com.badlogic.gdx.utils.OrderedMap)
	 */
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		/** @TODO check version */

		mLife = json.readValue("mLife", float.class, jsonData);


		// Get definition information to be able to load it
		UUID defId = UUID.fromString(json.readValue("mDefId", String.class, jsonData));
		String defTypeName = json.readValue("mDefType", String.class, jsonData);
		Class<?> defType = null;
		try {
			defType = Class.forName(defTypeName);
		} catch (ClassNotFoundException e) {
			Gdx.app.error("JsonRead", "Class not found for class: " + defTypeName);
			throw new GdxRuntimeException(e);
		}


		// Set the actual actor definition
		try {
			mDef = (ActorDef) ResourceCacheFacade.get(defId, defType);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("JsonRead", "Undefined Resource Type exception!");
			throw new GdxRuntimeException(e);
		}


		// Create stub body
		BodyDef bodyDef = mDef.getBodyDefCopy();

		// Set body information, i.e. position etc.
		OrderedMap<?, ?> bodyMap = json.readValue("mBody", OrderedMap.class, jsonData);
		if (bodyMap != null) {
			bodyDef.angle = json.readValue("angle", float.class, bodyMap);
			bodyDef.angularVelocity = json.readValue("angular_velocity", float.class, bodyMap);
			bodyDef.linearVelocity.set(json.readValue("linear_velocity", Vector2.class, bodyMap));
			bodyDef.position.set(json.readValue("position", Vector2.class, bodyMap));
			bodyDef.awake = json.readValue("awake", boolean.class, bodyMap);
			bodyDef.active = json.readValue("active", boolean.class, bodyMap);
		} else {
			Gdx.app.error("JsonRead", "Could not find body map");
			throw new GdxRuntimeException("Could not find body map in json string");
		}

		if (mWorld != null) {
			mBody = mWorld.createBody(bodyDef);
		} else {
			Gdx.app.error("JsonRead", "World was null when creating body");
			throw new GdxRuntimeException("World was null when creating body from json");
		}
	}

	/**
	 * Sets the world that shall be used for creating new bodies
	 * @param world the new world
	 */
	public static void setWorld(World world) {
		mWorld = world;
	}

	/**
	 * Protected constructor used for classes to create an empty actor
	 */
	protected Actor() {
		// Does nothing
	}

	/**
	 * Creates a new body out of the fixture and body definition. This body will however not have
	 * any angle, velocity, or position set to it.
	 */
	private void createBody(){
		if (mWorld != null) {
			BodyDef bodyDef = mDef.getBodyDef();
			mBody = mWorld.createBody(bodyDef);
			mBody.createFixture(mDef.getFixtureDef());
		}
	}

	/** Physical body */
	private Body mBody = null;
	/** Current life */
	private float mLife = 0.0f;
	/** Sprite, i.e. the graphical representation */
	private Sprite mSprite = null;
	/** The belonging definition of this actor */
	private ActorDef mDef = null;
	/** The world used for creating bodies */
	private static World mWorld = null;

	/** Current version of this actor, used for reading/writing to json */
	private static final long VERSION = 100;
}
