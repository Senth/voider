package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.app.PrototypeScene;
import com.spiddekauga.voider.app.TestUiScene;
import com.spiddekauga.voider.editor.EditorSelectionScene;
import com.spiddekauga.voider.game.GameSaveDef;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.LogoutMethodResponse;
import com.spiddekauga.voider.repo.ICallerResponseListener;
import com.spiddekauga.voider.repo.UserLocalRepo;
import com.spiddekauga.voider.repo.UserWebRepo;
import com.spiddekauga.voider.resources.ExternalTypes;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.SelectDefScene;
import com.spiddekauga.voider.utils.Pools;
import com.spiddekauga.voider.utils.User;

/**
 * Main menu of the scene
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MainMenu extends Scene implements ICallerResponseListener {
	/**
	 * Default constructor for main menu
	 */
	public MainMenu() {
		super(new MainMenuGui());
		((MainMenuGui)mGui).setMenuScene(this);
		mGuiStack.add(mGui);
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(InternalNames.UI_GENERAL);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.LEVEL_DEF, false);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.GAME_SAVE_DEF, false);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
	}

	@Override
	protected boolean unloadResourcesOnDeactivate() {
		return true;
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message) {
		super.onActivate(outcome, message);

		if (outcome == Outcomes.LOADING_FAILED_CORRUPT_FILE) {
			/** @todo handle corrupt file */
		} else if (outcome == Outcomes.LOADING_FAILED_MISSING_FILE) {
			/** @todo handle missing file */
		} else if (outcome == Outcomes.DEF_SELECTED) {
			if (message instanceof ResourceItem) {
				LevelDef loadedLevelDef = ResourceCacheFacade.get(((ResourceItem) message).id);
				GameScene gameScene = new GameScene(false, false);
				gameScene.setLevelToLoad(loadedLevelDef);
				SceneSwitcher.switchTo(gameScene);
				Pools.resourceItem.free((ResourceItem)message);
			} else {
				Gdx.app.error("MainMenu", "When seleting def, message was not a ResourceItem but a " + message.getClass().getName());
			}
		}

		//		mGui.dispose();
		//		mGui.initGui();
		mGui.resetValues();

		// Show if logged in online
		if (mFirstTimeActivation) {
			if (mUser.isOnline()) {
				mGui.showSuccessMessage(mUser.getUsername() + " is now online!");
			} else {
				mGui.showHighlightMessage(mUser.getUsername() + " is now offline!");
			}
			mFirstTimeActivation = false;
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		if (KeyHelper.isBackPressed(keycode)) {
			popMenu();
			return true;
		}

		// Testing
		else if (!Config.Debug.RELEASE_FINAL && keycode == Input.Keys.F5) {
			SceneSwitcher.switchTo(new TestUiScene());
		} else if (!Config.Debug.RELEASE_FINAL && keycode == Input.Keys.F10) {
			SceneSwitcher.switchTo(new PrototypeScene());
		} else if (!Config.Debug.RELEASE_FINAL && keycode == Input.Keys.F12) {
			handleException(new RuntimeException());
		}
		return false;
	}

	/**
	 * @return true if there is a game to resume
	 */
	boolean hasResumeGame() {
		ArrayList<GameSaveDef> gameSaves = ResourceCacheFacade.getAll(ExternalTypes.GAME_SAVE_DEF);
		boolean hasSaves = gameSaves.size() > 0;
		Pools.arrayList.free(gameSaves);
		return hasSaves;
	}

	/**
	 * Resumes the current game
	 */
	void resumeGame() {
		ArrayList<GameSaveDef> gameSaves = ResourceCacheFacade.getAll(ExternalTypes.GAME_SAVE_DEF);

		if (!gameSaves.isEmpty()) {
			GameScene gameScene = new GameScene(false, false);
			gameScene.setGameToResume(gameSaves.get(0));
			SceneSwitcher.switchTo(gameScene);
		}

		Pools.arrayList.free(gameSaves);
	}

	/**
	 * Goes to new game
	 */
	void newGame() {
		/** @todo change the simple load level to a more advanced level */
		SelectDefScene selectLevelScene = new SelectDefScene(ExternalTypes.LEVEL_DEF, false, true, false);
		SceneSwitcher.switchTo(selectLevelScene);
	}

	/**
	 * Goes to the editor
	 */
	void gotoEditor() {
		Scene scene = new EditorSelectionScene();
		SceneSwitcher.switchTo(scene);
	}

	/**
	 * All different available menus
	 */
	enum Menus {
		/** Main menu, or first menu the player sees */
		MAIN(MainMenuGui.class),
		/** Play menu, visible after clicking play in main menu */
		PLAY(PlayMenuGui.class),

		;

		/**
		 * @return new instance of this menu
		 */
		MenuGui newInstance() {
			try {
				return mGuiType.getConstructor().newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * Creates the enumeration with a GUI class
		 * @param gui the GUI class to create for this menu
		 */
		private Menus(Class<? extends MenuGui> gui) {
			mGuiType = gui;
		}

		/** The GUI class to create for this menu */
		Class<? extends MenuGui> mGuiType;
	}

	/**
	 * Pushes another GUI menu to the stack
	 * @param menu the menu to push to the stack
	 */
	void pushMenu(Menus menu) {
		MenuGui newGui = menu.newInstance();
		if (newGui != null) {
			newGui.setMenuScene(this);
			newGui.initGui();
			mInputMultiplexer.removeProcessor(mGui.getStage());
			mGui = newGui;
			mGuiStack.push(newGui);
			mInputMultiplexer.addProcessor(0, newGui.getStage());
			newGui.resetValues();
		}
	}

	/**
	 * Pops the current menu from the stack. Can not pop Main Menu from the stack.
	 */
	void popMenu() {
		if (mGui.getClass() != MainMenuGui.class) {
			mInputMultiplexer.removeProcessor(mGui.getStage());
			mGuiStack.pop().dispose();
			mGui = mGuiStack.peek();
			mInputMultiplexer.addProcessor(0, mGui.getStage());
		}
		// Logout
		else {
			Gdx.app.exit();
		}
	}

	/**
	 * Logs out the user
	 */
	void logout() {
		// Online
		if (mUser.isOnline()) {
			UserWebRepo.getInstance().logout(this);
		}
		// Offline
		else {
			mUser.setUsername("(invalid)");
			clearCurrentUser();
		}
	}

	/**
	 * Removes saved variables for the current user
	 */
	private void clearCurrentUser() {
		UserLocalRepo.removeLastUser();
		setNextScene(new LoginScene());
		setOutcome(Outcomes.LOGGED_OUT);
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof LogoutMethodResponse) {
			clearCurrentUser();
		}
	}

	/** Global user */
	private static final User mUser = User.getGlobalUser();
	/** First time we activated the scene */
	private boolean mFirstTimeActivation = true;
	/** GUI stack */
	private LinkedList<Gui> mGuiStack = new LinkedList<Gui>();
}
