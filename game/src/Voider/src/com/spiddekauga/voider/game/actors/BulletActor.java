package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.utils.Vector2Pool;

/**
 * Bullet actor, contains necessary information about the bullet.
 * Not only the type of bullet, but speed, etc.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BulletActor extends Actor {
	/**
	 * Shoots the bullet. Automatically normalizes the direction vector to the speed.
	 * @param position the original position of the bullet, i.e. where to shoot from
	 * @param direction the direction of the bullet
	 * @param speed of the bullet
	 * @param hitDamage how much life the bullet will inflict when it hits another actor
	 * @param shotByPlayer true if the player shot this bullet, false if the enemy shot this bullet
	 * @see #shoot(Vector2,Vector2,float,boolean) to use a velocity of the bullet directly
	 */
	public void shoot(Vector2 position, Vector2 direction, float speed, float hitDamage, boolean shotByPlayer) {
		Vector2 velocity = Vector2Pool.obtain();
		velocity.set(direction).nor().mul(speed);
		shoot(position, velocity, hitDamage, shotByPlayer);
		Vector2Pool.free(velocity);
	}

	/**
	 * Shoots the bullet in the specified velocity.
	 * @param position the original position of the bullet, i.e. where to shoot from
	 * @param velocity velocity of the bullet
	 * @param hitDamage how much life the bullet will inflict when it hits another actor
	 * @param shotByPlayer true if the player shot this bullet, false if the enemy shot this bullet
	 * @see #shoot(Vector2,Vector2,float,float,boolean) to use a direction and speed to calculate the velocity
	 */
	public void shoot(Vector2 position, Vector2 velocity, float hitDamage, boolean shotByPlayer) {
		mShotByPlayer = shotByPlayer;

		createBody();

		// Position and rotate the bullet to face the shooting direction
		double angle = velocity.angle();
		angle = Math.toRadians(angle) + getDef().getBodyDef().angle;
		getBody().setTransform(position, (float) angle);
		getBody().setLinearVelocity(velocity);

		mDamage = hitDamage;
		activate();
	}

	/**
	 * @return how much damage the bullet will inflict on hit
	 */
	public float getHitDamage() {
		return mDamage;
	}

	@Override
	public void write(Json json) {
		super.write(json);

		json.writeValue("mShotByPlayer", mShotByPlayer);
		json.writeValue("mDamage", mDamage);
	}

	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		super.read(json, jsonData);

		mShotByPlayer = json.readValue("mShotByPlayer", boolean.class, jsonData);
		mDamage = json.readValue("mDamage", float.class, jsonData);
	}

	@Override
	protected short getFilterCategory() {
		if (mShotByPlayer) {
			return ActorFilterCategories.PLAYER;
		} else {
			return ActorFilterCategories.ENEMY;
		}
	}

	@Override
	protected short getFilterCollidingCategories() {
		if (mShotByPlayer) {
			return (short) (ActorFilterCategories.STATIC_TERRAIN | ActorFilterCategories.ENEMY);
		} else {
			return (short) (ActorFilterCategories.STATIC_TERRAIN | ActorFilterCategories.PLAYER);
		}
	}

	/** True if bullet shot by player, false if shot by enemy */
	private boolean mShotByPlayer = true;
	/** Hom much damage the bullet will inflict on hit */
	private float mDamage = 0;
}
