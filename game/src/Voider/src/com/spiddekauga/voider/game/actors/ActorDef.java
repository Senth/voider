package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.game.Collectibles;
import com.spiddekauga.voider.resources.Def;

/**
 * Definition of the actor. This include common attribute for a common type of actor.
 * E.g. A specific enemy will have the same variables here. The only thing changed during
 * it's life is the variables in the Actor class.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class ActorDef extends Def implements Json.Serializable, Disposable {
	/**
	 * Sets the visual variable to the specified type
	 * @param actorType the actor type to which set the default values of
	 * the visual variables
	 */
	protected ActorDef(ActorTypes actorType) {
		mVisualVars = new VisualVars(actorType);

		/** @todo remove default color */
		switch (actorType) {
		case BULLET:
			mVisualVars.setColor(new Color(0.8f, 0.5f, 0, 1));
			break;

		case ENEMY:
			mVisualVars.setColor(new Color(1, 0, 0, 1));
			break;

		case PICKUP:
			mVisualVars.setColor(new Color(1, 1, 0, 1));
			break;

		case PLAYER:
			mVisualVars.setColor(new Color(0.25f, 1, 0.25f, 1));
			break;

		case STATIC_TERRAIN:
			mVisualVars.setColor(new Color(0, 0.5f, 0.1f, 1));
			break;

		default:
			mVisualVars.setColor(new Color(1, 1, 1, 1));
			break;
		}
	}

	/**
	 * Default constructor for JSON
	 */
	@SuppressWarnings("unused")
	private ActorDef() {
		// Does nothing
	}

	/**
	 * Sets the starting angle of the actor
	 * @param angle the starting angle, in radians
	 */
	public void setStartAngleRad(float angle) {
		getBodyDef().angle = angle;
		mBodyChangeTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * Sets the starting angle of the actor
	 * @param angle the starting angle, in degrees
	 */
	public void setStartAngleDeg(float angle) {
		setStartAngleRad(angle * MathUtils.degreesToRadians);
	}

	/**
	 * @return starting angle of the actor (in radians)
	 */
	public float getStartAngleRad() {
		return getBodyDef().angle;
	}

	/**
	 * @return starting angle of the actor (in degrees)
	 */
	public float getStartAngleDeg() {
		return getStartAngleRad() * MathUtils.radiansToDegrees;
	}

	/**
	 * Sets the collision damage of the actor
	 * If the actor is set to be destroyed on collision ({@link #setDestroyOnCollide(boolean)})
	 * it will decrease the full collisionDamage from the other actor and not per second.
	 * @param collisionDamage damage (per second) this actor will make to another when
	 * colliding actor.
	 * @return this for chaining commands
	 */
	public ActorDef setCollisionDamage(float collisionDamage) {
		mCollisionDamage = collisionDamage;
		return this;
	}

	/**
	 * @return the collision damage (per second) this actor will make to another
	 * colliding actor.
	 */
	public float getCollisionDamage() {
		return mCollisionDamage;
	}

	/**
	 * Sets the maximum life of the actor. I.e. starting amount of
	 * life.
	 * @param maxLife the maximum/starting amount of life.
	 * @return this for chaining commands
	 */
	public ActorDef setMaxLife(float maxLife) {
		mMaxLife = maxLife;
		return this;
	}

	/**
	 * @return Maximum life of the actor. I.e. starting amount of life
	 */
	public float getMaxLife() {
		return mMaxLife;
	}

	/**
	 * @return collectible of the actor def. Only works for PickupActorDef other
	 * actors defs returns null.
	 */
	public Collectibles getCollectible() {
		return null;
	}

	/**
	 * @return body definition of the actor
	 */
	public final BodyDef getBodyDef() {
		return mBodyDef;
	}

	/**
	 * @return a copy of the body definition
	 */
	public BodyDef getBodyDefCopy() {
		BodyDef copy = new BodyDef();
		copy.angle = mBodyDef.angle;
		copy.active = mBodyDef.active;
		copy.allowSleep = mBodyDef.allowSleep;
		copy.angularDamping = mBodyDef.angularDamping;
		copy.angularVelocity = mBodyDef.angularVelocity;
		copy.awake = mBodyDef.awake;
		copy.bullet = mBodyDef.bullet;
		copy.fixedRotation = mBodyDef.fixedRotation;
		copy.gravityScale = mBodyDef.gravityScale;
		copy.linearDamping = mBodyDef.linearDamping;
		copy.linearVelocity.set(mBodyDef.linearVelocity);
		copy.position.set(mBodyDef.position);
		copy.type = mBodyDef.type;
		return copy;
	}

	@Override
	public void dispose() {
		mVisualVars.dispose();
	}




	/**
	 * Sets whether this actor shall be destroyed on collision.
	 * If this actor has any collision damage set to it, it will decrease the
	 * other actors health with the whole amount instead of per second if this
	 * is set to true.
	 * @param destroyOnCollision set to true to destroy the actor on collision
	 */
	public void setDestroyOnCollide(boolean destroyOnCollision) {
		mDestroyOnCollide = destroyOnCollision;
	}

	/**
	 * @return true if this actor shall be destroyed on collision
	 */
	public boolean shallDestroyOnCollide() {
		return mDestroyOnCollide;
	}

	@Override
	public void write(Json json) {
		super.write(json);

		// Write ActorDef's variables first
		json.writeValue("mMaxLife", mMaxLife);
		json.writeValue("mBodyDef", mBodyDef);
		json.writeValue("mCollisionDamage", mCollisionDamage);
		json.writeValue("mDestroyOnCollide", mDestroyOnCollide);
		json.writeValue("mVisualVars", mVisualVars);

	}

	@Override
	public void read(Json json, JsonValue jsonValue) {
		super.read(json, jsonValue);


		// Our variables
		mMaxLife = json.readValue("mMaxLife", float.class, jsonValue);
		mBodyDef = json.readValue("mBodyDef", BodyDef.class, jsonValue);
		mCollisionDamage = json.readValue("mCollisionDamage", float.class, jsonValue);
		mDestroyOnCollide = json.readValue("mDestroyOnCollide", boolean.class, jsonValue);
		mVisualVars = json.readValue("mVisualVars", VisualVars.class, jsonValue);
	}


	/**
	 * @return when this definition was changed that affects the body.
	 */
	float getBodyChangeTime() {
		return mBodyChangeTime;
	}

	/**
	 * @return visual variables/parameters of the actor
	 */
	public VisualVars getVisualVars() {
		return mVisualVars;
	}

	/**
	 * Sets the rotation speed of the actor. This might not work for
	 * some actors that rotate the actor on their own...
	 * @param rotationSpeed new rotation speed of the actor. In radians.
	 */
	public void setRotationSpeedRad(float rotationSpeed) {
		getBodyDef().angularVelocity = rotationSpeed;
		mBodyChangeTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * Sets the rotation speed of the actor. This might not work for
	 * some actors that rotate the actor on their own...
	 * @param rotationSpeed new rotation speed of the actor. In degrees
	 */
	public void setRotationSpeedDeg(float rotationSpeed) {
		setRotationSpeedRad(rotationSpeed * MathUtils.degreesToRadians);
	}

	/**
	 * @return rotation speed of the actor, in radians
	 */
	public float getRotationSpeedRad() {
		return getBodyDef().angularVelocity;
	}

	/**
	 * @return rotation speed of the actor, in degrees
	 */
	public float getRotationSpeedDeg() {
		return getRotationSpeedRad() * MathUtils.radiansToDegrees;
	}

	/** When the body was changed last time */
	protected float mBodyChangeTime = 0;

	/** Maximum life of the actor, usually starting amount of life */
	@Tag(44) private float mMaxLife = 0;
	/** The body definition of the actor */
	@Tag(45) private BodyDef mBodyDef = new BodyDef();


	/** Collision damage (per second) */
	@Tag(46) private float mCollisionDamage = 0;
	/** If this actor shall be destroy on collision */
	@Tag(47) private boolean mDestroyOnCollide = false;
	/** Visual variables */
	@Tag(48) protected VisualVars mVisualVars = null;

}
