package com.spiddekauga.voider.network.entities;

/**
 * Level statistics
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelStatsEntity implements IEntity {
	/** Total plays */
	public int cPlayed = 0;
	/** Total bookmarks */
	public int cBookmarks = 0;
	/** clear count */
	public int cCleared = 0;
	/** number of ratings */
	public int cRatings = 0;
	/** sum of all ratings */
	public int ratingSum = 0;
	/** Average rating */
	public float ratingAverage = 0;
}
