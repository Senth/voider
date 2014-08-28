package com.spiddekauga.voider.menu;

import java.util.ArrayList;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.PlayerStats;
import com.spiddekauga.voider.network.entities.HighscoreSyncEntity;
import com.spiddekauga.voider.network.entities.Tags;
import com.spiddekauga.voider.repo.HighscoreRepo;
import com.spiddekauga.voider.repo.InternalNames;
import com.spiddekauga.voider.repo.ResourceCacheFacade;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * Displays the player score and adds the ability for the player to rate, tag, and
 * bookmark the level if s/he is online.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ScoreScene extends Scene {
	/**
	 * Creates a new game over scene with statistics and a heading
	 * @param playerStats all the player stats from the level
	 * @param levelDef the level that was completed
	 */
	public ScoreScene(PlayerStats playerStats, LevelDef levelDef) {
		super(new ScoreSceneGui());

		mPlayerStats = playerStats;
		mLevelDef = levelDef;

		((ScoreSceneGui) mGui).setScoreScene(this);
	}

	/**
	 * Sets the game as completed
	 * @param completed true if completed the level successfully, false if player died
	 */
	public void setLevelCompleted(boolean completed) {
		mLevelCompleted = completed;
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(InternalNames.UI_GENERAL);
		ResourceCacheFacade.load(this, mLevelDef.getId(), false);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
	}

	@Override
	public boolean onKeyDown(int keycode) {
		if (KeyHelper.isBackPressed(keycode)) {
			gotoMainMenu();
			return true;
		}

		return false;
	}

	/**
	 * Go back to main menu
	 */
	void gotoMainMenu() {
		SceneSwitcher.returnTo(MainMenu.class);
	}

	/**
	 * Try playing the level once again
	 */
	void tryAgain() {
		GameScene gameScene = new GameScene(false, false);
		gameScene.setLevelToLoad(mLevelDef);
		setNextScene(gameScene);
		setOutcome(Outcomes.NOT_APPLICAPLE);
	}

	/**
	 * Set the rating for the level
	 * @param rating new rating for the level
	 */
	void setRating(int rating) {
		// TODO set rating
	}

	/**
	 * @return player rating of the level
	 */
	int getRating() {
		// TODO
		return 0;
	}

	/**
	 * Set tags for the level
	 * @param tags list of selected tags
	 */
	void setTags(ArrayList<Tags> tags) {
		// TODO set tags
	}

	/**
	 * Bookmark the level
	 * @param bookmark true if the level should be bookmarked, false removes the bookmark
	 */
	void setBookmark(boolean bookmark) {
		// TODO set bookmark
	}

	/**
	 * @return true if the player has bookmarked the level
	 */
	boolean isBookmarked() {
		// TODO
		return true;
	}

	/**
	 * Set comment for level
	 * @param comment new comment for this level
	 */
	void setComment(String comment) {
		// TODO set comment
	}

	/**
	 * @return player comment for the level
	 */
	String getComment() {
		// TODO
		return "";
	}

	/**
	 * @return true if the level was completed
	 */
	boolean isLevelCompleted() {
		return mLevelCompleted;
	}

	/**
	 * @return player score string
	 */
	String getPlayerScore() {
		return mPlayerStats.getScoreString();
	}

	/**
	 * @return player highscore string
	 */
	String getPlayerHighscore() {
		HighscoreSyncEntity highscoreEntity = mHighscoreRepo.getPlayerHighscore(mLevelDef.getId());

		if (highscoreEntity != null) {
			return PlayerStats.formatScore(highscoreEntity.score);
		} else {
			return "0";
		}
	}

	/** Highscore repository */
	private HighscoreRepo mHighscoreRepo = HighscoreRepo.getInstance();
	/** True if the level was completed, false if player died */
	private boolean mLevelCompleted = false;
	/** Player statistics from the level */
	private PlayerStats mPlayerStats;
	/** Level that was completed */
	private LevelDef mLevelDef;
}
