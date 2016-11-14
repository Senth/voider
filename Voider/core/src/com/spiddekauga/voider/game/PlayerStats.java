package com.spiddekauga.voider.game;

import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Stack;
import java.util.UUID;


/**
 * Container for all player statistics when playing a level. This class calculates the score.
 */
public class PlayerStats extends Resource implements Disposable {
private static final int STARTING_LIVES = 3;
private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());
private static EventDispatcher mEventDipatcher = EventDispatcher.getInstance();

@Tag(20)
private int mExtraLives = STARTING_LIVES - 1;
@Tag(25)
private double mMultiplierPerCoord = 0;
@Tag(23)
private float mCoordEnd;
@Tag(24)
private float mCoordStart;
@Tag(21)
private float mCoordCurrent = 1;
@Tag(151)
private Stack<ScorePart> mScoreParts = new Stack<>();
@Tag(152)
private float mCollisionTime = 0;
private IEventListener mCollisionListener = new IEventListener() {
	@Override
	public void handleEvent(GameEvent event) {
		// Only if life was decreased
		HealthChangeEvent healthChangeEvent = (HealthChangeEvent) event;
		if (healthChangeEvent.actor.getHealth() < healthChangeEvent.oldHealth) {

			// Check cooldown -> Decrease multiplier
			float currentTime = GameTime.getTotalGlobalTimeElapsed();
			float cooldownTime = ConfigIni.getInstance().game.getMultiplierCollisionCooldown();
			if (currentTime >= mCollisionTime + cooldownTime) {
				mCollisionTime = currentTime;
				createScorePart();
			}
		}
	}
};

@Tag(146)
private double mScore = 0;
@Tag(153)
private double mMultiplier = 1;
@Tag(155)
private double mMultiplierNormalized = 0;


private boolean mNewHighscore = false;

@Deprecated
@Tag(22)
private int mNotUsed2 = 1;

/**
 * Create a new PlayerStats and sets the starting position of the level
 * @param startCoordinate starting coordinate of the level, this is used for calculating the
 * multiplier.
 * @param endCoordinate end coordinate, this is used for calculating the score
 * @param levelLength in time. Used to calculate the shown multiplier
 */
public PlayerStats(float startCoordinate, float endCoordinate, float levelLength) {
	this();
	mCoordStart = startCoordinate;
	mCoordCurrent = startCoordinate;
	mCoordEnd = endCoordinate;
	mUniqueId = UUID.randomUUID();


	// Calculate multiplies per coordinate
	double coordDiff = mCoordEnd - mCoordStart;
	mMultiplierPerCoord = Config.Game.MULTIPLIER_MAX / coordDiff;

	// Normalize multiplier
	mMultiplierNormalized = levelLength / Config.Game.MULTIPLIER_MAX;
}

/**
 * Default constructor for Kryo
 */
private PlayerStats() {
	mEventDipatcher.connect(EventTypes.GAME_ACTOR_HEALTH_CHANGED, mCollisionListener);
}

/**
 * @return starting number of lives
 */
public static int getStartLives() {
	return STARTING_LIVES;
}

/**
 * Get a new multiplier value after a ship was hit
 * @param multiplierBefore multiplier before the ship was hit
 * @return multiplier after the ship was hit
 */
private static double calculateNewMultiplier(double multiplierBefore) {
	return multiplierBefore * ConfigIni.getInstance().game.getMultiplierDecrement();
}

@Override
public void dispose() {
	mEventDipatcher.disconnect(EventTypes.GAME_ACTOR_HEALTH_CHANGED, mCollisionListener);
}

/**
 * Updates the score
 * @param coordinate current coordinate for the level
 */
public void updateScore(float coordinate) {
	mCoordCurrent = coordinate;

	if (mCoordCurrent > mCoordEnd) {
		mCoordCurrent = mCoordEnd;
	}

	updateMultiplier();
	updateTotalScore();
}

/**
 * Update the current multiplier
 */
private void updateMultiplier() {
	double diffCoord = mCoordCurrent - getCoordLastHit();
	mMultiplier = getMultiplierLastHit() + diffCoord * mMultiplierPerCoord;
}

/**
 * Calculates the total score
 */
private void updateTotalScore() {
	ScorePart endScorePart = new ScorePart(this);
	mScore = endScorePart.getScore();

	for (ScorePart scorePart : mScoreParts) {
		mScore += scorePart.getScore();
	}

	// Clamp to max score
	if (mScore > Config.Game.SCORE_MAX) {
		mScore = Config.Game.SCORE_MAX;
	}
}

/**
 * @return last coordinate when the player ship was hit
 */
private float getCoordLastHit() {
	if (mScoreParts.isEmpty()) {
		return mCoordStart;
	} else {
		return mScoreParts.peek().mCoordEnd;
	}
}

/**
 * @return last multiplier when the player ship was hit
 */
private double getMultiplierLastHit() {
	if (mScoreParts.isEmpty()) {
		return 1;
	} else {
		return mScoreParts.peek().mMultiplierAfter;
	}
}

/**
 * Calculate end score. Call this once the player finished playing the level
 */
public void calculateEndScore() {
	if (mScoreParts.isEmpty()) {
		mScore = Config.Game.SCORE_MAX;
	} else {
		mCoordCurrent = mCoordEnd;
		updateMultiplier();
		updateTotalScore();
	}
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
 * @return current multiplier value to be displayer
 */
public String getMultiplierString() {
	double multiplierShow = mMultiplier * mMultiplierNormalized;
	return Integer.toString((int) (multiplierShow + 0.5));
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

/**
 * Create and add a new score part
 */
private void createScorePart() {
	ScorePart scorePart = new ScorePart(this);
	mScoreParts.push(scorePart);
}

/**
 * Container class for part of the score
 */
public static class ScorePart {
	@Tag(147)
	private float mCoordEnd;
	@Tag(148)
	private double mMultiplierStart;
	@Tag(149)
	private double mMultiplierEnd;
	@Tag(154)
	private double mMultiplierAfter;
	@Tag(150)
	private double mScore;

	/**
	 * Creates a new score part
	 * @param playerStats player stats
	 */
	private ScorePart(PlayerStats playerStats) {
		// Coord start
		if (playerStats.mScoreParts.isEmpty()) {
			mMultiplierStart = 1;
		} else {
			ScorePart prevScorePart = playerStats.mScoreParts.peek();
			mMultiplierStart = prevScorePart.mMultiplierAfter;
		}
		mCoordEnd = playerStats.mCoordCurrent;
		mMultiplierEnd = playerStats.mMultiplier;
		mMultiplierAfter = calculateNewMultiplier(playerStats.mMultiplier);

		calculateScore();
	}

	/**
	 * Calculates the score
	 */
	private void calculateScore() {
		// (Xe + Xs) * (Xe - Xs + 1) / 2
		mScore = (mMultiplierEnd + mMultiplierStart) * (mMultiplierEnd - mMultiplierStart + 1) * 0.5;
	}

	/**
	 * Default constructor for kryo
	 */
	private ScorePart() {
		// Does nothing
	}

	/**
	 * @return score of this part
	 */
	public double getScore() {
		return mScore;
	}
}
}
