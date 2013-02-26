package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Json;
import com.spiddekauga.utils.Maths;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.ITriggerListener;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.Path.PathTypes;
import com.spiddekauga.voider.game.TriggerAction;
import com.spiddekauga.voider.game.TriggerInfo;
import com.spiddekauga.voider.game.Weapon;
import com.spiddekauga.voider.game.actors.EnemyActorDef.AimTypes;
import com.spiddekauga.voider.game.actors.EnemyActorDef.MovementTypes;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Geometry;

/**
 * An enemy actor, these can generally shoot on the player.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EnemyActor extends Actor implements ITriggerListener {
	/**
	 * Default constructor
	 */
	public EnemyActor() {
		// Does nothing
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

		// Update movement
		EnemyActorDef def = getDef(EnemyActorDef.class);
		if (def != null && def.getMovementType() != null && getBody() != null) {
			switch (def.getMovementType()) {
			case PATH:
				updatePathMovement(deltaTime);
				break;

			case AI:
				updateAiMovement(deltaTime);
				break;

			case STATIONARY:
				// Does nothing
				break;
			}
		}

		// Update weapon
		if (def.hasWeapon()) {
			updateWeapon(deltaTime);
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
	 * Adds a trigger to the enemy actor
	 * @param triggerInfo trigger information
	 */
	public void addTrigger(TriggerInfo triggerInfo) {
		mTriggerInfos.add(triggerInfo);

		if (mGroupLeader && mGroup != null) {
			mGroup.addTrigger(triggerInfo);
		}
	}

	/**
	 * Removes the specified trigger from this enemy
	 * @param triggerInfo trigger information
	 */
	public void removeTrigger(TriggerInfo triggerInfo) {
		mTriggerInfos.remove(triggerInfo);

		if (mGroupLeader && mGroup != null) {
			mGroup.removeTrigger(triggerInfo);
		}
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
			velocity.mul(speed);
			getBody().setLinearVelocity(velocity);
		}
	}

	@Override
	public void setPosition(Vector2 position) {
		super.setPosition(position);

		// Set position of other actors in the group
		if (mGroupLeader && mGroup != null) {

		}
	}

	/**
	 * Resets the weapon
	 */
	public void resetWeapon() {
		mWeapon.setWeaponDefResetCd(getDef(EnemyActorDef.class).getWeaponDef());

		mShootAngle = getDef(EnemyActorDef.class).getAimStartAngle();
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
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		super.read(json, jsonData);

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

			if (jsonData.containsKey("mPathOnceReachedEnd")) {
				mPathOnceReachedEnd = json.readValue("mPathOnceReachedEnd", boolean.class, jsonData);
			}
			if (jsonData.containsKey("mPathForward")) {
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
	public ArrayList<UUID> getReferences() {
		ArrayList<UUID> references = new ArrayList<UUID>();

		if (mPathId != null) {
			references.add(mPathId);
		}
		if (mGroupId != null) {
			references.add(mGroupId);
		}

		return references;
	}

	@Override
	public void bindReference(IResource resource) {
		if (resource.equals(mPathId)) {
			setPath((Path) resource);
		} else if (resource.equals(mGroupId)) {
			mGroup = (EnemyGroup) resource;
		} else {
			Gdx.app.error("EnemyActor", "Couldn't find any resource to bind for " + resource.getId());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <ResourceType> ResourceType copy() {
		EnemyActor copy = super.copy();

		// Set variables that aren't copied by default
		copy.mPath = mPath;
		copy.mGroup = mGroup;

		// Always make copy not a group leader
		copy.mGroupLeader = false;

		return (ResourceType) copy;
	}

	@Override
	public void onTriggered(TriggerAction action) {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<TriggerInfo> getTriggerInfos() {
		return mTriggerInfos;
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

			Pools.free(shootDirection);
		}
	}

	/**
	 * @return direction which we want to shoot in. Be sure to free this
	 * vector using Pools.free(vector);.
	 */
	private Vector2 getShootDirection() {
		Vector2 shootDirection = Pools.obtain(Vector2.class);

		switch (getDef(EnemyActorDef.class).getAimType()) {
		case ON_PLAYER:
			shootDirection.set(mPlayerActor.getPosition()).sub(getPosition());
			break;

		case MOVE_DIRECTION:
			shootDirection.set(getBody().getLinearVelocity());
			break;

		case IN_FRONT_OF_PLAYER: {
			Vector2 playerVelocity = mPlayerActor.getBody().getLinearVelocity();

			boolean targetPlayerInstead = false;

			// If velocity is standing still, just shoot at the player...
			if (playerVelocity.len2() == 0) {
				targetPlayerInstead = true;
			}
			// Calculate where the bullet would intersect with the player
			else {
				Vector2 bulletVelocity = Geometry.interceptTarget(getPosition(), mWeapon.getDef().getBulletSpeed(), mPlayerActor.getPosition(), playerVelocity);
				shootDirection.set(bulletVelocity);
				Pools.free(bulletVelocity);

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

			Vector2 velocity = Pools.obtain(Vector2.class);
			velocity.set(mPath.getCornerPosition(mPathIndexNext)).sub(getPosition());
			velocity.nor().mul(getDef(EnemyActorDef.class).getSpeed());
			getBody().setLinearVelocity(velocity);

			// Set angle
			if (getDef(EnemyActorDef.class).isTurning()) {
				getBody().setTransform(getPosition(), (float)Math.toRadians(velocity.angle()));
			}

			Pools.free(velocity);
		}


		if (isCloseToNextIndex()) {
			// Special case for ONCE and last index
			// Just continue straight forward, i.e do nothing
			if (mPath.getPathType() == PathTypes.ONCE && mPathIndexNext == mPath.getCornerCount() - 1) {
				mPathOnceReachedEnd = true;
				return;
			}

			calculateNextPathIndex();

			// No turning, change direction directly
			if (!getDef(EnemyActorDef.class).isTurning()) {
				Vector2 velocity = Pools.obtain(Vector2.class);
				velocity.set(mPath.getCornerPosition(mPathIndexNext)).sub(getPosition());
				velocity.nor().mul(getDef(EnemyActorDef.class).getSpeed());
				getBody().setLinearVelocity(velocity);
				Pools.free(velocity);
			}
		}


		if (getDef(EnemyActorDef.class).isTurning()) {
			Vector2 target = Pools.obtain(Vector2.class);
			target.set(mPath.getCornerPosition(mPathIndexNext)).sub(getPosition());
			moveToTarget(target, deltaTime);
		}
	}

	/**
	 * Update the AI movement
	 * @param deltaTime time elapsed since the last frame
	 */
	private void updateAiMovement(float deltaTime) {
		// Calculate distance to player
		Vector2 targetDirection = Pools.obtain(Vector2.class);
		targetDirection.set(mPlayerActor.getPosition()).sub(getPosition());
		float targetDistanceSq = targetDirection.len2();

		// Enemy too far from the player?
		if (targetDistanceSq > getDef(EnemyActorDef.class).getPlayerDistanceMaxSq()) {
			moveToTarget(targetDirection, deltaTime);
			resetRandomMove();
		}
		// Enemy too close to player?
		else if (targetDistanceSq < getDef(EnemyActorDef.class).getPlayerDistanceMinSq()) {
			targetDirection.mul(-1);
			moveToTarget(targetDirection, deltaTime);
			resetRandomMove();
		}
		// Enemy in range
		else {
			if (getDef(EnemyActorDef.class).isMovingRandomly()) {
				calculateRandomMove(deltaTime);
			} else {
				getBody().setLinearVelocity(mLevelSpeed, 0);
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
		Vector2 velocity = Pools.obtain(Vector2.class);
		velocity.set(targetDirection);
		velocity.nor().mul(getDef(EnemyActorDef.class).getSpeed());

		// Increase with level speed if AI movement
		if (getDef(EnemyActorDef.class).getMovementType() == MovementTypes.AI) {
			velocity.x += mLevelSpeed;
		}

		getBody().setLinearVelocity(velocity);
		Pools.free(velocity);
	}

	/**
	 * Moves to target using turning algorithm
	 * @param targetDirection direction we want the enemy to move in
	 * @param deltaTime time elapsed since last frame
	 */
	private void moveToTargetTurning(Vector2 targetDirection, float deltaTime) {
		Vector2 velocity = Pools.obtain(Vector2.class);
		velocity.set(getBody().getLinearVelocity());

		// Decrease with level speed if AI movement
		if (getDef(EnemyActorDef.class).getMovementType() == MovementTypes.AI) {
			velocity.x -= mLevelSpeed;
		}

		boolean noVelocity = velocity.len2() == 0;

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

		if (!Maths.approxCompare(diffAngle, Config.Actor.Enemy.TURN_ANGLE_MIN) || oppositeDirection) {
			float angleBefore = velocity.angle();
			float rotation = getDef(EnemyActorDef.class).getTurnSpeed() * deltaTime * getDef(EnemyActorDef.class).getSpeed();
			if (!counterClockwise) {
				rotation = -rotation;
			}

			float angleAfter = velocityAngleOriginal + rotation;

			if (noVelocity) {
				velocity.x = 1;
				velocity.setAngle(angleAfter);
				velocity.mul(getDef(EnemyActorDef.class).getSpeed());
			} else {
				velocity.rotate(rotation);
			}

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
				velocity.nor().mul(getDef(EnemyActorDef.class).getSpeed());
			}

			// Increase with level speed if AI movement
			if (getDef(EnemyActorDef.class).getMovementType() == MovementTypes.AI) {
				velocity.x += mLevelSpeed;
			}

			getBody().setLinearVelocity(velocity);
		}


		getBody().setTransform(getPosition(), (float) Math.toRadians(getBody().getLinearVelocity().angle()));

		Pools.free(velocity);
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
	 * @return true if the enemy is close to the next path index
	 */
	private boolean isCloseToNextIndex() {
		Vector2 diff = Pools.obtain(Vector2.class);
		diff.set(mPath.getCornerPosition(mPathIndexNext)).sub(getPosition());
		float distanceSq = diff.len2();
		Pools.free(diff);
		return distanceSq <= Config.Actor.Enemy.PATH_NODE_CLOSE_SQ;
	}


	/** Trigger informations */
	private ArrayList<TriggerInfo> mTriggerInfos = new ArrayList<TriggerInfo>();
	/** Enemy weapon */
	private Weapon mWeapon = new Weapon();
	/** Shooting angle (used when rotating) */
	private float mShootAngle = 0;

	// Group
	/** Group id, used for binding the group after loading the enemy */
	private UUID mGroupId = null;
	/** Group of the enemy, null if the enemy doesn't belong to a group */
	private EnemyGroup mGroup = null;
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
	private UUID mPathId = null;
	/** Index of path we're heading to */
	private int mPathIndexNext = -1;
	/** If the enemy is moving in the path direction */
	private boolean mPathForward = true;
	/** ONCE path reach end */
	private boolean mPathOnceReachedEnd = false;
}
