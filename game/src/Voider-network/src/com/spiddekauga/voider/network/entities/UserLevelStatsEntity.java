package com.spiddekauga.voider.network.entities;

import java.util.Date;

/**
 * Level statistics a single player
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class UserLevelStatsEntity implements IEntity {
	/** If the player has bookmarked the level */
	public boolean bookmarked = false;
	/** Last played */
	public Date lastPlayed = null;
	/** Rating */
	public int rating = -1;
	/** Play count */
	public int cPlayed = 0;
	/** Clear count */
	public int cCleared = 0;
}
