package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.voider.game.Actor;

/**
 * Bullet actor, contains necessary information about the bullet.
 * Not only the type of bullet, but speed, etc.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BulletActor extends Actor {
	/**
	 * Shoots the bullet
	 * @param position the original position of the bullet, i.e. where to shoot from
	 * @param direction the direction of the bullet
	 * @param speed of the bullet
	 * @param hitDamage how much life the bullet will inflict when it hits another actor
	 * @param shotByPlayer true if the player shot this bullet, false if the enemy shot this bullet
	 */
	public void shoot(Vector2 position, Vector2 direction, float speed, float hitDamage, boolean shotByPlayer) {
		mShotByPlayer = shotByPlayer;


		createBody();

		Vector2 velocity = Pools.obtain(Vector2.class);
		velocity.set(direction).nor().mul(speed);

		// Rotate the bullet to face the shooting direction
		double angle = velocity.angle();
		angle = Math.toRadians(angle);
		getBody().setTransform(position, (float) angle);
		getBody().setLinearVelocity(velocity);

		mDamage = hitDamage;
	}

	/**
	 * @return how much damage the bullet will inflict on hit
	 */
	public float getHitDamage() {
		return mDamage;
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
