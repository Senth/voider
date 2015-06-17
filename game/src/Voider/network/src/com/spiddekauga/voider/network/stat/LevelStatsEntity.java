package com.spiddekauga.voider.network.stat;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.resource.LevelDifficultySearchRanges;

/**
 * Level statistics
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LevelStatsEntity implements IEntity {
	private static final long serialVersionUID = 1L;
	/** Total plays */
	public int cPlayed = 0;
	/** Total bookmarks */
	public int cBookmarks = 0;
	/** clear count */
	public int cCleared = 0;
	/** number of ratings */
	public int cRatings = 0;
	/** number of deaths */
	public int cDeaths = 0;
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

	/**
	 * @return get frustration level
	 */
	public int getFrustrationLevel() {
		if (cPlayed > 0) {
			float frustration = (cCleared + cDeaths) / cPlayed;
			return (int) (frustration * 100);
		} else {
			return 0;
		}
	}

	/**
	 * @return get level difficulty
	 */
	public String getDifficulty() {
		float total = cDeaths + cCleared;
		float cleared = cCleared / total;
		LevelDifficultySearchRanges difficulty = LevelDifficultySearchRanges.getRange(cleared);
		if (difficulty != null) {
			return difficulty.toString();
		} else {
			return "UNKNOWN";
		}
	}
}
