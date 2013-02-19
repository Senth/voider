package com.spiddekauga.voider.game.actors;

import java.util.ArrayList;

/**
 * Groups together enemies. These enemies have the same properties: Same position,
 * same, definition, same trigger, same everything. Except they have all have
 * different trigger delays, so they don't start at the same time
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EnemyGroup {
	/**
	 * Adds an enemy to the enemy group. This automatically sets the enemy's
	 * group to this one. An enemy can only belong to one enemy group.
	 * If this is the first enemy of this group all other enemies will
	 * automatically use the same values as the first
	 */


	/**
	 * Sets the duplicate trigger delay of the selected actor.
	 * @param delay seconds delay between each actor.
	 */
	public void setDuplicateTriggerDelay(float delay) {
		mTriggerDelay = delay;
	}

	/**
	 * @return seconds of duplicate trigger delay between each actors. -1
	 * if no enemy is selected, or only one duplicate exist.
	 */
	public float getDuplicateTriggerDelay() {
		return mTriggerDelay;
	}


	/** All the enemies */
	private ArrayList<EnemyActor> mEnemies = new ArrayList<EnemyActor>();
	/** Trigger delay between enemies, in seconds */
	private float mTriggerDelay = 0;
}
