package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.Maths;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.Weapon;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimTypes;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.game.triggers.TActorActivated;
import com.spiddekauga.voider.game.triggers.TScreenAt;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;
import com.spiddekauga.voider.game.triggers.TriggerInfo;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Pools;

/**
 * An enemy actor, these can generally shoot on the player.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
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
		Pools.vector2.free(mTargetDirection);
		Pools.vector2.free(mRandomMoveDirection);
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

		if (isActive()) {
			// Update movement
			EnemyActorDef def = getDef(EnemyActorDef.class);
			if (def != null && def.getMovementType() != null && getBody() != null) {
				switch (def.getMovementType()) {
				case PATH:
					if (mPath != null) {
						updatePathMovement(deltaTime);
						if (!mEditorActive) {
							checkPathDeactivate();
						}
					}
					break;

				case AI:
					updateAiMovement(deltaTime);
					break;

				case STATIONARY:
					if (!mEditorActive) {
						checkStationaryDeactivate();
					}
					break;
				}
			}

			// Update weapon
			if (def.hasWeapon()) {
				updateWeapon(deltaTime);
			}

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

			mPathId = path.getId();
			mPath.addEnemy(this);
		} else {
			mPathId = null;
		}
	}

	/**
	 * @return current path we're following, if we're not following any it returns null
	 */
	public Path getPath() {
		return mPath;
	}

	/**
	 * Sets the speed of the actor, although not the definition, so this is
	 * just a temporary speed
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
		float xCoord = getPosition().x - getDef().getVisualVars().getBoundingRadius();

		// Decrease position if we are in an enemy group
		if (mGroup != null) {
			float distancePerEnemy = level.getSpeed() * mGroup.getSpawnTriggerDelay();
			float offset = (mGroup.getEnemyCount() - 1) * distancePerEnemy;
			xCoord -= offset;
		}


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
		mWeapon.setWeaponDefResetCd(getDef(EnemyActorDef.class).getWeaponDef());

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
				output.writeFloat(mRandomMoveNext);
				kryo.writeObject(output, mRandomMoveDirection);
			}
		} else if (enemyDef.getMovementType() == MovementTypes.PATH) {
			kryo.writeObjectOrNull(output, mPath, Path.class);

			if (mPath != null) {
				output.writeInt(mPathIndexNext);

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
			mShootAngle = input.readFloat();
		}

		// Group
		if (mGroup != null) {
			mGroupLeader = input.readBoolean();
		}

		// Movement
		if (enemyDef.getMovementType() == MovementTypes.AI) {
			if (enemyDef.isMovingRandomly()) {
				mRandomMoveNext = input.readFloat();
				Pools.vector2.free(mRandomMoveDirection);
				mRandomMoveDirection = kryo.readObject(input, Vector2.class);
			}
		} else if (enemyDef.getMovementType() == MovementTypes.PATH) {
			mPath = kryo.readObjectOrNull(input, Path.class);

			if (mPath != null) {
				mPathIndexNext = input.readInt();

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

		EnemyActor enemyCopy = (EnemyActor)copy;
		enemyCopy.mPath = mPath;
		enemyCopy.mGroup = mGroup;

		// Never make a copy a group leader?
		enemyCopy.mGroupLeader = false;

		return copy;
	}

	@Override
	public void copy(Object fromOriginal) {
		super.copy(fromOriginal);

		EnemyActor fromEnemy = (EnemyActor)fromOriginal;

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

	@Override
	public void write(Json json) {
		super.write(json);

		EnemyActorDef enemyDef = getDef(EnemyActorDef.class);

		if (enemyDef.hasWeapon()) {
			json.writeValue("mWeapon", mWeapon);
			json.writeValue("mShootAngle", mShootAngle);
		}

		json.writeValue("mGroupId", mGroupId);
		if (mGroupId != null) {
			json.writeValue("mGroupLeader", mGroupLeader);
		}

		if (enemyDef.getMovementType() == MovementTypes.AI) {
			if (enemyDef.isMovingRandomly()) {
				json.writeValue("mRandomMoveNext", mRandomMoveNext);
				json.writeValue("mRandomMoveDirection", mRandomMoveDirection);
			}
		} else if (enemyDef.getMovementType() == MovementTypes.PATH) {
			json.writeValue("mPathId", getPathId());

			if (mPath != null) {
				json.writeValue("mPathIndexNext", mPathIndexNext);

				switch (mPath.getPathType()) {
				case ONCE:
					json.writeValue("mPathOnceReachedEnd", mPathOnceReachedEnd);
					break;

				case BACK_AND_FORTH:
					json.writeValue("mPathForward", mPathForward);
					break;

				case LOOP:
					// Does nothing
					break;
				}

			}
		}
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		super.read(json, jsonData);

		mGroupId = json.readValue("mGroupId", UUID.class, jsonData);
		if (mGroupId != null) {
			mGroupLeader = json.readValue("mGroupLeader", boolean.class, jsonData);
		}

		EnemyActorDef enemyDef = getDef(EnemyActorDef.class);

		if (enemyDef.hasWeapon()) {
			mWeapon = json.readValue("mWeapon", Weapon.class, jsonData);
			mWeapon.setWeaponDef(enemyDef.getWeaponDef());
			mShootAngle = json.readValue("mShootAngle", float.class, jsonData);
		}

		if (enemyDef.getMovementType() == MovementTypes.AI) {
			if (enemyDef.isMovingRandomly()) {
				mRandomMoveNext = json.readValue("mRandomMoveNext", int.class, jsonData);
				mRandomMoveDirection = json.readValue("mRandomMoveDirection", Vector2.class, jsonData);
			}
		}
		else if (enemyDef.getMovementType() == MovementTypes.PATH) {
			mPathId = json.readValue("mPathId", UUID.class, jsonData);

			if (jsonData.getChild("mPathIndexNext") != null) {
				mPathIndexNext = json.readValue("mPathIndexNext", int.class, jsonData);
			}
			if (jsonData.getChild("mPathOnceReachedEnd") != null) {
				mPathOnceReachedEnd = json.readValue("mPathOnceReachedEnd", boolean.class, jsonData);
			}
			if (jsonData.getChild("mPathForward") != null) {
				mPathForward = json.readValue("mPathForward", boolean.class, jsonData);
			}
		}
	}

	/**
	 * Resets the movement. This resets the movement to start from the beginning of the path.
	 * Only applicable for path movement
	 */
	public void resetPathMovement() {
		mPathIndexNext = -1;
		mPathForward = true;
		mPathOnceReachedEnd = false;
		if (getBody() != null) {
			getBody().setAngularVelocity(0);
			getBody().setTransform(getPosition(), getDef().getBodyDef().angle);
		}
	}

	/**
	 * @return path id used for the enemy actor, null if none is used
	 */
	public UUID getPathId() {
		if (mPath != null) {
			return mPath.getId();
		}
		else {
			return mPathId;
		}
	}

	/**
	 * @return the enemy group
	 */
	public EnemyGroup getEnemyGroup() {
		return mGroup;
	}

	/**
	 * @return the enemy group id
	 */
	public UUID getEnemyGroupId() {
		return mGroupId;
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
	public void getReferences(ArrayList<UUID> references) {
		super.getReferences(references);
		if (mPathId != null) {
			references.add(mPathId);
		}
		if (mGroupId != null) {
			references.add(mGroupId);
		}
	}

	@Override
	public boolean bindReference(IResource resource) {
		boolean success = super.bindReference(resource);

		if (resource.equals(mPathId)) {
			setPath((Path) resource);
			success = true;
		} else if (resource.equals(mGroupId)) {
			mGroup = (EnemyGroup) resource;
			success = true;
		}

		return success;
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
	 * @param enemyGroup the enemy group, set as null to "remove" it from
	 * any group
	 */
	void setEnemyGroup(EnemyGroup enemyGroup) {
		mGroup = enemyGroup;

		if (mGroup != null) {
			mGroupId = mGroup.getId();
		} else {
			mGroupLeader = false;
			mGroupId = null;
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
	 * @return direction which we want to shoot in. Be sure to free this
	 * vector using Pools.vector2.free(vector);.
	 */
	private Vector2 getShootDirection() {
		Vector2 shootDirection = Pools.vector2.obtain();

		switch (getDef(EnemyActorDef.class).getAimType()) {
		case ON_PLAYER:
			shootDirection.set(mPlayerActor.getPosition()).sub(getPosition());
			break;

		case MOVE_DIRECTION:
			shootDirection.set(getBody().getLinearVelocity());
			break;

		case IN_FRONT_OF_PLAYER: {
			Vector2 playerVelocity = mPlayerActor.getBody().getLinearVelocity();
			//			float levelSpeed = 0;
			//			if (!mEditorActive) {
			//				levelSpeed = mLevel.getSpeed();
			//			}
			//			playerVelocity.x -= levelSpeed;

			boolean targetPlayerInstead = false;

			// If velocity is standing still, just shoot at the player...
			float playerSpeedSq = playerVelocity.len2();
			if (Maths.floatEquals(playerSpeedSq, 0)) {
				targetPlayerInstead = true;
			}
			// Calculate where the bullet would intersect with the player
			else {
				Vector2 bulletVelocity = Geometry.interceptTarget(getPosition(), mWeapon.getDef().getBulletSpeed() /*+ levelSpeed*/, mPlayerActor.getPosition(), playerVelocity);
				shootDirection.set(bulletVelocity);
				Pools.vector2.free(bulletVelocity);

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
			shootDirection.set(1,0);
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
				getBody().setTransform(getPosition(), (float)Math.toRadians(mTargetDirection.angle()));
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
		}
		else if (getDef(EnemyActorDef.class).isTurning()) {
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

		//		// Increase with level speed
		//		if (!mEditorActive && getDef(EnemyActorDef.class).getMovementType() == MovementTypes.AI) {
		//			velocity.x += mLevel.getSpeed();
		//		}

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

		//		// Decrease with level speed
		//		if (!mEditorActive && getDef(EnemyActorDef.class).getMovementType() == MovementTypes.AI && !velocity.equals(Vector2.Zero)) {
		//			velocity.x -= mLevel.getSpeed();
		//		}

		boolean noVelocity = Maths.approxCompare(velocity.len2(), 0.01f);

		// Calculate angle between the vectors
		boolean counterClockwise = false;
		float bodyAngle = (float)Math.toDegrees(getBody().getAngle()) % 360;
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

			//			// Increase with level speed
			//			if (!mEditorActive && getDef(EnemyActorDef.class).getMovementType() == MovementTypes.AI) {
			//				velocity.x += mLevel.getSpeed();
			//			}

			getBody().setLinearVelocity(velocity);
		}


		getBody().setTransform(getPosition(), (float) Math.toRadians(getBody().getLinearVelocity().angle()));

		Pools.vector2.free(velocity);
	}


	/**
	 * Sets the next index to move to, takes into account what type
	 * the path is.
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
	 * Checks if the enemy shall be deactivated and destroyed when following an path.
	 * It will destroy the enemy when it never can reach go onto the screen again.
	 * @note Will do nothing if the enemy has a deactivate trigger.
	 */
	private void checkPathDeactivate() {
		if (TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_DEACTIVATE) == null) {
			if (getPath().getRightestCorner().x + getDef().getVisualVars().getBoundingRadius() < mLevel.getXCoord() - SceneSwitcher.getWorldWidth()) {

				// For once, check that the ship cannot be seen too
				boolean deactivate = false;
				if (getPath().getPathType() == PathTypes.ONCE) {
					Vector2 minPos = SceneSwitcher.getWorldMinCoordinates();
					Vector2 maxPos = SceneSwitcher.getWorldMaxCoordinates();

					// Left
					if (getPosition().x + getDef().getVisualVars().getBoundingRadius() < minPos.x) {
						deactivate = true;
					}
					// Right
					else if (getPosition().x - getDef().getVisualVars().getBoundingRadius() > maxPos.x) {
						deactivate = true;
					}
					// Bottom
					else if (getPosition().y + getDef().getVisualVars().getBoundingRadius() < minPos.y) {
						deactivate = true;
					}
					// Top
					else if (getPosition().y - getDef().getVisualVars().getBoundingRadius() > maxPos.y) {
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
	 * Checks if the enemy shall be deactivated and destroyed when it is stationary.
	 * It will destroy the enemy once it's outside of the screen.
	 * @note Will do nothing if the enemy has a deactivate trigger.
	 */
	private void checkStationaryDeactivate() {
		if (TriggerInfo.getTriggerInfoByAction(this, Actions.ACTOR_DEACTIVATE) == null) {
			if (getPosition().x + getDef().getVisualVars().getBoundingRadius() < mLevel.getXCoord() - SceneSwitcher.getWorldWidth()) {
				deactivate();
				destroyBody();
			}
		}
	}

	/** Enemy weapon */
	private Weapon mWeapon = new Weapon();
	/** Shooting angle (used when rotating) */
	private float mShootAngle = 0;

	// Group
	/** Group id, used for binding the group after loading the enemy */
	@Deprecated
	private UUID mGroupId = null;
	/** Group of the enemy, null if the enemy doesn't belong to a group */
	@Tag(75) private EnemyGroup mGroup = null;
	/** If this enemy is the first in the group */
	private boolean mGroupLeader = false;

	// AI MOVEMENT
	/** Next random move time */
	private float mRandomMoveNext = 0;
	/** Direction of the current random move */
	private Vector2 mRandomMoveDirection = Pools.vector2.obtain().set(0,0);


	// PATH MOVEMENT
	/** Path we're currently following */
	private Path mPath = null;
	/** Path id, used when saving/loading enemy actor as it does not save the path */
	@Deprecated
	private UUID mPathId = null;
	/** Index of path we're heading to */
	private int mPathIndexNext = -1;
	/** If the enemy is moving in the path direction */
	private boolean mPathForward = true;
	/** ONCE path reach end */
	private boolean mPathOnceReachedEnd = false;
	/** Last direction */
	private Vector2 mTargetDirection = Pools.vector2.obtain().set(0,0);
}
