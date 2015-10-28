package com.spiddekauga.voider.network.resource;

import com.spiddekauga.voider.game.actors.AimTypes;
import com.spiddekauga.voider.game.actors.MovementTypes;

/**
 * Enemy definition entity
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class EnemyDefEntity extends DefEntity {
	/**
	 * Sets default variables
	 */
	public EnemyDefEntity() {
		type = UploadTypes.ENEMY_DEF;
	}

	/** If the enemy uses a weapon */
	public boolean hasWeapon = false;
	/** Movement type */
	public MovementTypes movementType = MovementTypes.PATH;
	/** Movement speed d */
	public float movementSpeed = 0;
	/** Bullet speed */
	public float bulletSpeed = 0;
	/** Enemy aim type */
	public AimTypes aimType = null;
	/** Bullet damage */
	public float bulletDamage = 0;
	/** If the enemy will be destroyed when it collides with the player */
	public boolean destroyOnCollide = false;
	/** Amount of damage the enemy does on the player when colliding */
	public float collisionDamage = 0;
}
