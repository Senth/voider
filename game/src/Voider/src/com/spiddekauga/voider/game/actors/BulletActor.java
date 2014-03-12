package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.utils.Pools;

/**
 * Bullet actor, contains necessary information about the bullet.
 * Not only the type of bullet, but speed, etc.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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
		// Velocity
		Vector2 velocity = Pools.vector2.obtain();
		velocity.set(direction).nor().scl(speed);

		//		if (!mEditorActive) {
		//			velocity.x += mLevel.getSpeed();
		//		}
		shoot(position, velocity, hitDamage, shotByPlayer);
		Pools.vector2.free(velocity);
	}

	/**
	 * Shoots the bullet in the specified velocity.
	 * @param position the original position of the bullet, i.e. where to shoot from
	 * @param velocity velocity of the bullet
	 * @param hitDamage how much life the bullet will inflict when it hits another actor
	 * @param shotByPlayer true if the player shot this bullet, false if the enemy shot this bullet
	 * @see #shoot(Vector2,Vector2,float,float,boolean) to use a direction and speed to calculate the velocity
	 */
	private void shoot(Vector2 position, Vector2 velocity, float hitDamage, boolean shotByPlayer) {
		mShotByPlayer = shotByPlayer;

		activate();
		createBody();

		// Position and rotate the bullet to face the shooting direction
		double angle = velocity.angle();
		angle = Math.toRadians(angle) + getDef().getBodyDef().angle;
		getBody().setTransform(position, (float) angle);
		getBody().setLinearVelocity(velocity);
		setPosition(position);

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

	@Override
	public RenderOrders getRenderOrder() {
		return RenderOrders.BULLET;
	}

	/** True if bullet shot by player, false if shot by enemy */
	@Tag(8) private boolean mShotByPlayer = false;
	/** How much damage the bullet will inflict on hit */
	@Tag(9) private float mDamage = 0;
}
