package com.spiddekauga.voider.game;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.StaticTerrainActorDef;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;

/**
 * The abstract base class for all actors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Actor extends Resource implements ITriggerListener, Json.Serializable, Disposable {
	/**
	 * Sets the texture of the actor including the actor definition.
	 * Automatically creates a body for the actor.
	 * @param def actor definition
	 */
	public Actor(ActorDef def) {
		mDef = def;
		mLife = def.getMaxLife();
		mUniqueId = UUID.randomUUID();
	}

	/**
	 * Updates the actor
	 * @param deltaTime seconds elapsed since last call
	 */
	public void update(float deltaTime) {
		// Decrease life if colliding with something...
		if (mDef.getMaxLife() > 0 && mLife > 0) {
			for (ActorDef collidingActor : mCollidingActors) {
				mLife -= collidingActor.getCollisionDamage() * deltaTime;
			}
		}

		// Do something if life is 0?
	}

	/**
	 * @return the definition of the actor
	 */
	public ActorDef getDef() {
		return mDef;
	}

	/**
	 * Sets the definition of the actor.
	 * @param def the new definition of the actor, if null nothing happens
	 */
	public void setDef(ActorDef def) {
		if (def != null) {
			mDef = def;
			mLife = mDef.getMaxLife();

			// Change fixtures as we have a new def now
			clearFixtures();
			for (FixtureDef fixtureDef : mDef.getFixtureDefs()) {
				mBody.createFixture(fixtureDef);
			}
		}
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
	 * Adds a colliding actor to this actor
	 * @param actorDef the actor definition this actor is colliding with
	 */
	public void addCollidingActor(ActorDef actorDef) {
		mCollidingActors.add(actorDef);
	}

	/**
	 * Removes a colliding actor from this actor
	 * @param actorDef the actor definition this actor is colliding with, but to now remove
	 */
	public void removeCollidingActor(ActorDef actorDef) {
		boolean removeSuccess = mCollidingActors.remove(actorDef);
		if (!removeSuccess) {
			Gdx.app.error("Actor", "Could not find colliding actor to remove");
		}
	}

	/**
	 * @return current life of the actor
	 */
	public float getLife() {
		return mLife;
	}

	@Override
	public void write(Json json) {
		super.write(json);

		json.writeValue("REVISION", Config.REVISION);
		json.writeValue("mLife", mLife);
		json.writeValue("mPosition", mPosition);


		if (savesDef()) {
			json.writeValue("mDef", mDef);
		} else {
			json.writeValue("mDefId", mDef.getId());
			json.writeValue("mDefType", mDef.getClass().getName());
		}

		/** @TODO Do we need to save colliding actors? */

		if (mBody != null) {
			json.writeObjectStart("mBody");
			json.writeValue("angle", mBody.getAngle());
			json.writeValue("angular_velocity", mBody.getAngularVelocity());
			json.writeValue("linear_velocity", mBody.getLinearVelocity());
			json.writeValue("awake", mBody.isAwake());
			json.writeValue("active", mBody.isActive());
			json.writeObjectEnd();
		} else {
			json.writeValue("mBody", (String) null);
		}
	}

	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		super.read(json, jsonData);

		mLife = json.readValue("mLife", float.class, jsonData);
		mPosition = json.readValue("mPosition", Vector2.class, jsonData);


		// Definition
		if (savesDef()) {
			mDef = json.readValue("mDef", StaticTerrainActorDef.class, jsonData);
		}
		// Get definition information to be able to load it
		else {
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
		}


		// Create stub body
		BodyDef bodyDef = mDef.getBodyDefCopy();

		// Set body information, i.e. position etc.
		OrderedMap<?, ?> bodyMap = json.readValue("mBody", OrderedMap.class, jsonData);
		if (bodyMap != null) {
			bodyDef.angle = json.readValue("angle", float.class, bodyMap);
			bodyDef.angularVelocity = json.readValue("angular_velocity", float.class, bodyMap);
			bodyDef.linearVelocity.set(json.readValue("linear_velocity", Vector2.class, bodyMap));
			bodyDef.awake = json.readValue("awake", boolean.class, bodyMap);
			bodyDef.active = json.readValue("active", boolean.class, bodyMap);

			// Set position
			bodyDef.position.set(mPosition);
		}

		if (mWorld != null) {
			createBody(bodyDef);
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
		mPosition.set(position);

		// Change body if exist
		if (mBody != null) {
			mBody.setTransform(position, mBody.getAngle());
		}
	}

	/**
	 * @return current position of the actor
	 */
	public Vector2 getPosition() {
		return mPosition;
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
	 * any angle, velocity set to it. Position is however set
	 */
	public void createBody(){
		BodyDef bodyDef = mDef.getBodyDefCopy();
		bodyDef.position.set(mPosition);
		createBody(bodyDef);
	}

	/**
	 * Creates a new body out of the fixture definition and the specified body definition
	 * @param bodyDef the body definition to use for the body
	 */
	public void createBody(BodyDef bodyDef) {
		if (mWorld != null && mBody == null) {
			mBody = mWorld.createBody(bodyDef);
			for (FixtureDef fixtureDef : mDef.getFixtureDefs()) {
				if (fixtureDef != null && fixtureDef.shape != null) {
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
	 * Disables the actor fully. Removes the body from the world and a level will
	 * not render it as it has been disposed.
	 */
	@Override
	public void dispose() {
		if (mBody != null) {
			destroyBody();
		}
		Pools.free(mPosition);
		mPosition = null;
		mDisposed = true;
	}

	/**
	 * @return true if the actor has been disposed. I.e. it shall not be used anymore.
	 */
	public boolean isDisposed() {
		return mDisposed;
	}

	/**
	 * @return true if this actor saves its def, i.e. #ResourceCacheFacade will not
	 * handle the def. This is true for terrain actors, as there is only one actor
	 * per definition, defaults to false.
	 */
	protected boolean savesDef() {
		return false;
	}

	/**
	 * Protected constructor, used for JSON
	 */
	protected Actor() {
		mUniqueId = UUID.randomUUID();
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

	/** Current life */
	protected float mLife = 0.0f;

	/** Physical body */
	private Body mBody = null;
	/** Sprite, i.e. the graphical representation */
	private Sprite mSprite = null;
	/** The belonging definition of this actor */
	private ActorDef mDef = null;
	/** Body position, remember even when we don't have a body */
	private Vector2 mPosition = Pools.obtain(Vector2.class).set(0, 0);
	/** Current actors we're colliding with */
	private ArrayList<ActorDef> mCollidingActors = new ArrayList<ActorDef>();
	/** True if the actor has been disposed of and is invalid to use */
	private boolean mDisposed = false;

	/** The world used for creating bodies */
	protected static World mWorld = null;
	/** If the actor will be used for an editor */
	protected static boolean mEditorActive = false;
}
