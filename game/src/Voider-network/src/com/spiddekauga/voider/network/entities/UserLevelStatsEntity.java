package com.spiddekauga.voider.network.entities;

import java.util.Date;

/**
 * Level statistics a single player
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class UserLevelStatsEntity implements IEntity {
	/** If the player has liked the level */
	public boolean like = false;
	/** Last played */
	public Date lastPlayed = null;
	/** Rating */
	public int rating = -1;
	/** Play count */
	public int cPlayed = 0;
	/** Clear count */
	public int cCleared = 0;
}
