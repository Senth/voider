package com.spiddekauga.voider.game.actors;

/**
 * All filter categories used for deciding who shall collide etc.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ActorFilterCategories {
	/** Category for all enemies */
	static public short ENEMY = 1 << 1;
	/** Category for the player */
	static public short PLAYER = 1 << 2;
	/** Category for the static terrain */
	static public short STATIC_TERRAIN = 1 << 3;
	/** Category for pickups, gold, etc */
	static public short PICKUP = 1 << 4;
	/** Border around the screen */
	static public short SCREEN_BORDER = 1 << 5;
	/** No collision at all */
	static public short NONE = 1 << 6;
}
