package com.spiddekauga.voider.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.voider.game.actors.BulletActor;

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
	public void setWeaponDef(WeaponDef weaponDef) {
		mDef = weaponDef;
		mCooldown = 0;
	}

	/**
	 * Updates the weapon, this reduces the cooldown
	 * @param deltaTime elapsed time since last frame
	 */
	public void update(float deltaTime) {
		if (mCooldown > 0) {
			mCooldown -= deltaTime;
		}
	}

	/**
	 * @return true if the weapon can shoot, i.e. no cooldown left
	 */
	public boolean canShoot() {
		return mCooldown <= 0;
	}

	/**
	 * Shoots a bullet in the specified direction
	 * @param direction direction of the bullet
	 */
	public void shoot(Vector2 direction) {
		BulletActor bullet = Pools.obtain(BulletActor.class);
		bullet.setDef(getDef().getBulletActorDef());
		bullet.shoot(mPosition, direction, getDef().getBulletSpeed(), getDef().getDamage(), false);


		// Cooldown
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

	@Override
	public void dispose() {
		Pools.free(mPosition);
	}

	/**
	 * @return weapon definition
	 */
	public WeaponDef getDef() {
		return mDef;
	}

	/** Weapon definition */
	private WeaponDef mDef = null;
	/** Current cooldown timer */
	private float mCooldown = 0;
	/** Position of the weapon */
	private Vector2 mPosition = Pools.obtain(Vector2.class);
}
