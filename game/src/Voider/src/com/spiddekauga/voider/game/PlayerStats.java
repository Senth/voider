package com.spiddekauga.voider.game;

import java.math.BigDecimal;

/**
 * Container for all player statistics when playing a level and calculates
 * the score.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PlayerStats {
	/**
	 * Create a new PlayerStats and sets the starting position of the level
	 * @param startCoordinate starting coordinate of the level, this is used
	 * for calculating the multiplier.
	 * @param levelSpeed speed of the level, used for calculating the multiplier
	 */
	public PlayerStats(float startCoordinate, float levelSpeed) {
		mHitCoordinateLast = startCoordinate;
		mCoordinateLast = startCoordinate;
		mLevelSpeed = levelSpeed;
	}

	/**
	 * Default constructor for Json
	 */
	@SuppressWarnings("unused")
	private PlayerStats() {
		// Does nothing
	}

	/**
	 * Updates the score
	 * @param coordinate current coordinate for the level
	 */
	public void updateScore(float coordinate) {
		/** @todo update score */
	}

	/**
	 * @return current score as String
	 */
	public String getScoreString() {
		return mScore.toBigInteger().toString();
	}

	/**
	 * @return a formatted version of #getScoreString() that includes leading
	 * zeros
	 */
	public String getScoreStringLeadingZero() {
		String score = getScoreString();
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 10 - score.length(); ++i) {
			stringBuilder.append('0');
		}
		stringBuilder.append(score);
		return stringBuilder.toString();
	}

	/**
	 * @return current multiplier value
	 */
	public String getMultiplierString() {
		float multiplier = mCoordinateLast - mHitCoordinateLast;
		multiplier /= mLevelSpeed;
		return Integer.toString((int)(multiplier + 0.5f));
	}

	/**
	 * @return number of extra lives
	 */
	public int getExtraLives() {
		return mExtraLives;
	}

	/**
	 * Decreases the number of extra lives
	 */
	public void decreaseExtraLives() {
		--mExtraLives;
	}

	/** Number of lives left */
	private int mExtraLives = 2;
	/** Speed of the level, used for calculating multiplier values */
	private float mLevelSpeed = 1;
	/** Current multiplier */
	private float mCoordinateLast = 0;
	/** Last hit coordinate */
	private float mHitCoordinateLast = 0;
	/** Score of the level */
	private BigDecimal mScore = new BigDecimal(0);
}
