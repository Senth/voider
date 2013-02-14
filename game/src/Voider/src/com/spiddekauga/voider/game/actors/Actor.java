package com.spiddekauga.voider.game.actors;

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
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.game.ITriggerListener;
import com.spiddekauga.voider.game.TriggerAction;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;

/**
 * The abstract base class for all actors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Actor extends Resource implements ITriggerListener, Json.Serializable, Disposable, Poolable {
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
		// Update position
		if (mBody != null) {
			mPosition.set(mBody.getPosition());

			if (mEditorActive) {
				// Do we need to reload the body?
				if (mBodyUpdateTime <= getDef().getBodyChangeTime()) {
					reloadBody();
				}
				// Do we need to reload the fixtures?
				else if (mFixtureCreateTime <= getDef().getFixtureChangeTime()) {
					reloadFixtures();
				}
			}
		}

		// Decrease life if colliding with something...
		if (mDef.getMaxLife() > 0 && mLife > 0) {
			for (ActorDef collidingActor : mCollidingActors) {
				mLife -= collidingActor.getCollisionDamage() * deltaTime;
			}
		}

		// Do something if life is 0?
	}

	@Override
	public void onTriggered(TriggerAction action) {
		/** @TODO implement trigger responses */
	}

	/**
	 * @return the definition of the actor
	 */
	public ActorDef getDef() {
		return mDef;
	}

	/**
	 * Returns the definition of the specified type
	 * @param <DefType> derived definition type to get instead
	 * @param defType the derived definition type to get instead of default ActorDef
	 * @return if the actor's definition is an instance of type it will return this type
	 * instead, if not null is returned
	 */
	@SuppressWarnings("unchecked")
	public <DefType> DefType getDef(Class<DefType> defType) {
		if (mDef.getClass() == defType) {
			return (DefType)mDef;
		}
		return null;
	}

	/**
	 * Sets the definition of the actor.
	 * @param def the new definition of the actor, if null nothing happens
	 */
	public void setDef(ActorDef def) {
		if (def != null) {
			// Change fixtures as we have a new def now
			destroyFixtures();

			mDef = def;
			mLife = mDef.getMaxLife();

			if (mBody != null) {
				for (FixtureDef fixtureDef : mDef.getFixtureDefs()) {
					mBody.createFixture(fixtureDef);
				}
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
		setPosition(position.x, position.y);
	}

	/**
	 * Sets the position of the actor
	 * @param x x-coordinate of new position
	 * @param y y-coordinate of new position
	 */
	public void setPosition(float x, float y) {
		mPosition.set(x, y);

		// Change body if exist
		if (mBody != null) {
			mBody.setTransform(mPosition, mBody.getAngle());
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
	 * Sets the current player for all the actors
	 * @param playerActor the current player actor
	 */
	public static void setPlayerActor(PlayerActor playerActor) {
		mPlayerActor = playerActor;
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
			if (mSkipRotate) {
				bodyDef.angularVelocity = 0;
			}
			mBody = mWorld.createBody(bodyDef);
			for (FixtureDef fixtureDef : mDef.getFixtureDefs()) {
				if (fixtureDef != null && fixtureDef.shape != null) {
					setFilterCollisionData(fixtureDef);
					mBody.createFixture(fixtureDef);
				}
			}
			mBody.setUserData(this);

			mFixtureCreateTime = GameTime.getTotalGlobalTimeElapsed();
			mBodyUpdateTime = mFixtureCreateTime;
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
		mDisposed = true;
	}

	/**
	 * @return true if the actor has been disposed. I.e. it shall not be used anymore.
	 */
	public boolean isDisposed() {
		return mDisposed;
	}

	@Override
	public void reset() {
		if (mBody != null) {
			destroyBody();
		}
		mDisposed = false;
		mBody = null;
		mDef = null;
		mPosition.set(0,0);
		mCollidingActors.clear();
	}

	/**
	 * Reload fixtures. This destroys all the existing fixtures and creates
	 * new fixtures from the actor definition. Does nothing if the actor
	 * doesn't have a body.
	 * This also resets the body corners if we have any
	 */
	public void reloadFixtures() {
		if (mBody != null) {
			destroyFixtures();

			for (FixtureDef fixtureDef : mDef.getFixtureDefs()) {
				mBody.createFixture(fixtureDef);
			}

			// Do we have body corners? Reset those in that case
			if (mHasBodyCorners) {
				destroyBodyCorners();
				createBodyCorners();
			}

			if (mHasBodyCenter) {
				destroyBodyCenter();
				createBodyCenter();
			}

			mFixtureCreateTime = GameTime.getTotalGlobalTimeElapsed();
		}
	}

	/**
	 * @return true if this actor saves its def, i.e. #ResourceCacheFacade will not
	 * handle the def. This is true for terrain actors, as there is only one actor
	 * per definition, defaults to false.
	 */
	public boolean savesDef() {
		return false;
	}

	/**
	 * Checks what index the specified position has. Only applicable if actor is using a custom
	 * shape and in an editor
	 * @param position the position of a corner
	 * @return corner index if a corner was found at position, -1 if none was found
	 */
	public int getCornerIndex(Vector2 position) {
		for (int i = 0; i < mCorners.size(); ++i) {
			if (mCorners.get(i).getPosition().equals(position)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Creates a center picking body to move the center of the actor
	 */
	public void createBodyCenter() {
		if (mEditorActive) {
			mHasBodyCenter = true;
			if (mBody != null && mCenterBody == null) {
				mCenterBody = mWorld.createBody(new BodyDef());
				mCenterBody.createFixture(Editor.getPickingFixture());
				mCenterBody.setTransform(mPosition, 0);
				HitWrapper hitWrapper = new HitWrapper(this, "center");
				mCenterBody.setUserData(hitWrapper);
			}
		}
	}

	/**
	 * Destroys the center body
	 */
	public void destroyBodyCenter() {
		mHasBodyCenter = false;
		if (mCenterBody != null) {
			mCenterBody.getWorld().destroyBody(mCenterBody);
			mCenterBody = null;
		}
	}

	/**
	 * @return true if the actor has a body center
	 */
	public boolean hasBodyCenter() {
		return mHasBodyCenter;
	}

	/**
	 * Creates body corners for the actor. Only applicable if actor is using a custom shape
	 * and in an editor.
	 */
	public void createBodyCorners() {
		if (mDef.getShapeType() == ActorShapeTypes.CUSTOM && mEditorActive) {
			mHasBodyCorners = true;
			Vector2 worldPos = Pools.obtain(Vector2.class);
			for (Vector2 localPos : mDef.getCorners()) {
				worldPos.set(localPos).add(mPosition).add(getDef().getCenterOffset());
				createBodyCorner(worldPos);
			}
			Pools.free(worldPos);
		}
	}

	/**
	 * Destroys all body corners
	 */
	public void destroyBodyCorners() {
		mHasBodyCorners = false;
		for (Body body : mCorners) {
			body.getWorld().destroyBody(body);
		}
		mCorners.clear();
	}

	/**
	 * Sets if the actor shall rotate on its own
	 * @param skipRotating set to true to skip making the actor rotate by its own
	 */
	public void setSkipRotating(boolean skipRotating) {
		mSkipRotate = skipRotating;

		if (mBody != null) {
			if (mSkipRotate) {
				mBody.setAngularVelocity(0);
			} else {
				mBody.setAngularVelocity(mDef.getRotationSpeed());
			}
		}
	}

	/**
	 * @return true if the actor is currently skipping rotation
	 */
	public boolean isSkippingRotation() {
		return mSkipRotate;
	}


	/**
	 * @return true if it currently is drawing body corners
	 */
	public boolean hasBodyCorners() {
		return mHasBodyCorners;
	}

	/**
	 * Protected constructor
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

		mFixtureCreateTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * Clears all fixtures, both from the actor and from the definition...
	 */
	protected void clearFixturesDefs() {
		destroyFixtures();

		if (mDef != null) {
			mDef.clearFixtures();
		}
	}

	/**
	 * Destroys all fixtures from the body
	 */
	protected void destroyFixtures() {
		if (mBody != null) {
			@SuppressWarnings("unchecked")
			ArrayList<Fixture> fixtures = (ArrayList<Fixture>) mBody.getFixtureList().clone();
			for (Fixture fixture : fixtures) {
				mBody.destroyFixture(fixture);
			}
		}
	}

	/**
	 * @return the filter category this actor belongs to
	 */
	protected abstract short getFilterCategory();

	/**
	 * @return the mask bit used for determining who the actor
	 * can collide with
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

	/**
	 * Sets the angular dampening on the current body
	 */
	private void reloadBody() {
		if (!mSkipRotate) {
			mBody.setAngularVelocity(mDef.getRotationSpeed());
		}

		mBodyUpdateTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * Creates a body for the specific point at the specific index
	 * @param corner the corner to create a body for
	 */
	private void createBodyCorner(Vector2 corner) {
		Body body = mWorld.createBody(new BodyDef());
		body.createFixture(Config.Editor.getPickingFixture());
		body.setTransform(corner, 0);
		HitWrapper hitWrapper = new HitWrapper(this, "corner");
		body.setUserData(hitWrapper);
		mCorners.add(body);
	}

	/** Current life */
	protected float mLife = 0;

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
	/** World corners of the actor, only used for custom shape and in an editor */
	private ArrayList<Body> mCorners = new ArrayList<Body>();
	/** Center body, this represents the center of the actor */
	private Body mCenterBody = null;
	/** True if the actor has a center body */
	private boolean mHasBodyCenter = false;
	/** Global time when we last created the fixtures */
	private float mFixtureCreateTime = 0;
	/** Global time when we last created the fixtures */
	private float mBodyUpdateTime = 0;
	/** True if the actor shall create body corners */
	private boolean mHasBodyCorners = false;
	/** True if the actor shall skip rotating */
	private boolean mSkipRotate = false;

	/** The world used for creating bodies */
	protected static World mWorld = null;
	/** If the actor will be used for an editor */
	protected static boolean mEditorActive = false;
	/** The player of this game, for derived actor to have easy access */
	protected static PlayerActor mPlayerActor = null;
}
