package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
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
	private float mSpeed = 10f;
}
