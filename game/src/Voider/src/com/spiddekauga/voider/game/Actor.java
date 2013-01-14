package com.spiddekauga.voider.game;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;

/**
 * The abstract base class for all actors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Actor extends Resource implements ITriggerListener, Json.Serializable {
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
		mUniqueId = UUID.randomUUID();
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
	 * Renders additional information when using an editor
	 * @param spriteBatch the current sprite batch for the scene
	 */
	public void renderEditor(SpriteBatch spriteBatch) {
		// Does nothing
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
		super.write(json);

		json.writeValue("VERSION", VERSION);
		json.writeValue("mLife", mLife);
		json.writeValue("mDefId", mDef.getId());
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
		super.read(json, jsonData);

		/** @TODO check version */

		mLife = json.readValue("mLife", float.class, jsonData);

		// Get definition information to be able to load it
		UUID defId = json.readValue("mDefId", UUID.class, jsonData);
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
		}

		if (mWorld != null) {
			mBody = mWorld.createBody(bodyDef);
			/** @TODO Create fixture? */
		} else {
			Gdx.app.error("JsonRead", "World was null when creating body");
			throw new GdxRuntimeException("World was null when creating body from json");
		}
	}

	/**
	 * Sets the position of the actor
	 * @param position the new position
	 */
	public void setPosition(Vector2 position) {
		mDef.getBodyDef().position.set(position);

		// Change body if exist
		if (mBody != null) {
			mBody.setTransform(position, mBody.getAngle());
		}
	}

	/**
	 * @return current position of the actor
	 */
	public Vector2 getPosition() {
		return mDef.getBodyDef().position;
	}

	/**
	 * Sets the world that shall be used for creating new bodies
	 * @param world the new world
	 */
	public static void setWorld(World world) {
		mWorld = world;
	}

	/**
	 * Sets if the editor will be active for all the actors
	 * @param editorActive true if the editor will be active
	 */
	public static void setEditorActive(boolean editorActive) {
		mEditorActive = editorActive;
	}

	/**
	 * @return the body of the actor
	 */
	public Body getBody() {
		return mBody;
	}

	/**
	 * Creates a new body out of the fixture and body definition. This body will however not have
	 * any angle, velocity, or position set to it.
	 */
	public void createBody(){
		if (mWorld != null && mBody == null) {
			BodyDef bodyDef = mDef.getBodyDef();
			mBody = mWorld.createBody(bodyDef);
			for (FixtureDef fixtureDef : mDef.getFixtureDefs()) {
				if (fixtureDef.shape != null) {
					mBody.createFixture(fixtureDef);
				}
			}
			mBody.setUserData(this);
		}
	}

	/**
	 * Destroys the body of the actor. This will remove it from the world
	 */
	public void destroyBody() {
		if (mBody != null) {
			mBody.getWorld().destroyBody(mBody);
			mBody = null;
		}
	}

	/**
	 * Protected constructor, used for JSON
	 */
	protected Actor() {
		// Does nothing
	}

	/**
	 * Adds a fixture to the body and fixture list
	 * @param fixtureDef the fixture to add
	 */
	protected void addFixture(FixtureDef fixtureDef) {
		mDef.addFixtureDef(fixtureDef);
		mBody.createFixture(fixtureDef);
	}

	/**
	 * Clears all fixtures
	 */
	@SuppressWarnings("unchecked")
	protected void clearFixtures() {
		ArrayList<Fixture> fixtures = (ArrayList<Fixture>) mBody.getFixtureList().clone();
		for (Fixture fixture : fixtures) {
			mBody.destroyFixture(fixture);
		}

		mDef.clearFixtures();
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
	protected static World mWorld = null;
	/** If the actor will be used for an editor */
	protected static boolean mEditorActive = false;

	/** Current version of this actor, used for reading/writing to json */
	private static final long VERSION = 100;
}
