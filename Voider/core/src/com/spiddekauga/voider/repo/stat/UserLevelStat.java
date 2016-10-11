package com.spiddekauga.voider.repo.stat;

import java.util.Date;

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
	/** User comment */
	public String comment = null;
}
