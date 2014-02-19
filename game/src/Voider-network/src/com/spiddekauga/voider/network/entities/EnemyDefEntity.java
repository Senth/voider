package com.spiddekauga.voider.network.entities;

import com.spiddekauga.voider.game.actors.MovementTypes;

/**
 * Enemy definition entity
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class EnemyDefEntity extends DefEntity {
	/** If the enemy uses a weapon */
	public boolean enemyHasWeapon = false;
	/** Movement type */
	public MovementTypes enemyMovementType = null;
}
