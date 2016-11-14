package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Collision;
import com.spiddekauga.voider.config.IC_Editor.IC_Enemy.IC_Movement;
import com.spiddekauga.voider.config.IC_Editor.IC_Enemy.IC_Weapon;
import com.spiddekauga.voider.game.WeaponDef;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.network.resource.EnemyDefEntity;
import com.spiddekauga.voider.resources.Resource;

/**
 * Enemy actor definition, does nothing more than specify that the actor is an enemy
 */
public class EnemyActorDef extends ActorDef {
@Tag(53)
private boolean mHasWeapon = false;
@Tag(55)
private WeaponDef mWeapon = new WeaponDef();
@Tag(56)
private AimTypes mAimType = AimTypes.MOVE_DIRECTION;
@Tag(57)
private AimRotateVars mAimRotateVars = new AimRotateVars();
@Tag(54)
private MovementTypes mMovementType = MovementTypes.PATH;
@Tag(58)
private MovementVars mMovementVars = new MovementVars();
@Tag(59)
private AiMovementVars mAiMovementVars = new AiMovementVars();

/**
 * Private default constructor used for kryo
 */
public EnemyActorDef() {
	super(ActorTypes.ENEMY);

	IC_Collision icCollision = ConfigIni.getInstance().editor.actor.collision;
	IC_Movement icMovement = ConfigIni.getInstance().editor.enemy.movement;

	setCollisionDamage(icCollision.getDamageDefault());
	setDestroyOnCollide(icCollision.getDestroyByDefault());
	getBodyDef().type = BodyType.KinematicBody;
	getBodyDef().fixedRotation = icMovement.isTurningByDefault();
}

@Override
public void set(Resource resource) {
	super.set(resource);

	EnemyActorDef def = (EnemyActorDef) resource;
	mAiMovementVars = def.mAiMovementVars;
	mAimRotateVars = def.mAimRotateVars;
	mAimType = def.mAimType;
	mHasWeapon = def.mHasWeapon;
	mMovementType = def.mMovementType;
	mMovementVars = def.mMovementVars;
	mWeapon = def.mWeapon;
}

@Override
protected void setNewDefEntity(DefEntity defEntity, boolean toOnline) {
	super.setNewDefEntity(defEntity, toOnline);

	EnemyDefEntity enemyDefEntity = (EnemyDefEntity) defEntity;
	enemyDefEntity.hasWeapon = mHasWeapon && mWeapon.getBulletActorDef() != null;
	enemyDefEntity.collisionDamage = getCollisionDamage();
	enemyDefEntity.destroyOnCollide = isDestroyedOnCollide();
	enemyDefEntity.movementType = mMovementType;

	// Movement
	if (mMovementType != MovementTypes.STATIONARY) {
		enemyDefEntity.movementSpeed = mMovementVars.speed;
	}

	// Weapon
	if (enemyDefEntity.hasWeapon) {
		enemyDefEntity.aimType = mAimType;
		enemyDefEntity.bulletDamage = mWeapon.getDamage();
		enemyDefEntity.bulletSpeed = mWeapon.getBulletSpeed();
	}
}

@Override
protected DefEntity newDefEntity() {
	return new EnemyDefEntity();
}

/**
 * @return the current movement type of the enemy
 */
public MovementTypes getMovementType() {
	return mMovementType;
}

/**
 * Sets the movement type of the enemy
 * @param movementType what kind of movement the enemy will use
 */
public void setMovementType(MovementTypes movementType) {
	mMovementType = movementType;

	switch (mMovementType) {
	case PATH:
	case AI:
		getBodyDef().type = BodyType.KinematicBody;
		break;

	case STATIONARY:
		getBodyDef().type = BodyType.StaticBody;
		break;
	}

	mBodyChangeTime = GameTime.getTotalGlobalTimeElapsed();
}

/**
 * @return speed of the enemy
 */
public float getSpeed() {
	return mMovementVars.speed;
}

/**
 * Sets the speed of the enemy
 * @param speed the new speed of the enemy
 */
public void setSpeed(float speed) {
	mMovementVars.speed = speed;
}

/**
 * @return minimum distance from the player the enemy shall have
 */
public float getPlayerDistanceMin() {
	return mAiMovementVars.playerDistanceMin;
}

/**
 * Sets the minimum distance from the player the enemy shall have. Only applicable if the enemy
 * movement is set to AI
 * @param minDistance the minimum distance from the player
 */
public void setPlayerDistanceMin(float minDistance) {
	mAiMovementVars.playerDistanceMin = minDistance;
	mAiMovementVars.playerDistanceMinSq = mAiMovementVars.playerDistanceMin * mAiMovementVars.playerDistanceMin;
}

/**
 * @return Squared version of minimum distance from the player. This has been pre-calculated.
 */
public float getPlayerDistanceMinSq() {
	return mAiMovementVars.playerDistanceMinSq;
}

/**
 * @return maximum distance from the player the enemy shall have
 */
public float getPlayerDistanceMax() {
	return mAiMovementVars.playerDistanceMax;
}

/**
 * Sets the maximum distance from the player the enemy shall have. Only applicable if the enemy
 * movement is set to AI
 * @param maxDistance the maximum distance from the player
 */
public void setPlayerDistanceMax(float maxDistance) {
	mAiMovementVars.playerDistanceMax = maxDistance;
	mAiMovementVars.playerDistanceMaxSq = mAiMovementVars.playerDistanceMax * mAiMovementVars.playerDistanceMax;
}

/**
 * @return Squared version of maximum distance from the player. This has been pre-calculated.
 */
public float getPlayerDistanceMaxSq() {
	return mAiMovementVars.playerDistanceMaxSq;
}

/**
 * Sets if the enemy shall move randomly using the random spread set through
 * #setRandomSpread(float).
 * @param moveRandomly true if the enemy shall move randomly.
 */
public void setMoveRandomly(boolean moveRandomly) {
	mAiMovementVars.randomMove = moveRandomly;
}

/**
 * @return true if the enemy shall move randomly.
 * @see #setRandomTimeMin(float) to set how random the enemy shall move
 */
public boolean isMovingRandomly() {
	return mAiMovementVars.randomMove;
}

/**
 * @return Minimum time until next random move
 */
public float getRandomTimeMin() {
	return mAiMovementVars.randomTimeMin;
}

/**
 * Sets the minimum time that must have passed until the enemy will decide on another direction.
 * @param minTime how many degrees it will can move
 * @see #setMoveRandomly(boolean) to activate/deactivate the random movement
 */
public void setRandomTimeMin(float minTime) {
	mAiMovementVars.randomTimeMin = minTime;
}

/**
 * @return Maximum time until next random move
 */
public float getRandomTimeMax() {
	return mAiMovementVars.randomTimeMax;
}

/**
 * Sets the maximum time that must have passed until the enemy will decide on another direction.
 * @param maxTime how many degrees it will can move
 * @see #setMoveRandomly(boolean) to activate/deactivate the random movement
 */
public void setRandomTimeMax(float maxTime) {
	mAiMovementVars.randomTimeMax = maxTime;
}

/**
 * @return how many degrees the enemy can turn per second
 */
public float getTurnSpeed() {
	return mMovementVars.turnSpeed;
}

/**
 * Sets the turning speed of the enemy
 * @param degrees how many degrees it can turn per second
 */
public void setTurnSpeed(float degrees) {
	mMovementVars.turnSpeed = degrees;
}

/**
 * Sets if the enemy shall turn
 * @param turn true if enemy shall turn
 */
public void setTurn(boolean turn) {
	getBodyDef().fixedRotation = !turn;
}

/**
 * @return true if the enemy turns
 */
public boolean isTurning() {
	return !getBodyDef().fixedRotation;
}

/**
 * Sets if this enemy has a weapon or not
 * @param useWeapon set to true to make the enemy use a weapon
 * @see #hasWeapon()
 */
public void setUseWeapon(boolean useWeapon) {
	mHasWeapon = useWeapon;
}

/**
 * @return true if the enemy has a weapon
 * @see #setUseWeapon(boolean)
 */
public boolean hasWeapon() {
	return mHasWeapon;
}

/**
 * @return weapon of the enemy
 */
public WeaponDef getWeaponDef() {
	return mWeapon;
}

/**
 * @return the aim type of the enemy
 */
public AimTypes getAimType() {
	return mAimType;
}

/**
 * Sets the aim type of the enemy
 * @param aimType new aim type
 */
public void setAimType(AimTypes aimType) {
	mAimType = aimType;
}

/**
 * @return starting aim angle.
 */
public float getAimStartAngle() {
	return mAimRotateVars.startAngle;
}

/**
 * Sets the starting aim angle, when rotating
 * @param angle starting angle of aim.
 */
public void setAimStartAngle(float angle) {
	mAimRotateVars.startAngle = angle;
}

/**
 * @return aim's rotation speed.
 */
public float getAimRotateSpeed() {
	return mAimRotateVars.rotateSpeed;
}

/**
 * Sets the aim's rotation speed. Only applicable when aim is set to rotating.
 * @param rotateSpeed new rotation speed
 */
public void setAimRotateSpeed(float rotateSpeed) {
	mAimRotateVars.rotateSpeed = rotateSpeed;
}

/**
 * Class for all movement variables (both AI and path)
 */
public static class MovementVars {
	@Tag(64)
	private float speed;
	@Tag(65)
	private float turnSpeed;

	{
		IC_Movement icMovement = ConfigIni.getInstance().editor.enemy.movement;
		speed = icMovement.getMoveSpeedDefault();
		turnSpeed = icMovement.getTurnSpeedDefault();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(speed);
		result = prime * result + Float.floatToIntBits(turnSpeed);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MovementVars other = (MovementVars) obj;
		if (Float.floatToIntBits(speed) != Float.floatToIntBits(other.speed)) {
			return false;
		}
		if (Float.floatToIntBits(turnSpeed) != Float.floatToIntBits(other.turnSpeed)) {
			return false;
		}
		return true;
	}


}

/**
 * Class for all AI movement variables
 */
public static class AiMovementVars {
	@Tag(66)
	private float playerDistanceMin;
	@Tag(67)
	private float playerDistanceMinSq;
	@Tag(68)
	private float playerDistanceMax;
	@Tag(69)
	private float playerDistanceMaxSq;
	@Tag(70)
	private boolean randomMove;
	@Tag(71)
	private float randomTimeMin;
	@Tag(72)
	private float randomTimeMax;

	{
		IC_Movement icMovement = ConfigIni.getInstance().editor.enemy.movement;
		playerDistanceMin = icMovement.getAiDistanceMinDefault();
		playerDistanceMinSq = playerDistanceMin * playerDistanceMin;
		playerDistanceMax = icMovement.getAiDistanceMaxDefault();
		playerDistanceMaxSq = playerDistanceMax * playerDistanceMax;
		randomMove = icMovement.isRandomMovementOnDefault();
		randomTimeMin = icMovement.getRandomMovementTimeMinDefault();
		randomTimeMax = icMovement.getRandomMovementTimeMaxDefault();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(playerDistanceMax);
		result = prime * result + Float.floatToIntBits(playerDistanceMaxSq);
		result = prime * result + Float.floatToIntBits(playerDistanceMin);
		result = prime * result + Float.floatToIntBits(playerDistanceMinSq);
		result = prime * result + (randomMove ? 1231 : 1237);
		result = prime * result + Float.floatToIntBits(randomTimeMax);
		result = prime * result + Float.floatToIntBits(randomTimeMin);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AiMovementVars other = (AiMovementVars) obj;
		if (Float.floatToIntBits(playerDistanceMax) != Float.floatToIntBits(other.playerDistanceMax)) {
			return false;
		}
		if (Float.floatToIntBits(playerDistanceMaxSq) != Float.floatToIntBits(other.playerDistanceMaxSq)) {
			return false;
		}
		if (Float.floatToIntBits(playerDistanceMin) != Float.floatToIntBits(other.playerDistanceMin)) {
			return false;
		}
		if (Float.floatToIntBits(playerDistanceMinSq) != Float.floatToIntBits(other.playerDistanceMinSq)) {
			return false;
		}
		if (randomMove != other.randomMove) {
			return false;
		}
		if (Float.floatToIntBits(randomTimeMax) != Float.floatToIntBits(other.randomTimeMax)) {
			return false;
		}
		if (Float.floatToIntBits(randomTimeMin) != Float.floatToIntBits(other.randomTimeMin)) {
			return false;
		}
		return true;
	}


}

/**
 * Class for aim rotating variables
 */
public static class AimRotateVars {
	@Tag(73)
	private float startAngle;
	@Tag(74)
	private float rotateSpeed;

	{
		IC_Weapon icWeapon = ConfigIni.getInstance().editor.enemy.weapon;
		startAngle = icWeapon.getStartAngleDefault();
		rotateSpeed = icWeapon.getRotateSpeedDefault();
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(rotateSpeed);
		result = prime * result + Float.floatToIntBits(startAngle);
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AimRotateVars other = (AimRotateVars) obj;
		if (Float.floatToIntBits(rotateSpeed) != Float.floatToIntBits(other.rotateSpeed)) {
			return false;
		}
		if (Float.floatToIntBits(startAngle) != Float.floatToIntBits(other.startAngle)) {
			return false;
		}
		return true;
	}
}
}

