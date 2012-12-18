package com.spiddekauga.voider.game.actors;

/**
 * All filter categories used for deciding who shall collide etc.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class FixtureFilterCategories {
	/** Category for all enemies */
	static public short ENEMY = 0x0001;
	/** Category for the player */
	static public short PLAYER = 0x0002;
	/** Category for the static terrain */
	static public short STATIC_TERRAIN = 0x0004;
	/** Category for pickups, gold, etc */
	static public short PICKUP = 0x0008;
}
