package com.spiddekauga.voider.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Pools;
/**
 * Weapon that hadles the shooting and cooldown.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Weapon implements Disposable {
	/**
	 * Creates an invalid weapon. setWeaponDef needs to be called one can shoot with
	 * the weapon.
	 */
	public Weapon() {
		// Does nothing
	}

	/**
	 * Sets the weapon definition of the weapon. This resets the cooldown of the weapon
	 * @param weaponDef new weapon definition
	 */
	public void setWeaponDefResetCd(WeaponDef weaponDef) {
		mDef = weaponDef;
		calculateCooldown();
	}

	/**
	 * Sets the weapon definiotn of the weapon without resetting the cooldown
	 * @param weaponDef weapon definition
	 */
	public void setWeaponDef(WeaponDef weaponDef) {
		mDef = weaponDef;
	}

	/**
	 * @return a copy of this object
	 */
	public final Weapon copy() {
		Kryo kryo = Pools.kryo.obtain();
		Weapon copy = kryo.copy(this);
		Pools.kryo.free(kryo);

		copy.mDef = mDef;
		calculateCooldown();

		return copy;
	}

	/**
	 * Updates the weapon, this reduces the cooldown
	 * @param deltaTime elapsed time since last frame
	 */
	public void update(float deltaTime) {
		if (mCooldown > 0) {
			mCooldown -= deltaTime;
		} else {
			mCooldown = 0;
		}
	}

	/**
	 * @return true if the weapon can shoot, i.e. no cooldown left
	 */
	public boolean canShoot() {
		return mCooldown <= 0;
	}

	/**
	 * @return remaining cooldown of the weapon. 0 if the weapon can be shot now
	 */
	public float getCooldownTime() {
		return mCooldown;
	}

	/**
	 * Shoots a bullet in the specified direction
	 * @param direction direction of the bullet
	 */
	public void shoot(Vector2 direction) {
		if (getDef().getBulletActorDef() != null) {
			BulletActor bullet = Pools.bullet.obtain();
			bullet.setDef(getDef().getBulletActorDef());
			bullet.shoot(mPosition, direction, getDef().getBulletSpeed(), getDef().getDamage(), false);

			// Add to bullet destroyer
			SceneSwitcher.getBulletDestroyer().add(bullet);

			calculateCooldown();
		}
	}

	/**
	 * Calculates the cooldown, or next shooting time
	 */
	private void calculateCooldown() {
		// Random cooldown
		if (getDef().getCooldownMin() != getDef().getCooldownMax()) {
			mCooldown = (float) Math.random();
			mCooldown *= getDef().getCooldownMax() - getDef().getCooldownMin();
			mCooldown += getDef().getCooldownMin();
		}
		// Else always same cooldown
		else {
			mCooldown = getDef().getCooldownMax();
		}
	}

	/**
	 * Sets the position of the weapon. It will copy the position and use its
	 * own Vector2 for containing the data.
	 * @param position the position of the weapon.
	 */
	public void setPosition(Vector2 position) {
		mPosition.set(position);
	}

	/**
	 * @return position of the weapon
	 */
	public Vector2 getPosition() {
		return mPosition;
	}

	@Override
	public void dispose() {
		Pools.vector2.free(mPosition);
		mPosition = null;
	}

	/**
	 * @return weapon definition
	 */
	public WeaponDef getDef() {
		return mDef;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(mCooldown);
		result = prime * result + ((mDef == null) ? 0 : mDef.hashCode());
		result = prime * result + ((mPosition == null) ? 0 : mPosition.hashCode());
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
		Weapon other = (Weapon) obj;
		if (Float.floatToIntBits(mCooldown) != Float.floatToIntBits(other.mCooldown)) {
			return false;
		}
		if (mDef == null) {
			if (other.mDef != null) {
				return false;
			}
		}
		else if (!mDef.equals(other.mDef)) {
			return false;
		}
		if (mPosition == null) {
			if (other.mPosition != null) {
				return false;
			}
		}
		else if (!mPosition.equals(other.mPosition)) {
			return false;
		}
		return true;
	}

	/** Weapon definition */
	private WeaponDef mDef = null;
	/** Current cooldown timer */
	@Tag(89) private float mCooldown = 0;
	/** Position of the weapon */
	private Vector2 mPosition = Pools.vector2.obtain().set(0,0);
}
