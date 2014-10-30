package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;
import java.util.Collections;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.Maths;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.Weapon;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimTypes;
import com.spiddekauga.voider.game.triggers.TActorActivated;
import com.spiddekauga.voider.game.triggers.TScreenAt;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.EarClippingTriangulator;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Pools;

/**
 * An enemy actor, these can generally shoot on the player.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class EnemyActor extends Actor {
	/**
	 * Default constructor
	 */
	public EnemyActor() {
		deactivate();
	}

	@Override
	public void dispose() {
		super.dispose();

		clearPolygonOutline();
	}

	@Override
	public void setDef(ActorDef def) {
		super.setDef(def);

		// Set weapon
		resetWeapon();
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);

		EnemyActorDef def = getDef(EnemyActorDef.class);
		if (def != null && def.getMovementType() != null && getBody() != null) {
			if (isActive()) {
				// Update movement
				switch (def.getMovementType()) {
				case PATH:
					if (mPath != null) {
						updatePathMovement(deltaTime);
					}

					if (!mEditorActive) {
						if (TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_DEACTIVATE) == null) {
							if (mPath != null) {
								checkPathDeactivate();
							} else {
								checkStationaryDeactivate();
							}
						}
					}
					break;

				case AI:
					updateAiMovement(deltaTime);
					break;

				case STATIONARY:
					if (!mEditorActive) {
						if (TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_DEACTIVATE) == null) {
							checkStationaryDeactivate();
						}
					}
					break;
				}

				// Update weapon
				if (def.hasWeapon()) {
					updateWeapon(deltaTime);
				}
			}
			// Not active -> Slow down for AI
			else {
				if (def.getMovementType() == MovementTypes.AI) {
					slowDown(deltaTime);
				}
			}
		}
	}

	/**
	 * Slows down the enemy
	 * @param deltaTime time elapsed since last frame
	 */
	private void slowDown(float deltaTime) {
		if (getBody() != null) {
			Vector2 velocity = Pools.vector2.obtain().set(getBody().getLinearVelocity());
			Vector2 dampVelocity = Pools.vector2.obtain().set(velocity);

			float dampening = Config.Actor.Enemy.LINEAR_DAMPENING * deltaTime;
			dampVelocity.scl(dampening);
			velocity.sub(dampVelocity);

			getBody().setLinearVelocity(velocity);

			Pools.vector2.freeAll(velocity, dampVelocity);
		}
	}

	@Override
	public void renderShape(ShapeRendererEx shapeRenderer) {
		// Don't render non-leaders when in editor
		if (mEditorActive) {
			if (mGroup == null || mGroupLeader) {
				super.renderShape(shapeRenderer);
			}
		} else {
			super.renderShape(shapeRenderer);
		}
	}

	@Override
	public void renderEditor(ShapeRendererEx shapeRenderer) {
		if (mGroup == null || mGroupLeader) {
			super.renderEditor(shapeRenderer);

			RenderOrders.offsetZValueEditor(shapeRenderer, this);

			// Draw path to
			// Activate trigger
			shapeRenderer.push(ShapeType.Line);
			TriggerInfo activateTrigger = TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_ACTIVATE);
			if (activateTrigger != null && activateTrigger.trigger instanceof IResourcePosition) {
				shapeRenderer.setColor((Color) SkinNames.getResource(SkinNames.EditorVars.ENEMY_ACTIVATE_TRIGGER_LINE_COLOR));
				shapeRenderer.line(getPosition(), ((IResourcePosition) activateTrigger.trigger).getPosition());
			}

			// Deactivate trigger
			TriggerInfo deactivateTrigger = TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_DEACTIVATE);
			if (deactivateTrigger != null && deactivateTrigger.trigger instanceof IResourcePosition) {
				shapeRenderer.setColor((Color) SkinNames.getResource(SkinNames.EditorVars.ENEMY_DEACTIVATE_TRIGGER_LINE_COLOR));
				shapeRenderer.line(getPosition(), ((IResourcePosition) deactivateTrigger.trigger).getPosition());
			}

			shapeRenderer.pop();


			// Highlight if will be spawned when test running the level
			Scene scene = SceneSwitcher.getActiveScene(false);
			if (scene instanceof LevelEditor) {
				LevelEditor levelEditor = (LevelEditor) scene;
				if (levelEditor.isEnemyHighlightOn()) {

					float levelStartCoord = levelEditor.getRunFromHerePosition();
					float enemyActivationCoord = Float.MIN_VALUE;

					// Enemy has a dedicated trigger
					TriggerInfo triggerInfo = TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_ACTIVATE);
					if (triggerInfo != null) {
						if (triggerInfo.trigger instanceof TScreenAt) {
							enemyActivationCoord = ((TScreenAt) triggerInfo.trigger).getPosition().x;
						}
					}

					// Enemy will use default trigger
					if (enemyActivationCoord == Float.MIN_VALUE) {
						enemyActivationCoord = calculateDefaultActivateTriggerPosition(levelEditor.getLevel().getSpeed());
					}

					// Enemy will spawn
					if (levelStartCoord <= enemyActivationCoord) {

						shapeRenderer.setColor((Color) SkinNames.getResource(SkinNames.EditorVars.ENEMY_ACTIVATE_ON_TEST_RUN_COLOR));

						Vector2 offsetPosition = new Vector2(getPosition());

						if (getDef().getVisual().getCornerCount() == 2) {
							offsetPosition.sub(getDef().getVisual().getCorners().get(0));
							offsetPosition.sub(getDef().getVisual().getCenterOffset());
						}

						if (mActivateCircle == null) {
							reloadActivateCircle();
						}

						if (mActivateCircle != null) {
							RenderOrders.offsetZValue(shapeRenderer);
							shapeRenderer.triangles(mActivateCircle, offsetPosition);
						}
					}
				}
			}

			RenderOrders.resetZValueOffset(shapeRenderer);
			RenderOrders.resetZValueOffsetEditor(shapeRenderer, this);
		}
	}

	/**
	 * Set the path we're following
	 * @param path the path we're following
	 */
	public void setPath(Path path) {
		// Remove enemy from old path
		if (mPath != null) {
			mPath.removeEnemy(this);
		}

		mPath = path;

		if (path != null) {
			mPath.addEnemy(this);
		}
	}

	/**
	 * @return current path we're following, if we're not following any it returns null
	 */
	public Path getPath() {
		return mPath;
	}

	/**
	 * Sets the speed of the actor, although not the definition, so this is just a
	 * temporary speed
	 * @param speed new temporary speed.
	 */
	public void setSpeed(float speed) {
		if (getBody() != null) {
			Vector2 velocity = getBody().getLinearVelocity();
			velocity.nor();
			velocity.scl(speed);
			getBody().setLinearVelocity(velocity);
		}
	}

	@Override
	public void setPosition(Vector2 position) {
		super.setPosition(position);

		// Set position of other actors in the group
		if (mGroupLeader && mGroup != null) {
			mGroup.setPosition(position);
		}
	}

	/**
	 * Adds a trigger to the enemy actor
	 * @param triggerInfo trigger information
	 */
	@Override
	public void addTrigger(TriggerInfo triggerInfo) {
		super.addTrigger(triggerInfo);

		if (mGroupLeader && mGroup != null) {
			mGroup.addTrigger(triggerInfo);
		}
	}

	/**
	 * Removes the specified trigger from this enemy
	 * @param triggerInfo trigger information
	 */
	@Override
	public void removeTrigger(TriggerInfo triggerInfo) {
		super.removeTrigger(triggerInfo);

		if (mGroupLeader && mGroup != null) {
			mGroup.removeTrigger(triggerInfo);
		}
	}

	/**
	 * Creates the default activate trigger.
	 * @param level the current active level
	 * @return trigger info that was created
	 */
	public TriggerInfo createDefaultActivateTrigger(Level level) {
		// Calculate position of trigger
		float xCoord = calculateDefaultActivateTriggerPosition(level.getSpeed());

		// Create the trigger
		TScreenAt trigger = new TScreenAt(level, xCoord);
		trigger.setHidden(true);


		// Create the trigger information
		TriggerInfo triggerInfo = new TriggerInfo();

		triggerInfo.action = Actions.ACTOR_ACTIVATE;
		triggerInfo.delay = 0;
		triggerInfo.listener = this;
		triggerInfo.setTrigger(trigger);

		return triggerInfo;
	}

	/**
	 * Calculates the activation coordinate depending on the current level speed
	 * @param levelSpeed speed of the level
	 * @return enemy activation coordinate
	 */
	private float calculateDefaultActivateTriggerPosition(float levelSpeed) {
		// Calculate position of trigger
		float xCoord = getPosition().x - getDef().getVisual().getBoundingRadius();

		// Decrease position if we are in an enemy group
		if (mGroup != null) {
			float distancePerEnemy = levelSpeed * mGroup.getSpawnTriggerDelay();
			float offset = (mGroup.getEnemyCount() - 1) * distancePerEnemy;
			xCoord -= offset;
		}

		return xCoord;
	}

	/**
	 * Creates the default deactivate trigger. Only AI movement uses this atm
	 * @return trigger info that was created
	 */
	public TriggerInfo createDefaultDeactivateTrigger() {
		TActorActivated trigger = new TActorActivated(this);

		TriggerInfo triggerInfo = new TriggerInfo();
		triggerInfo.action = Actions.ACTOR_DEACTIVATE;
		triggerInfo.delay = Config.Actor.Enemy.DEACTIVATE_TIME_DEFAULT;
		triggerInfo.listener = this;
		triggerInfo.setTrigger(trigger);

		return triggerInfo;
	}

	/**
	 * Resets the weapon
	 */
	public void resetWeapon() {
		mWeapon.setWeaponDef(getDef(EnemyActorDef.class).getWeaponDef());

		mShootAngle = getDef(EnemyActorDef.class).getAimStartAngle();
	}

	@Override
	public void write(Kryo kryo, Output output) {
		super.write(kryo, output);

		EnemyActorDef enemyDef = getDef(EnemyActorDef.class);

		// Weapon
		if (enemyDef.hasWeapon()) {
			kryo.writeObject(output, mWeapon);
			output.writeFloat(mShootAngle);
		}

		// Group
		if (mGroup != null) {
			output.writeBoolean(mGroupLeader);
		}

		// Movement
		if (enemyDef.getMovementType() == MovementTypes.AI) {
			if (enemyDef.isMovingRandomly()) {
				output.writeFloat(mRandomMoveNext, 100, true);
				kryo.writeObject(output, mRandomMoveDirection);
			}
		} else if (enemyDef.getMovementType() == MovementTypes.PATH) {
			kryo.writeObjectOrNull(output, mPath, Path.class);

			if (mPath != null) {
				output.writeInt(mPathIndexNext, false);

				switch (mPath.getPathType()) {
				case ONCE:
					output.writeBoolean(mPathOnceReachedEnd);
					break;

				case BACK_AND_FORTH:
					output.writeBoolean(mPathForward);
					break;

				case LOOP:
					// Does nothing
					break;
				}
			}
		}
	}

	@Override
	public void read(Kryo kryo, Input input) {
		super.read(kryo, input);

		EnemyActorDef enemyDef = getDef(EnemyActorDef.class);
		setDef(enemyDef);

		// Weapon
		if (enemyDef.hasWeapon()) {
			mWeapon = kryo.readObject(input, Weapon.class);
			mWeapon.setWeaponDef(enemyDef.getWeaponDef());
			mShootAngle = input.readFloat();
		}

		// Group
		if (mGroup != null) {
			mGroupLeader = input.readBoolean();
		}

		// Movement
		if (enemyDef.getMovementType() == MovementTypes.AI) {
			if (enemyDef.isMovingRandomly()) {
				mRandomMoveNext = input.readFloat(100, true);
				Pools.vector2.free(mRandomMoveDirection);
				mRandomMoveDirection = kryo.readObject(input, Vector2.class);
			}
		} else if (enemyDef.getMovementType() == MovementTypes.PATH) {
			mPath = kryo.readObjectOrNull(input, Path.class);

			if (mPath != null) {
				mPathIndexNext = input.readInt(false);

				switch (mPath.getPathType()) {
				case ONCE:
					mPathOnceReachedEnd = input.readBoolean();
					break;

				case BACK_AND_FORTH:
					mPathForward = input.readBoolean();
					break;

				case LOOP:
					// Does nothing
					break;
				}
			}
		}
	}

	@Override
	public <ResourceType> ResourceType copyNewResource() {
		ResourceType copy = super.copyNewResource();

		EnemyActor enemyCopy = (EnemyActor) copy;
		// enemyCopy.mPath = mPath;
		// enemyCopy.mGroup = mGroup;

		// Never make a copy a group leader?
		enemyCopy.mGroupLeader = false;

		enemyCopy.mWeapon = mWeapon.copy();
		enemyCopy.mRandomMoveDirection.set(mRandomMoveDirection);
		enemyCopy.mTargetDirection.set(mTargetDirection);

		return copy;
	}

	@Override
	public void copy(Object fromOriginal) {
		super.copy(fromOriginal);

		EnemyActor fromEnemy = (EnemyActor) fromOriginal;

		mGroupLeader = fromEnemy.mGroupLeader;
		mWeapon = fromEnemy.mWeapon.copy();
		mShootAngle = fromEnemy.mShootAngle;

		mRandomMoveNext = fromEnemy.mRandomMoveNext;
		mRandomMoveDirection.set(fromEnemy.mRandomMoveDirection);

		if (fromEnemy.mPath != null) {
			mPath = fromEnemy.mPath;
			mPathIndexNext = fromEnemy.mPathIndexNext;
			mPathOnceReachedEnd = fromEnemy.mPathOnceReachedEnd;
			mPathForward = fromEnemy.mPathForward;
		}
	}

	/**
	 * Resets the movement. This resets the movement to start from the beginning of the
	 * path. Only applicable for path movement
	 */
	public void resetPathMovement() {
		mPathIndexNext = -1;
		mPathForward = true;
		mPathOnceReachedEnd = false;
		if (getBody() != null) {
			getBody().setAngularVelocity(getDef().getRotationSpeedRad());
			getBody().setTransform(getPosition(), getDef().getBodyDef().angle);
		}
	}

	/**
	 * @return the enemy group
	 */
	public EnemyGroup getEnemyGroup() {
		return mGroup;
	}

	/**
	 * Sets if the enemy is a group leader or not
	 * @param leader set to true to make this a leader
	 */
	public void setGroupLeader(boolean leader) {
		mGroupLeader = leader;
	}

	/**
	 * @return true if the enemy is a group leader
	 */
	public boolean isGroupLeader() {
		return mGroupLeader;
	}


	@Override
	public boolean addBoundResource(IResource boundResource) {
		boolean success = super.addBoundResource(boundResource);

		if (boundResource instanceof Path) {
			setPath((Path) boundResource);
		}
		// Enemy group is always in charge of binding/unbinding all

		return success;
	}

	@Override
	public boolean removeBoundResource(IResource boundResource) {
		boolean success = super.removeBoundResource(boundResource);

		if (boundResource instanceof Path) {
			if (boundResource == mPath) {
				setPath(null);
				success = true;
			}
		}
		// Enemy group is always in charge of binding/unbinding all

		return success;
	}

	/**
	 * @return enemy filter category
	 */
	@Override
	protected short getFilterCategory() {
		return ActorFilterCategories.ENEMY;
	}

	/**
	 * Can collide only with other players
	 * @return colliding categories
	 */
	@Override
	protected short getFilterCollidingCategories() {
		return ActorFilterCategories.PLAYER;
	}

	/**
	 * Sets the group of this enemy
	 * @param enemyGroup the enemy group, set as null to "remove" it from any group
	 */
	void setEnemyGroup(EnemyGroup enemyGroup) {
		mGroup = enemyGroup;

		if (mGroup == null) {
			mGroupLeader = false;
		}
	}

	/**
	 * Updates the weapon
	 * @param deltaTime time elapsed since last frame
	 */
	private void updateWeapon(float deltaTime) {
		mWeapon.update(deltaTime);

		mWeapon.setPosition(getPosition());

		if (mWeapon.canShoot()) {
			Vector2 shootDirection = getShootDirection();

			mWeapon.shoot(shootDirection);

			// Calculate next shooting angle
			if (getDef(EnemyActorDef.class).getAimType() == AimTypes.ROTATE) {
				mShootAngle += mWeapon.getCooldownTime() * getDef(EnemyActorDef.class).getAimRotateSpeed();
			}

			Pools.vector2.free(shootDirection);
		}
	}

	/**
	 * @return direction which we want to shoot in. Be sure to free this vector using
	 *         Pools.vector2.free(vector);.
	 */
	private Vector2 getShootDirection() {
		Vector2 shootDirection = new Vector2();

		switch (getDef(EnemyActorDef.class).getAimType()) {
		case ON_PLAYER:
			shootDirection.set(mPlayerActor.getPosition()).sub(getPosition());
			break;

		case DIRECTION:
			// Shoot in current direction
			shootDirection.set(1, 0);
			shootDirection.rotate(getDef(EnemyActorDef.class).getAimStartAngle());
			break;

		case MOVE_DIRECTION:
			// If we're moving shoot in moving direction
			if (getBody() != null) {
				shootDirection.set(getBody().getLinearVelocity());
			} else {
				shootDirection.set(0, 0);
			}

			// If we're still, shoot in actor's "look" direction
			if (shootDirection.len2() == 0) {
				shootDirection.set(1, 0);

				if (getBody() != null) {
					shootDirection.rotate(MathUtils.radiansToDegrees * getBody().getAngle());
				} else {
					shootDirection.rotate(MathUtils.radiansToDegrees * getDef().getBodyDef().angle);
				}
			}
			break;

		case IN_FRONT_OF_PLAYER: {
			Vector2 playerVelocity = mPlayerActor.getBody().getLinearVelocity();
			// float levelSpeed = 0;
			// if (!mEditorActive) {
			// levelSpeed = mLevel.getSpeed();
			// }
			// playerVelocity.x -= levelSpeed;

			boolean targetPlayerInstead = false;

			// If velocity is standing still, just shoot at the player...
			float playerSpeedSq = playerVelocity.len2();
			if (Maths.floatEquals(playerSpeedSq, 0)) {
				targetPlayerInstead = true;
			}
			// Calculate where the bullet would intersect with the player
			else {
				Vector2 bulletVelocity = Geometry.interceptTarget(getPosition(), mWeapon.getDef().getBulletSpeed() /*
																													 * +
																													 * levelSpeed
																													 */, mPlayerActor.getPosition(),
						playerVelocity);
				shootDirection.set(bulletVelocity);
				Pools.vector2.free(bulletVelocity);
				bulletVelocity = null;

				// Cannot intercept, target player
				if (shootDirection.x != shootDirection.x || shootDirection.y != shootDirection.y) {
					targetPlayerInstead = true;
				}
			}

			if (targetPlayerInstead) {
				shootDirection.set(mPlayerActor.getPosition()).sub(getPosition());
			}
			break;
		}

		case ROTATE:
			// Shoot in current direction
			shootDirection.set(1, 0);
			shootDirection.rotate(mShootAngle);
			break;
		}

		return shootDirection;
	}

	/**
	 * Follow the path
	 * @param deltaTime time elapsed since last frame
	 */
	private void updatePathMovement(float deltaTime) {
		if (mPath == null || mPath.getCornerCount() < 2 || mPathOnceReachedEnd) {
			return;
		}

		// Special case, not initialized. Set position to first position
		if (mPathIndexNext == -1) {
			mPathIndexNext = 1;
			setPosition(mPath.getCornerPosition(0));

			mTargetDirection.set(mPath.getCornerPosition(mPathIndexNext)).sub(getPosition());

			// Set angle
			if (getDef(EnemyActorDef.class).isTurning()) {
				getBody().setTransform(getPosition(), (float) Math.toRadians(mTargetDirection.angle()));
			}

			moveToTarget(mTargetDirection, deltaTime);
		}


		if (hasPassedTarget()) {
			// Special case for ONCE and last index
			// Just continue straight forward, i.e do nothing
			if (mPath.getPathType() == PathTypes.ONCE && mPathIndexNext == mPath.getCornerCount() - 1) {
				mPathOnceReachedEnd = true;
				return;
			}

			calculateNextPathIndex();

			// No turning, change direction directly
			mTargetDirection.set(mPath.getCornerPosition(mPathIndexNext)).sub(getPosition());
			moveToTarget(mTargetDirection, deltaTime);
		} else if (getDef(EnemyActorDef.class).isTurning()) {
			mTargetDirection.set(mPath.getCornerPosition(mPathIndexNext)).sub(getPosition());
			moveToTarget(mTargetDirection, deltaTime);
		}
	}

	/**
	 * Update the AI movement
	 * @param deltaTime time elapsed since the last frame
	 */
	private void updateAiMovement(float deltaTime) {
		// Calculate distance to player
		Vector2 targetDirection = Pools.vector2.obtain();
		targetDirection.set(mPlayerActor.getPosition()).sub(getPosition());
		float targetDistanceSq = targetDirection.len2();

		// Enemy too far from the player?
		if (targetDistanceSq > getDef(EnemyActorDef.class).getPlayerDistanceMaxSq()) {
			moveToTarget(targetDirection, deltaTime);
			resetRandomMove();
		}
		// Enemy too close to player?
		else if (targetDistanceSq < getDef(EnemyActorDef.class).getPlayerDistanceMinSq()) {
			targetDirection.scl(-1);
			moveToTarget(targetDirection, deltaTime);
			resetRandomMove();
		}
		// Enemy in range
		else {
			if (getDef(EnemyActorDef.class).isMovingRandomly()) {
				calculateRandomMove(deltaTime);
			} else {
				if (mEditorActive) {
					getBody().setLinearVelocity(0, 0);
				} else {
					getBody().setLinearVelocity(mLevel.getSpeed(), 0);
				}
				getBody().setAngularVelocity(0);
				resetRandomMove();
			}
		}
	}

	/**
	 * Resets the random movement
	 */
	private void resetRandomMove() {
		mRandomMoveNext = 0;
	}

	/**
	 * Calculates the random movement of an enemy
	 * @param deltaTime time elapsed since last fram
	 */
	private void calculateRandomMove(float deltaTime) {
		boolean newMove = false;
		if (mRandomMoveNext <= 0) {
			float range = getDef(EnemyActorDef.class).getRandomTimeMax() - getDef(EnemyActorDef.class).getRandomTimeMin();
			mRandomMoveNext = (float) Math.random() * range + getDef(EnemyActorDef.class).getRandomTimeMin();
			newMove = true;
		} else {
			mRandomMoveNext -= deltaTime;
		}

		if (newMove) {
			float angle = (float) Math.random() * 360;
			mRandomMoveDirection.set(1, 0);
			mRandomMoveDirection.setAngle(angle);
			moveToTarget(mRandomMoveDirection, deltaTime);
		} else {
			moveToTarget(mRandomMoveDirection, deltaTime);
		}
	}

	/**
	 * Moves to the target area. Takes into account turning if enabled
	 * @param targetDirection the direction we want to move in
	 * @param deltaTime time elapsed since last frame
	 */
	private void moveToTarget(Vector2 targetDirection, float deltaTime) {

		if (getDef(EnemyActorDef.class).isTurning()) {
			moveToTargetTurning(targetDirection, deltaTime);
		} else {
			moveToTargetRegular(targetDirection, deltaTime);
		}
	}

	/**
	 * Moves to target using no turning algorithm
	 * @param targetDirection direction we want the enemy to move in
	 * @param deltaTime time elapsed since last frame
	 */
	private void moveToTargetRegular(Vector2 targetDirection, float deltaTime) {
		Vector2 velocity = Pools.vector2.obtain();
		velocity.set(targetDirection);
		velocity.nor().scl(getDef(EnemyActorDef.class).getSpeed());

		// Increase with level speed
		if (!mEditorActive && getDef(EnemyActorDef.class).getMovementType() == MovementTypes.AI) {
			velocity.x += mLevel.getSpeed();
		}

		getBody().setLinearVelocity(velocity);
		Pools.vector2.free(velocity);
	}

	/**
	 * Moves to target using turning algorithm
	 * @param targetDirection direction we want the enemy to move in
	 * @param deltaTime time elapsed since last frame
	 */
	private void moveToTargetTurning(Vector2 targetDirection, float deltaTime) {
		Vector2 velocity = Pools.vector2.obtain();
		velocity.set(getBody().getLinearVelocity());

		// Decrease with level speed
		if (!mEditorActive && getDef(EnemyActorDef.class).getMovementType() == MovementTypes.AI && !velocity.equals(Vector2.Zero)) {
			velocity.x -= mLevel.getSpeed();
		}

		boolean noVelocity = Maths.approxCompare(velocity.len2(), 0.01f);

		// Calculate angle between the vectors
		boolean counterClockwise = false;
		float bodyAngle = (float) Math.toDegrees(getBody().getAngle()) % 360;
		if (bodyAngle < 0) {
			bodyAngle += 360;
		}
		float velocityAngleOriginal = 0;
		float velocityAngle = 0;
		// We have no speed, use body angle instead
		if (noVelocity) {
			velocityAngleOriginal = bodyAngle;
		} else {
			velocityAngleOriginal = velocity.angle();
		}
		velocityAngle = velocityAngleOriginal;
		if (velocityAngle > 180) {
			counterClockwise = !counterClockwise;
			velocityAngle -= 180;
		}

		float targetAngleOriginal = targetDirection.angle();
		float targetAngle = targetAngleOriginal;
		if (targetAngle > 180) {
			counterClockwise = !counterClockwise;
			targetAngle -= 180;
		}

		float diffAngle = velocityAngle - targetAngle;
		if (diffAngle < 0) {
			counterClockwise = !counterClockwise;
		}

		// Because we only use 0-180 it could be 0 degrees when we should actually
		// head the other direction, take this into account!
		boolean oppositeDirection = false;
		if (velocityAngleOriginal < targetAngleOriginal) {
			if (Maths.approxCompare(velocityAngleOriginal, targetAngleOriginal - 180, Config.Actor.Enemy.TURN_ANGLE_MIN)) {
				oppositeDirection = true;
			}
		} else {
			if (Maths.approxCompare(velocityAngleOriginal - 180, targetAngleOriginal, Config.Actor.Enemy.TURN_ANGLE_MIN)) {
				oppositeDirection = true;
			}
		}

		if (!Maths.approxCompare(diffAngle, Config.Actor.Enemy.TURN_ANGLE_MIN) || oppositeDirection || noVelocity) {
			float angleBefore = velocity.angle();
			float rotation = getDef(EnemyActorDef.class).getTurnSpeed() * deltaTime * getDef(EnemyActorDef.class).getSpeed();
			if (!counterClockwise) {
				rotation = -rotation;
			}

			float angleAfter = velocityAngleOriginal + rotation;

			// Check if we turned too much?
			boolean turnedTooMuch = false;
			// If toTarget angle is between the before and after velocity angles
			// We have turned too much
			if (counterClockwise) {
				if (angleBefore < targetAngleOriginal && targetAngleOriginal < angleAfter) {
					turnedTooMuch = true;
				}
			} else {
				if (angleBefore > targetAngleOriginal && targetAngleOriginal > angleAfter) {
					turnedTooMuch = true;
				}
			}


			if (turnedTooMuch) {
				velocity.set(targetDirection);
				velocity.nor().scl(getDef(EnemyActorDef.class).getSpeed());
			} else {
				velocity.y = 0;
				velocity.x = 1;
				velocity.setAngle(angleAfter);
				velocity.scl(getDef(EnemyActorDef.class).getSpeed());
			}

			// Increase with level speed
			if (!mEditorActive && getDef(EnemyActorDef.class).getMovementType() == MovementTypes.AI) {
				velocity.x += mLevel.getSpeed();
			}

			getBody().setLinearVelocity(velocity);
		}


		getBody().setTransform(getPosition(), (float) Math.toRadians(getBody().getLinearVelocity().angle()));

		Pools.vector2.free(velocity);
	}


	/**
	 * Sets the next index to move to, takes into account what type the path is.
	 */
	private void calculateNextPathIndex() {
		if (mPathForward) {
			// We were at last, do path specific actions
			if (mPath.getCornerCount() - 1 == mPathIndexNext) {
				switch (mPath.getPathType()) {
				case ONCE:
					// Do nothing...
					break;

				case LOOP:
					mPathIndexNext = 0;
					break;

				case BACK_AND_FORTH:
					mPathIndexNext--;
					mPathForward = false;
				}
			} else {
				mPathIndexNext++;
			}
		}
		// We're going backwards, which must mean BACK_AND_FORTH is set
		else {
			if (mPathIndexNext == 0) {
				mPathIndexNext++;
				mPathForward = true;
			} else {
				mPathIndexNext--;
			}
		}
	}

	/**
	 * @return true if the enemy has passed the target.
	 */
	private boolean hasPassedTarget() {
		Vector2 diff = Pools.vector2.obtain();
		diff.set(mPath.getCornerPosition(mPathIndexNext)).sub(getPosition());

		boolean hasPassedTarget = false;

		if (getDef(EnemyActorDef.class).isTurning()) {
			float distanceSq = diff.len2();
			hasPassedTarget = distanceSq <= Config.Actor.Enemy.PATH_NODE_CLOSE_SQ;
		} else {
			float diffAngle = diff.angle() - mTargetDirection.angle();
			if (diffAngle > 100 || diffAngle < -100) {
				hasPassedTarget = true;
			}
		}

		Pools.vector2.free(diff);
		return hasPassedTarget;
	}

	/**
	 * Checks if the enemy shall be deactivated and destroyed when following an path. It
	 * will destroy the enemy when it never can reach go onto the screen again.
	 * @note Will do nothing if the enemy has a deactivate trigger.
	 */
	private void checkPathDeactivate() {
		if (TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_DEACTIVATE) == null) {
			if (getPath().getRightestCorner().x + getDef().getVisual().getBoundingRadius() < mLevel.getXCoord() - SceneSwitcher.getWorldWidth()) {

				// For 'once', check that the ship cannot be seen too
				boolean deactivate = false;
				if (getPath().getPathType() == PathTypes.ONCE) {
					Vector2 minPos = SceneSwitcher.getWorldMinCoordinates();
					Vector2 maxPos = SceneSwitcher.getWorldMaxCoordinates();

					// Left
					if (getPosition().x + getDef().getVisual().getBoundingRadius() < minPos.x) {
						deactivate = true;
					}
					// Right
					else if (getPosition().x - getDef().getVisual().getBoundingRadius() > maxPos.x) {
						deactivate = true;
					}
					// Bottom
					else if (getPosition().y + getDef().getVisual().getBoundingRadius() < minPos.y) {
						deactivate = true;
					}
					// Top
					else if (getPosition().y - getDef().getVisual().getBoundingRadius() > maxPos.y) {
						deactivate = true;
					}

					Pools.vector2.free(minPos);
					Pools.vector2.free(maxPos);
				} else {
					deactivate = true;
				}

				if (deactivate) {
					deactivate();
					destroyBody();
				}
			}
		}
	}

	/**
	 * Checks if the enemy shall be deactivated and destroyed when it is stationary. It
	 * will destroy the enemy once it's outside of the screen.
	 * @note Will do nothing if the enemy has a deactivate trigger.
	 */
	private void checkStationaryDeactivate() {
		if (TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_DEACTIVATE) == null) {
			if (getPosition().x + getDef().getVisual().getBoundingRadius() < mLevel.getXCoord() - SceneSwitcher.getWorldWidth()) {
				deactivate();
				destroyBody();
			}
		}
	}

	@Override
	public void reloadFixtures() {
		super.reloadFixtures();

		reloadActivateCircle();
	}

	/**
	 * Clears the polygon outline
	 */
	private void clearPolygonOutline() {
		if (mActivateCircle != null) {
			Pools.vector2.freeDuplicates(mActivateCircle);
			mActivateCircle = null;
		}
	}

	/**
	 * Reloads the activate polygon circle for the enemy
	 */
	private void reloadActivateCircle() {
		if (mEditorActive) {
			clearPolygonOutline();

			ArrayList<Vector2> vertices = getDef().getVisual().getPolygonShape();
			if (vertices != null && !vertices.isEmpty()) {
				float radius = SkinNames.getResource(SkinNames.EditorVars.ENEMY_ACTIVATE_ON_TEST_RUN_RADIUS);
				ArrayList<Vector2> circleLines = Geometry.createCircle(radius);

				// Calculate center
				Vector2 center = new Vector2();
				for (Vector2 vertex : vertices) {
					center.add(vertex);
				}
				center.scl(1 / vertices.size());

				// Offset to center
				for (Vector2 vertex : circleLines) {
					vertex.add(center);
				}

				EarClippingTriangulator earClippingTriangulator = new EarClippingTriangulator();
				mActivateCircle = earClippingTriangulator.computeTriangles(circleLines);
				Collections.reverse(mActivateCircle);
			}
		}
	}

	@Override
	public RenderOrders getRenderOrder() {
		return RenderOrders.ENEMY;
	}

	/** Polygon line for drawing wider outline */
	private ArrayList<Vector2> mActivateCircle = null;
	/** Enemy weapon */
	private Weapon mWeapon = new Weapon();
	/** Shooting angle (used when rotating) */
	private float mShootAngle = 0;

	// Group
	/** Group of the enemy, null if the enemy doesn't belong to a group */
	@Tag(75) private EnemyGroup mGroup = null;
	/** If this enemy is the first in the group */
	private boolean mGroupLeader = false;

	// AI MOVEMENT
	/** Next random move time */
	private float mRandomMoveNext = 0;
	/** Direction of the current random move */
	private Vector2 mRandomMoveDirection = new Vector2();


	// PATH MOVEMENT
	/** Path we're currently following */
	private Path mPath = null;
	/** Path id, used when saving/loading enemy actor as it does not save the path */
	/** Index of path we're heading to */
	private int mPathIndexNext = -1;
	/** If the enemy is moving in the path direction */
	private boolean mPathForward = true;
	/** ONCE path reach end */
	private boolean mPathOnceReachedEnd = false;
	/** Last direction */
	private Vector2 mTargetDirection = new Vector2();
}
