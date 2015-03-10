package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.kryo.KryoPreWrite;
import com.spiddekauga.utils.kryo.KryoTaggedCopyable;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.HealthChangeEvent;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.triggers.ITriggerListener;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.game.triggers.TriggerAction;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceBody;
import com.spiddekauga.voider.resources.IResourceChangeListener.EventTypes;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.voider.resources.IResourceEditorRender;
import com.spiddekauga.voider.resources.IResourceEditorUpdate;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.resources.IResourceRenderShape;
import com.spiddekauga.voider.resources.IResourceRenderSprite;
import com.spiddekauga.voider.resources.IResourceSelectable;
import com.spiddekauga.voider.resources.IResourceUpdate;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.GameEvent;

/**
 * The abstract base class for all actors
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class Actor extends Resource implements IResourceUpdate, KryoTaggedCopyable, KryoSerializable, Disposable, Poolable, IResourceBody,
		IResourcePosition, ITriggerListener, IResourceEditorUpdate, IResourceRenderShape, IResourceRenderSprite, IResourceEditorRender,
		IResourceSelectable, IResourceCorner, KryoPreWrite {
	/**
	 * Sets the texture of the actor including the actor definition. Automatically creates
	 * a body for the actor.
	 * @param def actor definition
	 */
	public Actor(ActorDef def) {
		mDef = def;
		mLife = def.getHealthMax();
		mUniqueId = UUID.randomUUID();
	}

	/**
	 * Updates the actor. This automatically calls #editorUpdate()
	 * @param deltaTime seconds elapsed since last call
	 */
	@Override
	public void update(float deltaTime) {
		if (mDestroyBody) {
			mActive = false;
			destroyBody();
			mDestroyBody = false;
		}

		if (mBody != null) {
			mPosition.set(mBody.getPosition());
		}

		if (mActive) {
			// Update position
			if (mBody != null) {
				calculateRotatedVertices();

				// Rotation
				if (!mSkipRotate && mDef.getBodyDef().angularVelocity != 0 && !mDef.getVisual().getCenterOffset().equals(Vector2.Zero)) {
					float newAngle = mBody.getAngle();
					newAngle += mDef.getBodyDef().angularVelocity * deltaTime;
					if (newAngle >= MathUtils.PI2) {
						newAngle -= MathUtils.PI2;
					} else if (newAngle <= -MathUtils.PI2) {
						newAngle += MathUtils.PI2;
					}
					mBody.setTransform(mPosition, newAngle);
				}
			}

			// Decrease life if colliding with something...
			if (mDef.getHealthMax() > 0 && mLife > 0) {
				for (Actor collidingActor : mCollidingActors.keySet()) {
					decreaseHealth(collidingActor.getDef().getCollisionDamage() * deltaTime);
				}
			}
		}
	}

	/**
	 * Updates the actor's body position, fixture sizes, fixture shapes etc. if they have
	 * been changed since the actor was created.
	 */
	@Override
	public void updateEditor() {
		if (mEditorActive && mBody != null) {
			calculateRotatedVertices();

			// Do we need to reload the body?
			if (mBodyUpdateTime <= getDef().getBodyChangeTime()) {
				reloadBody();
			}
			// Do we need to reload the fixtures?
			if (mFixtureCreateTime <= getDef().getVisual().getFixtureChangeTime()) {
				reloadFixtures();
				calculateRotatedVertices(true);
			}

			// Do we need to fix the body corners?
			if (hasBodyCorners() && mCorners != null && mCorners.size() != mDef.getVisual().getCornerCount()) {
				destroyBodyCorners();
				createBodyCorners();
			}
		}
	}

	@Override
	public void onTriggered(TriggerAction action) {
		if (action.action == Actions.ACTOR_ACTIVATE) {
			activate();
		} else if (action.action == Actions.ACTOR_DEACTIVATE) {
			deactivate();
		}

	}

	@Override
	public ArrayList<TriggerInfo> getTriggerInfos() {
		return mTriggerInfos;
	}

	/**
	 * Adds a trigger to the actor
	 * @param triggerInfo trigger information
	 */
	@Override
	public void addTrigger(TriggerInfo triggerInfo) {
		triggerInfo.listener = this;
		triggerInfo.trigger.addListener(triggerInfo);
		mTriggerInfos.add(triggerInfo);
	}

	/**
	 * Removes the specified trigger from this actor
	 * @param triggerInfo trigger information
	 */
	@Override
	public void removeTrigger(TriggerInfo triggerInfo) {
		triggerInfo.trigger.removeListener(getId());
		mTriggerInfos.remove(triggerInfo);
	}

	@Override
	@Deprecated
	public boolean addBoundResource(IResource boundResource) {
		Config.Debug.deprecatedException();

		boolean success = super.addBoundResource(boundResource);

		// Find all listeners for this actor. Then check for trigger info
		// that isn't in this actors trigger list; that should be the added
		// trigger
		if (boundResource instanceof Trigger) {
			ArrayList<TriggerInfo> thisListeners = new ArrayList<TriggerInfo>();

			// Find all listeners for this actor
			for (TriggerInfo triggerInfo : ((Trigger) boundResource).getListeners()) {
				if (triggerInfo.listener == this) {
					thisListeners.add(triggerInfo);
				}
			}

			// Is a trigger info missing?
			boolean foundMissing = false;
			for (TriggerInfo fromTrigger : thisListeners) {
				if (!mTriggerInfos.contains(fromTrigger)) {
					mTriggerInfos.add(fromTrigger);
					foundMissing = true;
				}
			}

			if (foundMissing) {
				success = true;
			} else {
				Gdx.app.error("Actor", "Didn't find the missing trigger when readding it");
			}
		}

		return success;
	}

	@Override
	public boolean removeBoundResource(IResource boundResource) {
		boolean success = super.removeBoundResource(boundResource);

		// Find and remove the trigger
		if (boundResource instanceof Trigger) {
			Iterator<TriggerInfo> iterator = mTriggerInfos.iterator();
			while (iterator.hasNext()) {
				TriggerInfo triggerInfo = iterator.next();
				if (triggerInfo.trigger == boundResource) {
					iterator.remove();
					success = true;
				}
			}
		}

		return success;
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
	 *         instead, if not null is returned
	 */
	@SuppressWarnings("unchecked")
	public <DefType> DefType getDef(Class<DefType> defType) {
		if (mDef.getClass() == defType) {
			return (DefType) mDef;
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
			mLife = mDef.getHealthMax();

			if (mBody != null) {
				reloadBody();
				createFixtures();
			}
			calculateRotatedVertices(true);
		}
	}

	/**
	 * @return actor offset, i.e. local to world coordinates.
	 */
	public Vector2 getWorldOffset() {
		Vector2 offsetPosition = new Vector2();
		offsetPosition.set(mPosition);

		// Offset for circle
		if (mDef.getVisual().getShapeType() == ActorShapeTypes.CUSTOM && mDef.getVisual().getCornerCount() >= 1
				&& mDef.getVisual().getCornerCount() <= 2) {
			offsetPosition.add(mDef.getVisual().getCorners().get(0));
		}

		offsetPosition.add(mDef.getVisual().getCenterOffset());

		return offsetPosition;
	}

	@Override
	public void renderShape(ShapeRendererEx shapeRenderer) {
		// Skip if the actor is rendered with sprites
		if (mDef.getVisual().hasImage()) {
			return;
		}


		RenderOrders.offsetZValue(shapeRenderer, this);

		Vector2 offsetPosition = getWorldOffset();

		// Draw regular filled shape
		if (!mDrawOnlyOutline && mDef.getVisual().isComplete()) {
			if (mRotatedVertices != null) {
				shapeRenderer.setColor(mDef.getVisual().getColor());
				shapeRenderer.triangles(mRotatedVertices, offsetPosition);
			}
		}
		// Draw outline
		else if (mDef.getVisual().getCornerCount() >= 2) {
			shapeRenderer.push(ShapeType.Line);

			if (mDef.getVisual().getCornerCount() == 2) {
				offsetPosition.sub(mDef.getVisual().getCorners().get(0));
				offsetPosition.sub(mDef.getVisual().getCenterOffset());
			}

			shapeRenderer.setColor(Config.Actor.OUTLINE_COLOR);
			shapeRenderer.polyline(mDef.getVisual().getCorners(), false, offsetPosition);

			// Close the shape
			if (mDef.getVisual().getCornerCount() >= 3) {
				shapeRenderer.setColor(Config.Actor.OUTLINE_CLOSE_COLOR);
				shapeRenderer.line(mDef.getVisual().getCorners().get(mDef.getVisual().getCornerCount() - 1), mDef.getVisual().getCorners().get(0),
						offsetPosition);
			}

			shapeRenderer.pop();
		}

		RenderOrders.resetZValueOffset(shapeRenderer, this);
	}

	@Override
	public void renderSprite(SpriteBatch spriteBatch) {
		Sprite sprite = mDef.getVisual().getImage(mPosition);

		if (sprite != null) {
			sprite.draw(spriteBatch);
		}
	}

	/**
	 * Renders additional information when using an editor
	 * @param shapeRenderer the current sprite batch for the scene
	 */
	@Override
	public void renderEditor(ShapeRendererEx shapeRenderer) {
		if (mBody == null) {
			return;
		}

		RenderOrders.offsetZValueEditor(shapeRenderer, this);

		Vector2 offsetPosition = getWorldOffset();

		// Draw selected outline
		if (!mDrawOnlyOutline && mSelected && getDef().getVisual().isComplete() && mRotatedVertices != null) {
			if (mSelectedOutline == null) {
				reloadSelectedOutline();
			}

			if (mSelectedOutline != null) {
				Color color = SkinNames.getResource(SkinNames.EditorVars.SELECTED_COLOR_ACTOR);
				shapeRenderer.setColor(color);
				shapeRenderer.triangles(mSelectedOutline, offsetPosition);
			}
		}


		// Draw corners?
		RenderOrders.offsetZValue(shapeRenderer);
		if (mHasBodyCorners) {
			shapeRenderer.push(ShapeType.Line);

			shapeRenderer.setColor(Config.Editor.CORNER_COLOR);
			Vector2 cornerOffset = new Vector2();
			for (Vector2 corner : mDef.getVisual().getCorners()) {
				cornerOffset.set(offsetPosition).add(corner);
				shapeRenderer.polyline(SceneSwitcher.getPickingVertices(), true, cornerOffset);
			}
			cornerOffset = null;

			shapeRenderer.pop();
		}


		// Draw center body?
		RenderOrders.offsetZValue(shapeRenderer);
		if (mHasBodyCenter) {
			shapeRenderer.push(ShapeType.Line);

			if (mDef.getVisual().getCornerCount() > 0 && mDef.getVisual().getCornerCount() <= 2) {
				offsetPosition.sub(mDef.getVisual().getCorners().get(0));
			}
			offsetPosition.sub(mDef.getVisual().getCenterOffset());
			shapeRenderer.setColor(Config.Editor.CENTER_OFFSET_COLOR);
			shapeRenderer.polyline(SceneSwitcher.getPickingVertices(), true, offsetPosition);

			shapeRenderer.pop();
		}
		RenderOrders.resetZValueOffset(shapeRenderer);
		RenderOrders.resetZValueOffset(shapeRenderer);

		RenderOrders.resetZValueOffsetEditor(shapeRenderer, this);
	}

	/**
	 * Reload selected outline
	 */
	private void reloadSelectedOutline() {
		if (mEditorActive) {
			clearSelectedOutline();

			ArrayList<Vector2> corners = copyVectorArray(getDef().getVisual().getPolygonShape());
			if (corners != null && !corners.isEmpty()) {
				float width = SkinNames.getResource(SkinNames.EditorVars.SELECTED_OUTLINE_WIDTH);
				ArrayList<Vector2> outerCorners = Geometry.createdBorderCorners(corners, true, width);
				mSelectedOutline = Geometry.createBorderVertices(corners, outerCorners);

				if (mSelectedOutline != null) {
					// Rotate outline
					float rotation = getDef().getBodyDef().angle;
					if (getBody() != null) {
						rotation = getBody().getAngle();
					}
					rotation *= MathUtils.radDeg;

					Geometry.rotateVertices(mSelectedOutline, rotation, true, getDef().getVisual().getCenterOffset());
				}
			}
		}
	}

	/**
	 * Clear selected outline
	 */
	private void clearSelectedOutline() {
		mSelectedOutline = null;
	}

	/**
	 * Adds a colliding actor to this actor
	 * @param actor the actor this actor is colliding with
	 */
	public void addCollidingActor(Actor actor) {
		AtomicInteger count = mCollidingActors.get(actor);

		// New actor
		if (count == null) {
			count = new AtomicInteger(1);
			mCollidingActors.put(actor, count);
		}
		// Increase count
		else {
			count.incrementAndGet();
		}
	}

	/**
	 * Removes a colliding actor from this actor
	 * @param actor the actor this actor is colliding with, but to now remove
	 */
	public void removeCollidingActor(Actor actor) {
		AtomicInteger count = mCollidingActors.get(actor);

		if (count != null) {
			int countValue = count.decrementAndGet();
			if (countValue == 0) {
				mCollidingActors.remove(actor);
			}
		} else {
			Gdx.app.error("Actor", "Could not find colliding actor to remove");
		}
	}

	/**
	 * @return current life of the actor
	 */
	public float getHealth() {
		return mLife;
	}

	/**
	 * Resets the life
	 */
	public void resetHealth() {
		float oldLife = mLife;
		mLife = getDef().getHealthMax();
		sendChangeHealthEvent(oldLife);
	}

	/**
	 * Decrease the actor's life with the specified amount
	 * @param amount the amount to to decrease the actor's life with
	 */
	public void decreaseHealth(float amount) {
		if (mLife > 0) {
			float oldLife = mLife;
			mLife -= amount;
			sendChangeEvent(EventTypes.LIFE_DECREASED);
			sendChangeHealthEvent(oldLife);
			if (this instanceof PlayerActor && mLife <= 0) {
				mEventDispatcher.fire(new GameEvent(com.spiddekauga.voider.utils.event.EventTypes.GAME_PLAYER_SHIP_LOST));
			}
		}
	}

	/**
	 * Increases the actor's life with the specified amount
	 * @param amount the amount to increase
	 */
	public void increaseHealth(float amount) {
		float oldLife = mLife;
		mLife += amount;

		// Crop
		if (mLife > getDef().getHealthMax()) {
			mLife = getDef().getHealthMax();
		}

		sendChangeHealthEvent(oldLife);
	}

	/**
	 * Send an change life event if the life was changed
	 * @param oldLife
	 */
	private void sendChangeHealthEvent(float oldLife) {
		if (mLife != oldLife) {
			mEventDispatcher.fire(new HealthChangeEvent(this, oldLife));
		}
	}

	/**
	 * Sets if the actor shall only draw its outline
	 * @param drawOnlyOutline set to true if the actor shall only draw its shape's
	 *        outline.
	 */
	public void setDrawOnlyOutline(boolean drawOnlyOutline) {
		mDrawOnlyOutline = drawOnlyOutline;
	}

	/**
	 * @return true if only the outline of the actor is drawn
	 */
	public boolean isDrawOnlyOutline() {
		return mDrawOnlyOutline;
	}

	@Override
	public void preWrite() {
		mClassRevision = CLASS_REVISION;
	}

	@Override
	public void write(Kryo kryo, Output output) {
		// Saves active state?
		output.writeBoolean(mEditorActive);
		if (!mEditorActive) {
			output.writeBoolean(mActive);
		}

		// Save def or just fetch it
		if (savesDef()) {
			kryo.writeClassAndObject(output, mDef);
		} else {
			kryo.writeObject(output, mDef.getId());
		}

		// Save body
		if (!mEditorActive) {
			output.writeBoolean(mBody != null);
			if (mBody != null) {
				output.writeFloat(mBody.getAngle());
				output.writeFloat(mBody.getAngularVelocity());
				kryo.writeObject(output, mBody.getLinearVelocity());
				output.writeBoolean(mBody.isAwake());
				output.writeBoolean(mBody.isActive());
			}
		}
	}

	@Override
	public void read(Kryo kryo, Input input) {
		if (mClassRevision == 0) {
			// Read old class revision which never was used
			mClassRevision = input.readInt(true);
		}

		// Load active state
		boolean editorWasActive = input.readBoolean();
		if (!editorWasActive) {
			mActive = input.readBoolean();
		}

		// Saves def or just fetch it
		if (savesDef()) {
			Object object = kryo.readClassAndObject(input);
			if (object instanceof ActorDef) {
				mDef = (ActorDef) object;
			} else {
				Gdx.app.error("Actor", "Def read from kryo was not an ActorDef!");
			}
		} else {
			UUID defId = kryo.readObject(input, UUID.class);
			mDef = ResourceCacheFacade.get(defId);
		}

		// Read body
		if (!editorWasActive) {
			if (input.readBoolean()) {
				BodyDef bodyDef = mDef.getBodyDefCopy();

				bodyDef.angle = input.readFloat();
				bodyDef.angularVelocity = input.readFloat();
				bodyDef.linearVelocity.set(kryo.readObject(input, Vector2.class));
				bodyDef.awake = input.readBoolean();
				bodyDef.active = input.readBoolean();
				bodyDef.position.set(mPosition);

				mSavedBody = bodyDef;
			}
		}
	}

	@Override
	public void copy(Object fromOriginal) {
		if (fromOriginal instanceof Actor) {
			Actor fromActor = (Actor) fromOriginal;

			// Set active state
			if (!mEditorActive) {
				mActive = fromActor.mActive;
			}

			// Set def
			mDef = fromActor.mDef;

			// Create body
			if (!mEditorActive) {
				if (fromActor.mBody != null) {
					BodyDef bodyDef = mDef.getBodyDefCopy();

					bodyDef.angle = fromActor.mBody.getAngle();
					bodyDef.angularVelocity = fromActor.mBody.getAngularVelocity();
					bodyDef.linearVelocity.set(fromActor.mBody.getLinearVelocity());
					bodyDef.awake = fromActor.mBody.isAwake();
					bodyDef.active = fromActor.mBody.isActive();
					bodyDef.position.set(mPosition);

					createBody(bodyDef);
				}
			}
		}
	}

	@Override
	public void setPosition(Vector2 position) {
		mPosition.set(position);

		// Change body if exist
		if (mBody != null) {
			mBody.setTransform(mPosition, mBody.getAngle());
		}

		if (mHasBodyCenter) {
			destroyBodyCenter();
			createBodyCenter();
		}

		if (mHasBodyCorners) {
			destroyBodyCorners();
			createBodyCorners();
		}

		sendChangeEvent(EventTypes.POSITION);
	}

	@Override
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
	 * @return current world
	 */
	public static World getWorld() {
		return mWorld;
	}

	/**
	 * Sets if the editor will be active for all the actors
	 * @param editorActive true if the editor will be active
	 */
	public static void setEditorActive(boolean editorActive) {
		mEditorActive = editorActive;
	}

	/**
	 * @return true if the editor is active
	 */
	public static boolean isEditorActive() {
		return mEditorActive;
	}

	/**
	 * Sets the current player for all the actors
	 * @param playerActor the current player actor
	 */
	public static void setPlayerActor(PlayerActor playerActor) {
		mPlayerActor = playerActor;
	}

	/**
	 * Sets the level
	 * @param level the current level
	 */
	public static void setLevel(Level level) {
		mLevel = level;
	}

	/**
	 * @return the body of the actor
	 */
	public Body getBody() {
		return mBody;
	}

	@Override
	public void createBody() {
		// Use saved body
		if (mSavedBody != null) {
			createBody(mSavedBody);
			mSavedBody = null;
		}
		// Use default
		else {
			BodyDef bodyDef = mDef.getBodyDefCopy();
			bodyDef.position.set(mPosition);
			createBody(bodyDef);
		}
	}

	/**
	 * @return true if the actor has created a body
	 */
	public boolean hasBody() {
		return mBody != null;
	}

	/**
	 * @return true if the actor has a saved body
	 */
	public boolean hasSavedBody() {
		return mSavedBody != null;
	}

	/**
	 * Creates a new body out of the fixture definition and the specified body definition
	 * @param bodyDef the body definition to use for the body
	 */
	public void createBody(BodyDef bodyDef) {
		if (mWorld != null && mBody == null) {
			if (mSkipRotate || SceneSwitcher.getActiveScene(true) instanceof LevelEditor) {
				bodyDef.angularVelocity = 0;
			} else {
				bodyDef.fixedRotation = true;
			}
			mBody = mWorld.createBody(bodyDef);
			createFixtures();
			calculateRotatedVertices(true);
			mBody.setUserData(this);

			mFixtureCreateTime = GameTime.getTotalGlobalTimeElapsed();
			mBodyUpdateTime = mFixtureCreateTime;
		}
	}

	/**
	 * Sets the angular dampening on the current body
	 */
	private void reloadBody() {
		if (!mSkipRotate) {
			mBody.setAngularVelocity(mDef.getRotationSpeedRad());
		} else {
			mBody.setAngularVelocity(0);
		}

		// Only set starting angle if we're not rotating
		if (mDef.getRotationSpeedRad() == 0) {
			mBody.setTransform(mPosition, mDef.getStartAngleRad());
		}
		mBody.setType(mDef.getBodyDef().type);

		mBodyUpdateTime = GameTime.getTotalGlobalTimeElapsed();
	}

	@Override
	public void destroyBody() {
		if (mBody != null) {
			mBody.getWorld().destroyBody(mBody);
			mBody = null;
			clearRotatedVertices();
		}
	}

	/**
	 * Disables the actor fully. Removes the body from the world and a level will not
	 * render it as it has been disposed.
	 */
	@Override
	public void dispose() {
		mActive = false;

		destroyBody();
	}

	@Override
	public void reset() {
		destroyBody();
		mBody = null;
		mDef = null;
		mPosition.set(0, 0);
		mCollidingActors.clear();
		mDestroyBody = false;
		mRotatedVertices = null;

		mTriggerInfos.clear();
	}

	/**
	 * Reload fixtures. This destroys all the existing fixtures and creates new fixtures
	 * from the actor definition. Does nothing if the actor doesn't have a body. This also
	 * resets the body corners if we have any
	 */
	public void reloadFixtures() {
		if (mBody != null) {
			destroyFixtures();
			calculateRotatedVertices(true);
			createFixtures();

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

			reloadSelectedOutline();
		}
	}

	/**
	 * @return true if this actor saves its def, i.e. #ResourceCacheFacade will not handle
	 *         the def. This is true for terrain actors, as there is only one actor per
	 *         definition, defaults to false.
	 */
	public boolean savesDef() {
		return false;
	}

	/**
	 * Creates a center picking body to move the center of the actor
	 */
	public void createBodyCenter() {
		if (mEditorActive) {
			mHasBodyCenter = true;
			if (mBody != null && mCenterBody == null) {
				mCenterBody = mWorld.createBody(new BodyDef());
				mCenterBody.createFixture(SceneSwitcher.getPickingFixtureDef());
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
	 * Creates body corners for the actor. Only applicable if actor is using a custom
	 * shape and in an editor.
	 */
	@Override
	public void createBodyCorners() {
		if (mDef.getVisual().getShapeType() == ActorShapeTypes.CUSTOM && mEditorActive) {
			mHasBodyCorners = true;
			Vector2 worldPos = new Vector2();
			for (Vector2 localPos : mDef.getVisual().getCorners()) {
				worldPos.set(localPos).add(mPosition).add(getDef().getVisual().getCenterOffset());
				createBodyCorner(worldPos);
			}
		}
	}

	/**
	 * Destroys all body corners
	 */
	@Override
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
				mBody.setAngularVelocity(mDef.getRotationSpeedRad());
			}
		}
	}

	/**
	 * @return true if the actor is currently skipping rotation
	 */
	public boolean isSkippingRotation() {
		return mSkipRotate;
	}

	@Override
	public void setSelected(boolean selected) {
		mSelected = selected;
	}

	@Override
	public boolean isSelected() {
		return mSelected;
	}

	@Override
	public float getBoundingRadius() {
		return mDef.getVisual().getBoundingRadius();
	}

	/**
	 * @return activation time of the actor, negative value if the actor is inactive.
	 */
	public float getActivationTime() {
		return mActivationTime;
	}

	/**
	 * Activates the actor.
	 */
	public void activate() {
		mDestroyBody = false;
		mActive = true;
		mActivationTime = SceneSwitcher.getGameTime().getTotalTimeElapsed();

		// Create body
		if (mBody == null) {
			createBody();
		}
	}

	/**
	 * Deactivates the actor. This resets the activation time to a negative value.
	 */
	public void deactivate() {
		mActivationTime = -1;
		mActive = false;
	}

	/**
	 * The body will destroyed next update...
	 */
	public void destroyBodySafe() {
		mDestroyBody = true;
	}

	/**
	 * @return true if the body shall be destroyed
	 */
	public boolean shallBodyBeDestroyed() {
		return mDestroyBody;
	}

	/**
	 * @return true if the actor is activated
	 */
	public boolean isActive() {
		return mActive;
	}

	/**
	 * @return true if it currently is drawing body corners
	 */
	public boolean hasBodyCorners() {
		return mHasBodyCorners;
	}

	/**
	 * Default constructor
	 * @note needs to be public for reflect on android
	 */
	public Actor() {
		mUniqueId = UUID.randomUUID();
	}

	/**
	 * Adds a fixture to the body and fixture list
	 * @param fixtureDef the fixture to add
	 */
	protected void addFixture(FixtureDef fixtureDef) {
		mDef.getVisual().addFixtureDef(fixtureDef);
		mBody.createFixture(fixtureDef);

		mFixtureCreateTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * Clears all fixtures, both from the actor and from the definition...
	 */
	protected void clearFixturesDefs() {
		destroyFixtures();

		if (mDef != null) {
			mDef.getVisual().clearFixtures();
		}
	}

	/**
	 * Destroys all fixtures from the body
	 */
	protected void destroyFixtures() {
		if (mBody != null) {
			Array<Fixture> fixtures = new Array<>(mBody.getFixtureList());
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
	 * @return the mask bit used for determining who the actor can collide with
	 */
	protected abstract short getFilterCollidingCategories();

	/**
	 * @return rotated vertices for this actor
	 */
	protected ArrayList<Vector2> getRotatedVertices() {
		return mRotatedVertices;
	}

	/**
	 * Calculates the rotated vertices (both regular and border)
	 * @param forceRecalculation this forces recalculation
	 */
	protected void calculateRotatedVertices(boolean forceRecalculation) {
		float currentRotation = mDef.getBodyDef().angle;
		if (mBody != null) {
			currentRotation = mBody.getAngle();
		}

		// Update vertices
		if (mRotationPrevious != currentRotation || mRotatedVertices == null || forceRecalculation) {
			mRotationPrevious = currentRotation;

			clearRotatedVertices();

			float rotation = MathUtils.radiansToDegrees * currentRotation;

			mRotatedVertices = copyVectorArray(mDef.getVisual().getTriangleVertices());
			if (mRotatedVertices != null) {
				Geometry.rotateVertices(mRotatedVertices, rotation, true, getDef().getVisual().getCenterOffset());
			}
		}
	}

	/**
	 * Clears the rotated vertices
	 */
	private void clearRotatedVertices() {
		mRotatedVertices = null;
	}

	/**
	 * Calculates the rotated vertices (both regular and border). Calls
	 * {@link #calculateRotatedVertices(boolean)} with false.
	 */
	protected void calculateRotatedVertices() {
		calculateRotatedVertices(false);
	}

	/**
	 * Creates a copy of the specified vector2 array
	 * @param array the array to make a copy of
	 * @return copied array, null if parameter array is null.
	 */
	protected static ArrayList<Vector2> copyVectorArray(ArrayList<Vector2> array) {
		if (array == null) {
			return null;
		}

		ArrayList<Vector2> verticesCopy = new ArrayList<>();

		for (Vector2 vertex : array) {
			int foundIndex = verticesCopy.indexOf(vertex);
			if (foundIndex != -1) {
				verticesCopy.add(verticesCopy.get(foundIndex));
			} else {
				verticesCopy.add(new Vector2(vertex));
			}
		}

		return verticesCopy;
	}

	/**
	 * Create the fixtures for the actor
	 */
	protected void createFixtures() {
		for (FixtureDef fixtureDef : mDef.getVisual().getFixtureDefs()) {
			if (fixtureDef != null && fixtureDef.shape != null) {
				setFilterCollisionData(fixtureDef);
				mBody.createFixture(fixtureDef);
			}
		}
	}

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
	 * Creates a body for the specific point at the specific index
	 * @param corner the corner to create a body for
	 */
	private void createBodyCorner(Vector2 corner) {
		Body body = mWorld.createBody(new BodyDef());
		body.createFixture(SceneSwitcher.getPickingFixtureDef());
		body.setTransform(corner, 0);
		HitWrapper hitWrapper = new HitWrapper(this, "corner");
		body.setUserData(hitWrapper);
		mCorners.add(body);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <ResourceType> ResourceType copyNewResource() {
		Actor copy = super.copyNewResource();

		copy.mDef = mDef;
		copy.mPosition.set(mPosition);
		copy.mActive = false;

		// Triggers
		for (TriggerInfo triggerInfo : mTriggerInfos) {
			TriggerInfo copyTriggerInfo = triggerInfo.copy();
			copyTriggerInfo.listener = copy;
			copy.mTriggerInfos.add(copyTriggerInfo);
			copyTriggerInfo.trigger.addListener(copyTriggerInfo);
		}

		return (ResourceType) copy;
	}

	@Override
	public void addCorners(java.util.List<Vector2> corners) {
		getDef().getVisual().addCorners(corners);
	}

	@Override
	public void addCorners(Vector2[] corners) {
		getDef().getVisual().addCorners(corners);
	}

	@Override
	public void addCorner(Vector2 corner) {
		getDef().getVisual().addCorner(corner);
	}

	@Override
	public void addCorner(Vector2 corner, int index) {
		getDef().getVisual().addCorner(corner, index);
	}

	@Override
	public Vector2 removeCorner(int index) {
		return getDef().getVisual().removeCorner(index);
	}

	@Override
	public void moveCorner(int index, Vector2 newPos) {
		getDef().getVisual().moveCorner(index, newPos);
	}

	@Override
	public int getCornerCount() {
		return getDef().getVisual().getCornerCount();
	}

	@Override
	public Vector2 getCornerPosition(int index) {
		return getDef().getVisual().getCornerPosition(index);
	}

	@Override
	public int getCornerIndex(Vector2 position) {
		for (int i = 0; i < mCorners.size(); ++i) {
			if (mCorners.get(i).getPosition().equals(position)) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public ArrayList<Vector2> getCorners() {
		return getDef().getVisual().getCorners();
	}

	@Override
	public void clearCorners() {
		getDef().getVisual().clearCorners();
	}

	@Override
	public void setIsBeingMoved(boolean isBeingMoved) {
		mIsBeingMoved = isBeingMoved;
	}

	@Override
	public boolean isBeingMoved() {
		return mIsBeingMoved;
	}

	/**
	 * @return colliding actors
	 */
	protected Set<Actor> getCollidingActors() {
		return mCollidingActors.keySet();
	}

	// Kryo variables
	/** Current life */
	@Tag(3) private float mLife = 0;
	/** Body position, remember even when we don't have a body */
	@Tag(4) private Vector2 mPosition = new Vector2();
	/** Trigger informations */
	@Tag(5) private ArrayList<TriggerInfo> mTriggerInfos = new ArrayList<>();

	// Kryo special variables
	/** Revision of the actor */
	private static final int CLASS_REVISION = 2;
	@Tag(131) private int mClassRevision = 0;

	/** True if the actor is active */
	private boolean mActive = true;
	/** The belonging definition of this actor */
	private ActorDef mDef = null;
	/** Physical body */
	private Body mBody = null;

	// Not saved
	/** Saved body definition (used when creating the body after a load */
	private BodyDef mSavedBody = null;
	/** Current actors we're colliding with @todo do we need to save colliding actors? */
	private HashMap<Actor, AtomicInteger> mCollidingActors = new HashMap<>();
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
	/** Activation time of the actor, local time for the scene */
	private float mActivationTime = mActive ? 0 : -1;
	/** True if the actor is selected, only applicable in editor */
	private boolean mSelected = false;
	/** Old rotation */
	private float mRotationPrevious = 0;
	/** Rotated vertices of the actor */
	private ArrayList<Vector2> mRotatedVertices = null;
	/** If the actor shall be destroyed */
	private boolean mDestroyBody = false;
	/** Only draws the shape's outline */
	private boolean mDrawOnlyOutline = false;
	/** If the actor is being moved */
	private boolean mIsBeingMoved = false;
	/** Selected outline */
	private ArrayList<Vector2> mSelectedOutline = null;

	/** Event dispatcher */
	protected static EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
	/** The world used for creating bodies */
	protected static World mWorld = null;
	/** If the actor will be used for an editor */
	protected static boolean mEditorActive = false;
	/** The player of this game, for derived actor to have easy access */
	protected static PlayerActor mPlayerActor = null;
	/** Current level */
	protected static Level mLevel = null;
}
