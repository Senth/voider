package com.spiddekauga.voider.menu;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.scene.ui.UiFactory;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.PlayerStats;
import com.spiddekauga.voider.network.entities.stat.HighscoreSyncEntity;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.stat.HighscoreRepo;
import com.spiddekauga.voider.repo.stat.StatLocalRepo;
import com.spiddekauga.voider.repo.stat.UserLevelStat;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Synchronizer;
import com.spiddekauga.voider.utils.Synchronizer.SyncTypes;

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
		mStat = mStatRepo.getLevelStats(mLevelDef.getId());
		if (mStat == null) {
			mStat = new UserLevelStat();
		}

		((ScoreSceneGui) mGui).setScoreScene(this);
		setClearColor(UiFactory.getInstance().getStyles().color.sceneBackground);
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
			SceneSwitcher.returnTo(MainMenu.class);
			return true;
		}

		return false;
	}

	/**
	 * Go back to main menu
	 */
	void gotoMainMenu() {
		if (isPublished()) {
			mSynchronizer.synchronize(SyncTypes.STATS);
		}

		SceneSwitcher.returnTo(MainMenu.class);
	}

	/**
	 * Try playing the level once again
	 */
	void tryAgain() {
		if (isPublished()) {
			mSynchronizer.synchronize(SyncTypes.STATS);
		}

		GameScene gameScene = new GameScene(false, false);
		gameScene.setLevelToLoad(mLevelDef);
		setNextScene(gameScene);
		setOutcome(Outcomes.NOT_APPLICAPLE);
	}

	/**
	 * @return true if the level is published
	 */
	boolean isPublished() {
		return ResourceLocalRepo.isPublished(mLevelDef.getId());
	}

	/**
	 * Set the rating for the level
	 * @param rating new rating for the level
	 */
	void setRating(int rating) {
		mStat.rating = rating;
		mStatRepo.setRating(mLevelDef.getId(), rating);
	}

	/**
	 * @return player rating of the level
	 */
	int getRating() {
		return mStat.rating;
	}

	/**
	 * Bookmark the level
	 * @param bookmark true if the level should be bookmarked, false removes the bookmark
	 */
	void setBookmark(boolean bookmark) {
		mStat.bookmarked = bookmark;
		mStatRepo.setBookmark(mLevelDef.getId(), bookmark);
	}

	/**
	 * @return true if the player has bookmarked the level
	 */
	boolean isBookmarked() {
		return mStat.bookmarked;
	}

	/**
	 * Set comment for level
	 * @param comment new comment for this level
	 */
	void setComment(String comment) {
		mStat.comment = comment;
		mStatRepo.setComment(mLevelDef.getId(), comment);
	}

	/**
	 * @return player comment for the level
	 */
	String getComment() {
		return mStat.comment;
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

	/** Player stats */
	private UserLevelStat mStat = null;
	/** Synchronizer */
	private Synchronizer mSynchronizer = Synchronizer.getInstance();
	/** Local stat repository */
	private StatLocalRepo mStatRepo = StatLocalRepo.getInstance();
	/** Highscore repository */
	private HighscoreRepo mHighscoreRepo = HighscoreRepo.getInstance();
	/** True if the level was completed, false if player died */
	private boolean mLevelCompleted = false;
	/** Player statistics from the level */
	private PlayerStats mPlayerStats;
	/** Level that was completed */
	private LevelDef mLevelDef;
}
