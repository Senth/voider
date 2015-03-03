package com.spiddekauga.voider.game;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceChangeListener;
import com.spiddekauga.voider.resources.Resource;


/**
 * Container for all player statistics when playing a level and calculates the score.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class PlayerStats extends Resource implements IResourceChangeListener {
	/**
	 * Create a new PlayerStats and sets the starting position of the level
	 * @param startCoordinate starting coordinate of the level, this is used for
	 *        calculating the multiplier.
	 * @param levelSpeed speed of the level, used for calculating the multiplier
	 * @param playerActor binds these stats to listen to the player
	 */
	public PlayerStats(float startCoordinate, float levelSpeed, PlayerActor playerActor) {
		mHitCoordinateLast = startCoordinate;
		mMultiplierChangeCoordinate = startCoordinate;
		mLevelSpeed = levelSpeed;
		mUniqueId = UUID.randomUUID();

		playerActor.addChangeListener(this);
	}

	/**
	 * Default constructor for Kryo
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
		float diffCoord = coordinate - mHitCoordinateLast;
		float nextMultiplierCoord = mMultiplierChangeCoordinate + mLevelSpeed;

		// Calculate the coordinates we have left on the current multiplier
		boolean increaseMultiplier = false;
		if (coordinate <= nextMultiplierCoord) {
			mScore += getScoreMultiplied(diffCoord);
		} else {
			diffCoord = coordinate - nextMultiplierCoord;
			mScore += getScoreMultiplied(nextMultiplierCoord - mHitCoordinateLast);
			increaseMultiplier = true;
		}


		// Increase multiplier and add rest of the score
		if (increaseMultiplier) {
			mMultiplierChangeCoordinate = nextMultiplierCoord;
			mMultiplier++;

			// Add any full score
			while (diffCoord >= mLevelSpeed) {
				diffCoord = diffCoord - mLevelSpeed;
				mMultiplier++;
				mScore += getScoreMultiplied(mLevelSpeed);
				mMultiplierChangeCoordinate += mLevelSpeed;
			}

			// Add rest score
			mScore += getScoreMultiplied(diffCoord);
		}


		mHitCoordinateLast = coordinate;
	}

	/**
	 * Multiplies the coordinates with the current multiplier to get the correct score
	 * @param coordinate number of coordinates to calculate the score on
	 * @return score for these coordinates
	 * @note Does not wrap if the coordinates would increase the multiplier, this is
	 *       instead done by #update(float).
	 */
	private float getScoreMultiplied(float coordinate) {
		return coordinate / mLevelSpeed * mMultiplier * Config.Game.SCORE_MULTIPLIER;
	}

	/**
	 * @return current score as String
	 */
	public String getScoreString() {
		return formatScore((int) (mScore + 0.5f));
	}

	/**
	 * Formats a score to a correct string
	 * @param score the score to format to a string
	 * @return formatted score string
	 */
	public static String formatScore(int score) {
		return NUMBER_FORMAT.format(score);
	}

	/**
	 * @return score of the player
	 */
	public int getScore() {
		return (int) (mScore + 0.5);
	}

	/**
	 * @return current multiplier value
	 */
	public String getMultiplierString() {
		return Integer.toString(mMultiplier);
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

	/**
	 * @return starting number of lives
	 */
	public static int getStartLives() {
		return STARTING_LIVES;
	}

	/**
	 * Sets whether or not the score is a new highscore
	 * @param newHighscore true if the score is a new highscore
	 */
	public void setIsNewHighscore(boolean newHighscore) {
		mNewHighscore = newHighscore;
	}

	/**
	 * @return true if the score is a new highscore
	 */
	public boolean isNewHighscore() {
		return mNewHighscore;
	}

	@Override
	public void onResourceChanged(IResource resource, EventTypes type) {
		if (type == EventTypes.LIFE_DECREASED) {
			mMultiplier = 1;
		}
	}

	/** Number of starting lives */
	private static final int STARTING_LIVES = 3;
	/** Number of lives left */
	@Tag(20) private int mExtraLives = STARTING_LIVES - 1;
	/** Speed of the level, used for calculating multiplier values */
	@Tag(21) private float mLevelSpeed;
	/** Current multiplier */
	@Tag(22) private int mMultiplier = 1;
	/** Last change of multiplier coordinate */
	@Tag(23) private float mMultiplierChangeCoordinate;
	/** Last hit coordinate */
	@Tag(24) private float mHitCoordinateLast;
	/** Score of the level */
	@Tag(25) private double mScore = 0;
	/** New highscore for the player */
	private boolean mNewHighscore = false;
	/** Number format */
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());
}
