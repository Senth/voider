package com.spiddekauga.voider.network.stat;

import com.spiddekauga.voider.network.entities.IEntity;

import java.util.Date;

/**
 * Level statistics a single player
 */
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
