package com.spiddekauga.voider.menu;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.PlayerStats;
import com.spiddekauga.voider.repo.InternalNames;
import com.spiddekauga.voider.repo.ResourceCacheFacade;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * Score and game over scene. Displayed whether the player completed or failed a level. If
 * the level belongs to a campaign the player will continue to the next level (and the
 * player started the campaign and is not playing only this level). If the player failed
 * the game or is only playing this level add buttons for try again and main menu.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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

		((GameOverSceneGui) mGui).setGameOverScene(this);
	}

	/**
	 * Sets the game as completed
	 * @param completed true if completed the level successfully, false if player died
	 */
	public void setLevelCompleted(boolean completed) {
		mLevelCompleted = completed;
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message) {
		super.onActivate(outcome, message);

		// TODO upload stats
		// StatSyncer.uploadStats(mPlayerStats);
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
