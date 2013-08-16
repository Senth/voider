package com.spiddekauga.voider.game;

import java.util.UUID;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceChangeListener;
import com.spiddekauga.voider.resources.Resource;


/**
 * Container for all player statistics when playing a level and calculates
 * the score.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PlayerStats extends Resource implements IResourceChangeListener {
	/**
	 * Create a new PlayerStats and sets the starting position of the level
	 * @param startCoordinate starting coordinate of the level, this is used
	 * for calculating the multiplier.
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
	 * instead done by #update(float).
	 */
	private float getScoreMultiplied(float coordinate) {
		return coordinate / mLevelSpeed * mMultiplier * Config.Game.SCORE_MULTIPLIER;
	}

	/**
	 * @return current score as String
	 */
	public String getScoreString() {
		return Long.toString((long)(mScore + 0.5));
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

	@Override
	public void onResourceChanged(IResource resource, EventTypes type) {
		if (type == EventTypes.LIFE_DECREASED) {
			mMultiplier = 1;
		}
	}

	@Override
	public void write(Json json) {
		super.write(json);

		json.writeValue("mExtraLives", mExtraLives);
		json.writeValue("mLevelSpeed", mLevelSpeed);
		json.writeValue("mMultiplier", mMultiplier);
		json.writeValue("mMultiplierChangeCoordinate", mMultiplierChangeCoordinate);
		json.writeValue("mHitCoordinateLast", mHitCoordinateLast);
		json.writeValue("mScore", mScore);
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		super.read(json, jsonData);

		mExtraLives = json.readValue("mExtraLives", int.class, jsonData);
		mLevelSpeed = json.readValue("mLevelSpeed", float.class, jsonData);
		mMultiplier = json.readValue("mMultiplier", int.class, jsonData);
		mMultiplierChangeCoordinate = json.readValue("mMultiplierChangeCoordinate", float.class, jsonData);
		mHitCoordinateLast = json.readValue("mHitCoordinateLast", float.class, jsonData);
		mScore = json.readValue("mScore", double.class, jsonData);
	}

	/** Number of lives left */
	private int mExtraLives = 2;
	/** Speed of the level, used for calculating multiplier values */
	private float mLevelSpeed;
	/** Current multiplier */
	private int mMultiplier = 1;
	/** Last change of multiplier coordinate */
	private float mMultiplierChangeCoordinate;
	/** Last hit coordinate */
	private float mHitCoordinateLast;
	/** Score of the level */
	private double mScore = 0;
}
