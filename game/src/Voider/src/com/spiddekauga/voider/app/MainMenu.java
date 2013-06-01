package com.spiddekauga.voider.app;

import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;

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
	protected void update() {

	}

	@Override
	public boolean hasResources() {
		return true;
	}

	@Override
	public void loadResources() {
		ResourceCacheFacade.load(ResourceNames.UI_GENERAL);
	}

	@Override
	public void unloadResources() {
		ResourceCacheFacade.unload(ResourceNames.UI_GENERAL);
	}

	@Override
	public void onActivate(Outcomes outcome, String message) {
		if (outcome == Outcomes.LOADING_FAILED_CORRUPT_FILE) {
			/** @todo handle corrupt file */
		} else if (outcome == Outcomes.LOADING_FAILED_MISSING_FILE) {
			/** @todo handle missing file */
		} else {
			if (!mGui.isInitialized()) {
				mGui.initGui();
				mGui.resetValues();
			}
		}
	}

	/**
	 * @return true if there is a game to resume
	 */
	boolean hasResumeGame() {
		/** @todo check if a game exists to resume */
		return false;
	}

	/**
	 * Resumes the current game
	 */
	void resumeGame() {
		/** @todo resume current game */
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
		/** @todo goes to the downloaded content menus */
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
