package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor.Enemy;
import com.spiddekauga.voider.game.ActorDef;
import com.spiddekauga.voider.game.Weapon;

/**
 * Enemy actor definition, does nothing more than specify that the actor is
 * an enemy
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EnemyActorDef extends ActorDef {
	/**
	 * Private default constructor used for json
	 */
	public EnemyActorDef() {
		getBodyDef().type = BodyType.KinematicBody;
		getBodyDef().fixedRotation = true;

		// Create default fixture
		FixtureDef fixtureDef = new FixtureDef();
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(1.0f);
		fixtureDef.friction = 0.0f;
		fixtureDef.restitution = 0.1f;
		fixtureDef.density = 0.001f;
		fixtureDef.shape = circleShape;
		addFixtureDef(fixtureDef);
	}

	/**
	 * Sets the movement type of the enemy
	 * @param movementType what kind of movement the enemy will use
	 */
	public void setMovementType(MovementTypes movementType) {
		mMovementType = movementType;
	}

	/**
	 * @return the current movement type of the enemy
	 */
	public MovementTypes getMovementType() {
		return mMovementType;
	}

	/**
	 * Sets the speed of the enemy
	 * @param speed the new speed of the enemy
	 */
	public void setSpeed(float speed) {
		mMovementVars.speed = speed;
	}

	/**
	 * @return speed of the enemy
	 */
	public float getSpeed() {
		return mMovementVars.speed;
	}

	/**
	 * Sets if the enemy shall stay on the screen once it has entered it.
	 * Only applicable for AI movement
	 * @param stayOnScreen true if the enemy shall stay on the screen.
	 */
	public void setStayOnScreen(boolean stayOnScreen) {
		mAiMovementVars.stayOnScreen = stayOnScreen;
	}

	/**
	 * @return true if the enemy shall stay on the screen once it has
	 * entered it. Only applicable for AI movement.
	 */
	public boolean shallStayOnScreen() {
		return mAiMovementVars.stayOnScreen;
	}

	/**
	 * Sets the minimum distance from the player the enemy shall have.
	 * Only applicable if the enemy movement is set to AI
	 * @param minDistance the minimum distance from the player
	 */
	public void setPlayerDistanceMin(float minDistance) {
		mAiMovementVars.playerDistanceMin = minDistance;
		mAiMovementVars.playerDistanceMinSq = mAiMovementVars.playerDistanceMin * mAiMovementVars.playerDistanceMin;
	}

	/**
	 * @return minimum distance from the player the enemy shall have
	 */
	public float getPlayerDistanceMin() {
		return mAiMovementVars.playerDistanceMin;
	}

	/**
	 * @return Squared version of minimum distance from the player.
	 * This has been pre-calculated.
	 */
	public float getPlayerDistanceMinSq() {
		return mAiMovementVars.playerDistanceMinSq;
	}

	/**
	 * Sets the maximum distance from the player the enemy shall have.
	 * Only applicable if the enemy movement is set to AI
	 * @param maxDistance the maximum distance from the player
	 */
	public void setPlayerDistanceMax(float maxDistance) {
		mAiMovementVars.playerDistanceMax = maxDistance;
		mAiMovementVars.playerDistanceMaxSq = mAiMovementVars.playerDistanceMax * mAiMovementVars.playerDistanceMax;
	}

	/**
	 * @return maximum distance from the player the enemy shall have
	 */
	public float getPlayerDistanceMax() {
		return mAiMovementVars.playerDistanceMax;
	}

	/**
	 * @return Squared version of maximum distance from the player.
	 * This has been pre-calculated.
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
	 * Sets the minimum time that must have passed until the enemy will decide
	 * on another direction.
	 * @param minTime how many degrees it will can move
	 * @see #setMoveRandomly(boolean) to activate/deactivate the random movement
	 */
	public void setRandomTimeMin(float minTime) {
		mAiMovementVars.randomTimeMin = minTime;
	}

	/**
	 * @return Minimum time until next random move
	 */
	public float getRandomTimeMin() {
		return mAiMovementVars.randomTimeMin;
	}

	/**
	 * Sets the maximum time that must have passed until the enemy will decide
	 * on another direction.
	 * @param maxTime how many degrees it will can move
	 * @see #setMoveRandomly(boolean) to activate/deactivate the random movement
	 */
	public void setRandomTimeMax(float maxTime) {
		mAiMovementVars.randomTimeMax = maxTime;
	}

	/**
	 * @return Maximum time until next random move
	 */
	public float getRandomTimeMax() {
		return mAiMovementVars.randomTimeMax;
	}

	/**
	 * Sets the turning speed of the enemy
	 * @param degrees how many degrees it can turn per second
	 */
	public void setTurnSpeed(float degrees) {
		mMovementVars.turnSpeed = degrees;
	}

	/**
	 * @return how many degrees the enemy can turn per second
	 */
	public float getTurnSpeed() {
		return mMovementVars.turnSpeed;
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
	public Weapon getWeapon() {
		return mWeapon;
	}

	@Override
	public void write(Json json) {
		json.writeValue("REVISION", Config.REVISION);

		json.writeObjectStart("ActorDef");
		super.write(json);
		json.writeObjectEnd();


		json.writeValue("mHasWeapon", mHasWeapon);
		json.writeValue("mMovementType", mMovementType);


		// Conditional variables to write
		if (mHasWeapon) {
			json.writeValue("mWeapon", mWeapon);
		}
		if (mMovementType != MovementTypes.STATIONARY) {
			json.writeValue("mMovementVars", mMovementVars);
		}
		if (mMovementType == MovementTypes.AI) {
			json.writeValue("mAiMovementVars", mAiMovementVars);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		OrderedMap<String, Object> superMap = json.readValue("ActorDef", OrderedMap.class, jsonData);
		super.read(json, superMap);


		mHasWeapon = json.readValue("mUniqueId", boolean.class, jsonData);
		mMovementType = json.readValue("mMovementType", MovementTypes.class, jsonData);


		// Conditional variables to read
		if (mHasWeapon) {
			mWeapon = json.readValue("mWeapon", Weapon.class, jsonData);
		}
		if (mMovementType == MovementTypes.AI) {
			mAiMovementVars = json.readValue("mAiMovementVars", AiMovementVars.class, jsonData);
		}
		if (mMovementType != MovementTypes.STATIONARY) {
			mMovementVars = json.readValue("mMovementVars", MovementVars.class, jsonData);
		}
	}

	/**
	 * Enumeration for all movement types
	 */
	public enum MovementTypes {
		/** Uses variable values to behave in a certain manner */
		AI,
		/** Follows a path */
		PATH,
		/** Stationary, cannot move */
		STATIONARY
	}

	/**
	 * @return enemy filter category
	 */
	@Override
	protected short getFilterCategory() {
		return FixtureFilterCategories.ENEMY;
	}

	/**
	 * Can collide only with other players
	 * @return colliding categories
	 */
	@Override
	protected short getFilterCollidingCategories() {
		return FixtureFilterCategories.PLAYER;
	}

	/** If the enemy has a weapon */
	private boolean mHasWeapon = false;
	/** Weapon of the enemy */
	private Weapon mWeapon = new Weapon();
	/** What type of movement the enemy has */
	private MovementTypes mMovementType = MovementTypes.PATH;
	/** Movement variables */
	private MovementVars mMovementVars = new MovementVars();
	/** AI movement variables */
	private AiMovementVars mAiMovementVars = new AiMovementVars();

	/**
	 * Class for all movement variables (both AI and path)
	 */
	private class MovementVars {
		/** Speed of the enemy */
		public float speed = Enemy.Movement.MOVE_SPEED_DEFAULT;
		/** How fast the enemy can turn */
		public float turnSpeed = Enemy.Movement.TURN_SPEED_DEFAULT;
	}

	/**
	 * Class for all AI movement variables
	 */
	private class AiMovementVars {
		/** Minimum distance from the player */
		public float playerDistanceMin = Enemy.Movement.AI_DISTANCE_MIN_DEFAULT;
		/** Minimum distance from player, squared */
		public float playerDistanceMinSq = playerDistanceMin * playerDistanceMin;
		/** Maximum distance from the player */
		public float playerDistanceMax = Enemy.Movement.AI_DISTANCE_MAX_DEFAULT;
		/** Maximum distance from the player, squared */
		public float playerDistanceMaxSq = playerDistanceMax * playerDistanceMax;
		/** If the enemy shall stay on the screen */
		public boolean stayOnScreen = Enemy.Movement.STAY_ON_SCREEN_DEFAULT;
		/** If the enemy shall move randomly when inside the preferred space */
		public boolean randomMove = Enemy.Movement.RANDOM_MOVEMENT_DEFAULT;
		/** Minimum time until next random move */
		public float randomTimeMin = Enemy.Movement.RANDOM_MOVEMENT_TIME_MIN_DEFAULT;
		/** Maxumum time until next random move */
		public float randomTimeMax = Enemy.Movement.RANDOM_MOVEMENT_TIME_MAX_DEFAULT;
	}
}
