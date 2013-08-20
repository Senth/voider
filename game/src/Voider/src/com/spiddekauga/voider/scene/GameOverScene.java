package com.spiddekauga.voider.scene;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.app.MainMenu;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.PlayerStats;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.utils.StatSyncer;

/**
 * Score and game over scene. Displayed whether the player completed or failed a level.
 * If the level belongs to a campaign the player will continue to the next level (and
 * the player started the campaign and is not playing only this level). If the player
 * failed the game or is only playing this level add buttons for try again and main menu.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class GameOverScene extends Scene {
	/**
	 * Creates a new game over scene with statistics and a heading
	 * @param playerStats all the player stats from the level
	 * @param levelDef the level that was completed
	 */
	public GameOverScene(PlayerStats playerStats, LevelDef levelDef) {
		super(new GameOverSceneGui());

		mPlayerStats = playerStats;
		mLevelDef = levelDef;

		((GameOverSceneGui)mGui).setGameOverScene(this);
	}

	/**
	 * Sets the game as completed
	 * @param completed true if completed the level successfully, false if player died
	 */
	public void setLevelCompleted(boolean completed) {
		mLevelCompleted = completed;
	}

	@Override
	protected void onActivate(Outcomes outcome, String message) {
		super.onActivate(outcome, message);

		StatSyncer.uploadStats(mPlayerStats);
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.load(mLevelDef, false);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.unload(mLevelDef, false);
	}

	@Override
	public boolean keyDown(int keycode) {
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
	 * @todo Continue with the next level
	 */
	void continueWithNextLevel() {
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

	/** True if the level was completed, false if player died */
	private boolean mLevelCompleted = false;
	/** Player statistics from the level */
	private PlayerStats mPlayerStats;
	/** Level that was completed */
	private LevelDef mLevelDef;
}
