package com.spiddekauga.voider.app;

import java.util.List;
import java.util.UUID;

import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.GameSaveDef;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.SelectDefScene;

/**
 * Main menu of the scene
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class MainMenu extends Scene {
	/**
	 * Default constructor for main menu
	 */
	public MainMenu() {
		super(new MainMenuGui());
		((MainMenuGui)mGui).setMainMenu(this);
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.loadAllOf(LevelDef.class, false);
		ResourceCacheFacade.loadAllOf(GameSaveDef.class, false);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.unloadAllOf(LevelDef.class, false);
		ResourceCacheFacade.unloadAllOf(GameSaveDef.class, false);
	}

	@Override
	protected boolean unloadResourcesOnDeactivate() {
		return true;
	}

	@Override
	protected void onActivate(Outcomes outcome, String message) {
		if (outcome == Outcomes.LOADING_FAILED_CORRUPT_FILE) {
			/** @todo handle corrupt file */
		} else if (outcome == Outcomes.LOADING_FAILED_MISSING_FILE) {
			/** @todo handle missing file */
		} else if (outcome == Outcomes.DEF_SELECTED) {
			LevelDef loadedLevelDef = ResourceCacheFacade.get(UUID.fromString(message), LevelDef.class);
			GameScene gameScene = new GameScene(false, false);
			gameScene.setLevelToLoad(loadedLevelDef);
			SceneSwitcher.switchTo(gameScene);
		} else {
			if (!mGui.isInitialized()) {
				mGui.initGui();
			}
		}

		mGui.resetValues();
	}

	/**
	 * @return true if there is a game to resume
	 */
	boolean hasResumeGame() {
		return ResourceCacheFacade.getAll(GameSaveDef.class).size() > 0;
	}

	/**
	 * Resumes the current game
	 */
	void resumeGame() {
		List<GameSaveDef> gameSaves = ResourceCacheFacade.getAll(GameSaveDef.class);

		if (!gameSaves.isEmpty()) {
			GameScene gameScene = new GameScene(false, false);
			gameScene.setGameToResume(gameSaves.get(0));
			SceneSwitcher.switchTo(gameScene);
		}
	}

	/**
	 * Goes to the campaign menus
	 */
	void gotoCampaignMenu() {
		/** @todo go to the campaign menus */
	}

	/**
	 * Goes to the downloaded content menu
	 */
	void gotoDownloadedContentMenu() {
		/** @todo change the simple load level to a more advanced level */
		SelectDefScene selectLevelScene = new SelectDefScene(LevelDef.class, false, true);
		SceneSwitcher.switchTo(selectLevelScene);
	}

	/**
	 * Goes to the explore menu
	 */
	void gotoExploreMenu() {
		/** @todo goes to the explore menu */
	}

	/**
	 * Goes to the editor
	 */
	void gotoEditor() {
		LevelEditor levelEditor = new LevelEditor();

		LevelDef levelDef = new LevelDef();
		Level level = new Level(levelDef);
		levelEditor.setLevel(level);
		SceneSwitcher.switchTo(levelEditor);
	}

	/**
	 * Goes to the options menu
	 */
	void gotoOptions() {
		/** @todo goes to the options menu */
	}
}
