package com.spiddekauga.voider.game;

import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;

/**
 * Holds all the necessary information about a weapon
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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
	 * Sets the minimum weapon cooldown. If this is equal to the max value set through
	 * #setCooldownMax(float) it will always have the same cooldown; if not it will get a
	 * random cooldown between min and max time.
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
	 * Sets the maximum weapon cooldown. If this is equal to the min value set through
	 * #setCooldownMin(float) it will always have the same cooldown; if not it will get a
	 * random cooldown between min and max time.
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
		if (mBulletActorDef != null) {
			mBulletActorDefId = bulletActorDef.getId();
		} else {
			mBulletActorDefId = null;
		}
	}

	/**
	 * @return the bullet actor definition, i.e. the look of the bullets.
	 */
	public BulletActorDef getBulletActorDef() {
		if (mBulletActorDef == null && mBulletActorDefId != null) {
			mBulletActorDef = ResourceCacheFacade.get(mBulletActorDefId);
		}

		return mBulletActorDef;
	}

	/**
	 * Set if the bullet speed should be relative to the level speed
	 * @param relativeToLevelSpeed true if the bullet speed should be relative to the
	 *        bullet speed
	 */
	public void setSpeedRelativeToLevel(boolean relativeToLevelSpeed) {
		mRelativeToLevelSpeed = relativeToLevelSpeed;
	}

	/**
	 * @return true if the bullet speed should be relative to the current level speed
	 */
	public boolean isSpeedRelativeToLevelSpeed() {
		return mRelativeToLevelSpeed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mBulletActorDef == null) ? 0 : mBulletActorDef.hashCode());
		result = prime * result + Float.floatToIntBits(mBulletSpeed);
		result = prime * result + Float.floatToIntBits(mCooldownMax);
		result = prime * result + Float.floatToIntBits(mCooldownMin);
		result = prime * result + Float.floatToIntBits(mDamage);
		result = prime * result + (mRelativeToLevelSpeed ? 1 : 0);
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
		WeaponDef other = (WeaponDef) obj;
		if (mBulletActorDef == null) {
			if (other.mBulletActorDef != null) {
				return false;
			}
		} else if (!mBulletActorDef.equals(other.mBulletActorDef)) {
			return false;
		}
		if (Float.floatToIntBits(mBulletSpeed) != Float.floatToIntBits(other.mBulletSpeed)) {
			return false;
		}
		if (Float.floatToIntBits(mCooldownMax) != Float.floatToIntBits(other.mCooldownMax)) {
			return false;
		}
		if (Float.floatToIntBits(mCooldownMin) != Float.floatToIntBits(other.mCooldownMin)) {
			return false;
		}
		if (Float.floatToIntBits(mDamage) != Float.floatToIntBits(other.mDamage)) {
			return false;
		}
		if (mRelativeToLevelSpeed != other.mRelativeToLevelSpeed) {
			return false;
		}
		return true;
	}

	/** Type and visuals of the bullet */
	private BulletActorDef mBulletActorDef = null;
	/** Id of the bullet actor */
	@Tag(104) private UUID mBulletActorDefId = null;
	/** Bullet speed */
	@Tag(92) private float mBulletSpeed = Editor.Weapon.BULLET_SPEED_DEFAULT;
	/** Damage when bullet hits */
	@Tag(93) private float mDamage = Editor.Weapon.DAMAGE_DEFAULT;
	/** Minimum weapon cooldown */
	@Tag(94) private float mCooldownMin = Editor.Weapon.COOLDOWN_MIN_DEFAULT;
	/** Maximum weapon cooldown */
	@Tag(95) private float mCooldownMax = Editor.Weapon.COOLDOWN_MAX_DEFAULT;
	/** The bullet speed is relative to the level speed */
	@Tag(143) private boolean mRelativeToLevelSpeed = true;
}
