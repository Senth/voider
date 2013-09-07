package com.spiddekauga.voider.app;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.GameSaveDef;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.SelectDefScene;
import com.spiddekauga.voider.utils.Pools;

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
		ResourceCacheFacade.loadAllOf(this, LevelDef.class, false);
		ResourceCacheFacade.loadAllOf(this, GameSaveDef.class, false);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.unloadAllOf(this, LevelDef.class, false);
		ResourceCacheFacade.unloadAllOf(this, GameSaveDef.class, false);
	}

	@Override
	protected boolean unloadResourcesOnDeactivate() {
		return true;
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message) {
		if (outcome == Outcomes.LOADING_FAILED_CORRUPT_FILE) {
			/** @todo handle corrupt file */
		} else if (outcome == Outcomes.LOADING_FAILED_MISSING_FILE) {
			/** @todo handle missing file */
		} else if (outcome == Outcomes.DEF_SELECTED) {
			if (message instanceof ResourceItem) {
				LevelDef loadedLevelDef = ResourceCacheFacade.get(this, ((ResourceItem) message).id, ((ResourceItem) message).revision);
				GameScene gameScene = new GameScene(false, false);
				gameScene.setLevelToLoad(loadedLevelDef);
				SceneSwitcher.switchTo(gameScene);
				Pools.resourceItem.free((ResourceItem)message);
			} else {
				Gdx.app.error("MainMenu", "When seleting def, message was not a ResourceItem but a " + message.getClass().getName());
			}
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
		return ResourceCacheFacade.getAll(this, GameSaveDef.class).size() > 0;
	}

	/**
	 * Resumes the current game
	 */
	void resumeGame() {
		List<GameSaveDef> gameSaves = ResourceCacheFacade.getAll(this, GameSaveDef.class);

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
