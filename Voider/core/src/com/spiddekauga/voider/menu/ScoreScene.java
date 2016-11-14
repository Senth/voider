package com.spiddekauga.voider.menu;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.explore.ExploreLevelScene;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.PlayerStats;
import com.spiddekauga.voider.network.stat.HighscoreSyncEntity;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.stat.HighscoreRepo;
import com.spiddekauga.voider.repo.stat.StatLocalRepo;
import com.spiddekauga.voider.repo.stat.UserLevelStat;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.spiddekauga.voider.utils.Synchronizer;
import com.spiddekauga.voider.utils.Synchronizer.SyncTypes;

/**
 * Displays the player score and adds the ability for the player to rate, tag, and bookmark the
 * level if player is online.
 */
public class ScoreScene extends Scene {
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

	getGui().setScoreScene(this);
	setClearColor(UiFactory.getInstance().getStyles().color.sceneBackground);
}

@Override
public boolean onKeyDown(int keycode) {
	if (KeyHelper.isBackPressed(keycode)) {
		SceneSwitcher.returnTo(MainMenu.class);
		return true;
	}

	return super.onKeyDown(keycode);
}

@Override
protected void loadResources() {
	super.loadResources();
	ResourceCacheFacade.load(this, InternalDeps.UI_GENERAL);
	ResourceCacheFacade.load(this, mLevelDef.getId(), false);
}

@Override
protected ScoreSceneGui getGui() {
	return (ScoreSceneGui) super.getGui();
}

/**
 * Go back to main menu
 */
void gotoMainMenu() {
	if (isPublished()) {
		mSynchronizer.synchronize(SyncTypes.STATS);
	}

	SceneSwitcher.returnTo(ExploreLevelScene.class);
}

/**
 * @return true if the level is published
 */
boolean isPublished() {
	return ResourceLocalRepo.isPublished(mLevelDef.getId());
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
 * @return player rating of the level
 */
int getRating() {
	return mStat.rating;
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
 * @return player comment for the level
 */
String getComment() {
	return mStat.comment;
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
 * @return true if the level was completed
 */
boolean isLevelCompleted() {
	return mLevelCompleted;
}

/**
 * Sets the game as completed
 * @param completed true if completed the level successfully, false if player died
 */
public void setLevelCompleted(boolean completed) {
	mLevelCompleted = completed;
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
}
