package com.spiddekauga.voider.network.stat;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IEntity;

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
	/** Tags for the level */
	public ArrayList<Tags> tags = new ArrayList<>();

	/**
	 * @return integer rating of the average rating
	 */
	public int getIntRating() {
		return (int) (ratingAverage + 0.5f);
	}
}
