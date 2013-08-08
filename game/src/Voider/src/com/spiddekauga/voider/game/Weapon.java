package com.spiddekauga.voider.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonValue;
import com.spiddekauga.utils.JsonWrapper; import com.badlogic.gdx.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Pools;
/**
 * Weapon that hadles the shooting and cooldown.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Weapon implements Disposable, Json.Serializable {
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
		mCooldown = 0;
	}

	/**
	 * Sets the weapon definiotn of the weapon without resetting the cooldown
	 * @param weaponDef weapon definition
	 */
	public void setWeaponDef(WeaponDef weaponDef) {
		mDef = weaponDef;
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
		Pools.vector2.free(mPosition);
	}

	/**
	 * @return weapon definition
	 */
	public WeaponDef getDef() {
		return mDef;
	}

	@Override
	public void write(Json json) {
		json.writeValue("REVISION", Config.REVISION);
		json.writeValue("mCooldown", mCooldown);
	}

	@Override
	public void read(Json json, JsonValue jsonValue) {
		mCooldown = json.readValue("mCooldown", float.class, jsonValue);
	}

	/** Weapon definition */
	private WeaponDef mDef = null;
	/** Current cooldown timer */
	private float mCooldown = 0;
	/** Position of the weapon */
	private Vector2 mPosition = Pools.vector2.obtain();
}
