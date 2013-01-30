package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.ActorDef;

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
		mSpeed = speed;
	}

	/**
	 * @return speed of the enemy
	 */
	public float getSpeed() {
		return mSpeed;
	}

	/**
	 * Sets the minimum distance from the player the enemy shall have.
	 * Only applicable if the enemy movement is set to AI
	 * @param minDistance the minimum distance from the player
	 */
	public void setPlayerDistanceMin(float minDistance) {
		mPlayerDistanceMin = minDistance;
		mPlayerDistanceMinSq = mPlayerDistanceMin * mPlayerDistanceMin;
	}

	/**
	 * @return minimum distance from the player the enemy shall have
	 */
	public float getPlayerDistanceMin() {
		return mPlayerDistanceMin;
	}

	/**
	 * @return Squared version of minimum distance from the player.
	 * This has been pre-calculated.
	 */
	public float getPlayerDistanceMinSq() {
		return mPlayerDistanceMinSq;
	}

	/**
	 * Sets the maximum distance from the player the enemy shall have.
	 * Only applicable if the enemy movement is set to AI
	 * @param maxDistance the maximum distance from the player
	 */
	public void setPlayerDistanceMax(float maxDistance) {
		mPlayerDistanceMax = maxDistance;
		mPlayerDistanceMaxSq = mPlayerDistanceMax * mPlayerDistanceMax;
	}

	/**
	 * @return maximum distance from the player the enemy shall have
	 */
	public float getPlayerDistanceMax() {
		return mPlayerDistanceMax;
	}

	/**
	 * @return Squared version of maximum distance from the player.
	 * This has been pre-calculated.
	 */
	public float getPlayerDistanceMaxSq() {
		return mPlayerDistanceMaxSq;
	}

	/**
	 * Sets the turning speed of the enemy
	 * @param degrees how many degrees it can turn per second
	 */
	public void setTurnSpeed(float degrees) {
		mTurnSpeed = degrees;
	}

	/**
	 * @return how many degrees the enemy can turn per second
	 */
	public float getTurnSpeed() {
		return mTurnSpeed;
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

	@Override
	public void write(Json json) {
		json.writeObjectStart("ActorDef");
		super.write(json);
		json.writeObjectEnd();

		/** @TODO write object */
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		OrderedMap<String, Object> superMap = json.readValue("ActorDef", OrderedMap.class, jsonData);
		super.read(json, superMap);

		/** @TODO read object */
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

	/** What type of movement the enemy has */
	private MovementTypes mMovementType = MovementTypes.PATH;
	/** Speed of the enemy */
	private float mSpeed = Config.Editor.Enemy.MOVE_SPEED_DEFAULT;
	/** How fast the enemy can turn */
	private float mTurnSpeed = Config.Editor.Enemy.TURN_SPEED_DEFAULT;
	/** Minimum distance from the player */
	private float mPlayerDistanceMin = Config.Editor.Enemy.AI_DISTANCE_MIN_DEFAULT;
	/** Minimum distance from player, squared */
	private float mPlayerDistanceMinSq = mPlayerDistanceMin * mPlayerDistanceMin;
	/** Maximum distance from the player */
	private float mPlayerDistanceMax = Config.Editor.Enemy.AI_DISTANCE_MAX_DEFAULT;
	/** Maximum distance from the player, squared */
	private float mPlayerDistanceMaxSq = mPlayerDistanceMax * mPlayerDistanceMax;
}
