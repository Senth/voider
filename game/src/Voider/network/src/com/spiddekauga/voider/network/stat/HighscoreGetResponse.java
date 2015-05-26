package com.spiddekauga.voider.network.stat;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;


/**
 * Response from highscore get method
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class HighscoreGetResponse implements IEntity, ISuccessStatuses {
	private static final long serialVersionUID = 1L;
	/**
	 * First place of the level. Null if no one has first place. Available for
	 * <ul>
	 * <li>Fetch.FIRST_PLACE</li>
	 * <li>Fetch.USER_SCORE</li>
	 * </ul>
	 */
	public HighscoreEntity firstPlace = null;
	/** Player score. Available for Fetch.USER_SCORE */
	public HighscoreEntity userScore = null;
	/** User place. Available for Fetch.USER_SCORE */
	public int userPlace = 0;
	/** Scores before the user. Available for Fetch.USER_SCORE */
	public ArrayList<HighscoreEntity> beforeUser = null;
	/** Scores after the user. Available for Fetch.USER_SCORE */
	public ArrayList<HighscoreEntity> afterUser = null;
	/** Top scores. Availaable for Fetch.TOP_SCORES */
	public ArrayList<HighscoreEntity> topScores = null;
	/** Response status */
	public Statuses status = null;


	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	/** Response statuses */
	public enum Statuses implements ISuccessStatuses {
		/** SUCCESS */
		SUCCESS,
		/** Didn't find any highscores for the level */
		FAILED_HIGHSCORES_NOT_FOUND,
		/** Failed could not find level */
		FAILED_LEVEL_NOT_FOUND,
		/** Failed internal error */
		FAILED_INTERNAL,
		/** Failed to connect to server */
		FAILED_CONNECTION,
		/** Failed user is not logged in */
		FAILED_USER_NOT_LOGGED_IN,

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}
