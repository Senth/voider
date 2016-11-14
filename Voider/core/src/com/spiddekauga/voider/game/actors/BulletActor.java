package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.utils.Pools;

/**
 * Bullet actor, contains necessary information about the bullet. Not only the type of bullet, but
 * speed, etc.
 */
public class BulletActor extends Actor {
/** True if bullet shot by player, false if shot by enemy */
@Tag(8)
private boolean mShotByPlayer = false;
/** How much damage the bullet will inflict on hit */
@Tag(9)
private float mDamage = 0;

/**
 * Shoots the bullet. Automatically normalizes the direction vector to the speed.
 * @param position the original position of the bullet, i.e. where to shoot from
 * @param direction the direction of the bullet
 * @param speed of the bullet
 * @param hitDamage how much life the bullet will inflict when it hits another actor
 * @param relativeToLevelSpeed true if the bullet speed should be relative to the level speed
 * @param shotByPlayer true if the player shot this bullet, false if the enemy shot this bullet
 * @see #shoot(Vector2, Vector2, float, boolean) to use a velocity of the bullet directly
 */
public void shoot(Vector2 position, Vector2 direction, float speed, float hitDamage, boolean relativeToLevelSpeed, boolean shotByPlayer) {
	// Velocity
	Vector2 velocity = Pools.vector2.obtain();
	velocity.set(direction).nor().scl(speed);


	// Add speed from the level
	if (!mEditorActive && relativeToLevelSpeed) {
		float ratio = Math.abs(velocity.x / speed);
		float addSpeed = mLevel.getSpeed() * ratio;
		velocity.x += addSpeed;
	}


	shoot(position, velocity, hitDamage, shotByPlayer);
	Pools.vector2.free(velocity);
}

/**
 * Shoots the bullet in the specified velocity.
 * @param position the original position of the bullet, i.e. where to shoot from
 * @param velocity velocity of the bullet
 * @param hitDamage how much life the bullet will inflict when it hits another actor
 * @param shotByPlayer true if the player shot this bullet, false if the enemy shot this bullet
 * @see #shoot(Vector2, Vector2, float, float, boolean, boolean) to use a direction and speed to
 * calculate the velocity
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
	getBody().setLinearDamping(Config.Actor.Bullet.FRICTION);
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
}
