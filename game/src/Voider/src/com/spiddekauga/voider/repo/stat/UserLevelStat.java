package com.spiddekauga.voider.repo.stat;

import java.util.ArrayList;
import java.util.Date;

import com.spiddekauga.voider.network.entities.stat.Tags;

/**
 * Local user level statistics
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class UserLevelStat {
	/** Total plays */
	public int cPlayed = 0;
	/** Total number of clears */
	public int cCleared = 0;
	/** Bookmarked */
	public boolean bookmarked = false;
	/** Rating */
	public int rating = 0;
	/** Last played date */
	public Date lastPlayed = null;
	/** Tags for the level */
	public ArrayList<Tags> tags = null;
}
