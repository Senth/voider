package com.spiddekauga.voider.game;

import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.game.actors.BulletActorDef;

/**
 * Holds all the necessary information about a weapon
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class WeaponDef {
	/**
	 * Sets the bullet speed
	 * @param speed new bullet speed
	 */
	public void setBulletSpeed(float speed) {
		mBulletSpeed = speed;
	}

	/**
	 * @return the bullet speed
	 */
	public float getBulletSpeed() {
		return mBulletSpeed;
	}

	/**
	 * Sets the weapon damage
	 * @param damage how much damage the bullets will take when they hit something
	 */
	public void setDamage(float damage) {
		mDamage = damage;
	}

	/**
	 * @return weapon damage
	 */
	public float getDamage() {
		return mDamage;
	}

	/**
	 * Sets the minimum weapon cooldown. If this is equal to the max value set
	 * through #setCooldownMax(float) it will always have the same cooldown; if not
	 * it will get a random cooldown between min and max time.
	 * @param minCooldown minimum cooldown.
	 */
	public void setCooldownMin(float minCooldown) {
		mCooldownMin = minCooldown;
	}

	/**
	 * @return minimum cooldown time
	 */
	public float getCooldownMin() {
		return mCooldownMin;
	}

	/**
	 * Sets the maximum weapon cooldown. If this is equal to the min value set
	 * through #setCooldownMin(float) it will always have the same cooldown; if not
	 * it will get a random cooldown between min and max time.
	 * @param maxCooldown maximum cooldown.
	 */
	public void setCooldownMax(float maxCooldown) {
		mCooldownMax = maxCooldown;
	}

	/**
	 * @return maximum cooldown time
	 */
	public float getCooldownMax() {
		return mCooldownMax;
	}

	/**
	 * Sets the bullet actor definition. The look of bullets.
	 * @param bulletActorDef bullet definition
	 */
	public void setBulletActorDef(BulletActorDef bulletActorDef) {
		mBulletActorDef = bulletActorDef;
	}

	/**
	 * @return the bullet actor definition, i.e. the look of the bullets.
	 */
	public BulletActorDef getBulletActorDef() {
		return mBulletActorDef;
	}

	/** Type and visuals of the bullet. @todo remove default bullet actor def*/
	private BulletActorDef mBulletActorDef = new BulletActorDef();
	/** Bullet speed */
	private float mBulletSpeed = Editor.Weapon.BULLET_SPEED_DEFAULT;
	/** Damage when bullet hits */
	private float mDamage = Editor.Weapon.DAMAGE_DEFAULT;
	/** Minimum weapon cooldown */
	private float mCooldownMin = Editor.Weapon.COOLDOWN_MIN_DEFAULT;
	/** Maximum weapon coolown */
	private float mCooldownMax = Editor.Weapon.COOLDOWN_MAX_DEFAULT;
}
