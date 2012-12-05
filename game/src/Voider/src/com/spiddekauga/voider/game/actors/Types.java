package com.spiddekauga.voider.game.actors;

/**
 * All Actor types
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public enum Types {
	/**
	 * Player, this is the ship that the player controls
	 */
	PLAYER,
	/**
	 * All enemies
	 */
	ENEMY,
	/**
	 * All boss actors
	 */
	BOSS,
	/**
	 * All weapons fire bullet actors. Weapons are by themselves not an actor
	 */
	BULLET,
	/**
	 * Pickups. These are both weapon and ability pickups
	 */
	PICKUP,
	/**
	 * Terrain that cannot move
	 */
	STATIC_TERRAIN,
	/**
	 * Invalid type of actor type
	 */
	INVALID
}
