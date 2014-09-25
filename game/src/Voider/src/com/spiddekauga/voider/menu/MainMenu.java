package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.app.TestUiScene;
import com.spiddekauga.voider.editor.BulletEditor;
import com.spiddekauga.voider.editor.CampaignEditor;
import com.spiddekauga.voider.editor.EnemyEditor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.GameSaveDef;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.user.LoginMethodResponse;
import com.spiddekauga.voider.network.entities.user.LogoutMethodResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.user.UserLocalRepo;
import com.spiddekauga.voider.repo.user.UserWebRepo;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Pools;
import com.spiddekauga.voider.utils.Synchronizer;
import com.spiddekauga.voider.utils.Synchronizer.SyncEvents;
import com.spiddekauga.voider.utils.User;

/**
 * Main menu of the scene
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MainMenu extends Scene implements IResponseListener, Observer {
	/**
	 * Default constructor for main menu
	 */
	public MainMenu() {
		super(new MainMenuGui());
		((MainMenuGui) mGui).setMenuScene(this);
		mGuiStack.add(mGui);
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(InternalNames.UI_GENERAL);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.GAME_SAVE_DEF, false);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.BUG_REPORT, true);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
	}

	@Override
	protected void reloadResourcesOnActivate(Outcomes outcome, Object message) {
		super.reloadResourcesOnActivate(outcome, message);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.GAME_SAVE_DEF, false);
		ResourceCacheFacade.finishLoading();
	}

	@Override
	public void update(Observable observable, Object arg) {
		if (arg instanceof SyncEvents) {
			switch ((SyncEvents) arg) {
			case COMMUNITY_DOWNLOAD_SUCCESS:
			case USER_RESOURCES_DOWNLOAD_SUCCESS:
				ResourceCacheFacade.loadAllOf(this, ExternalTypes.GAME_SAVE_DEF, false);
				// ResourceCacheFacade.loadAllOf(this, ExternalTypes.LEVEL_DEF, false);
				ResourceCacheFacade.finishLoading();
				break;

			default:
				break;

			}
		}
	}

	@Override
	protected void onInit() {
		super.onInit();
		mSynchronizer.addObserver(this);
	}

	@Override
	protected void onDispose() {
		super.onDispose();
		mSynchronizer.deleteObserver(this);
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		if (outcome == Outcomes.LOADING_FAILED_CORRUPT_FILE) {
			/** @todo handle corrupt file */
		} else if (outcome == Outcomes.LOADING_FAILED_MISSING_FILE) {
			/** @todo handle missing file */
		} else if (outcome == Outcomes.DEF_SELECTED) {
			if (message instanceof ResourceItem) {
				GameScene gameScene = new GameScene(false, false);
				ResourceCacheFacade.load(gameScene, ((ResourceItem) message).id, false);
				ResourceCacheFacade.finishLoading();
				LevelDef loadedLevelDef = ResourceCacheFacade.get(((ResourceItem) message).id);
				gameScene.setLevelToLoad(loadedLevelDef);
				SceneSwitcher.switchTo(gameScene);
				Pools.resourceItem.free((ResourceItem) message);
			} else {
				Gdx.app.error("MainMenu", "When seleting def, message was not a ResourceItem but a " + message.getClass().getName());
			}
		}

		mGui.dispose();
		mGui.initGui();
		mGui.resetValues();


		// Show if logged in online
		if (outcome == Outcomes.LOGGED_IN) {
			// Synchronize
			if (mUser.isOnline()) {
				mGui.showSuccessMessage(mUser.getUsername() + " is now online!");
				Synchronizer.getInstance().synchronizeAll();
			} else {
				mGui.showHighlightMessage(mUser.getUsername() + " is now offline!");
			}

			if (message instanceof LoginMethodResponse) {
				LoginMethodResponse response = (LoginMethodResponse) message;
				switch (response.clientVersionStatus) {
				case NEW_VERSION_AVAILABLE:
					((MainMenuGui) mGui).showUpdateAvailable(response.latestClientVersion, response.changeLogMessage);
					break;

				case UPDATE_REQUIRED:
					((MainMenuGui) mGui).showUpdateNeeded(response.latestClientVersion, response.changeLogMessage);
					break;

				case UNKNOWN:
				case UP_TO_DATE:
					// Does nothing
					break;
				}
			}
		}
	}

	@Override
	public boolean onKeyDown(int keycode) {
		if (KeyHelper.isBackPressed(keycode)) {
			popMenu();
			return true;
		}

		// Testing
		if (Config.Debug.isBuildOrBelow(Builds.NIGHTLY)) {
			if (keycode == Input.Keys.F5) {
				SceneSwitcher.switchTo(new TestUiScene());
			} else if (keycode == Input.Keys.F6) {
				String message = "This is a longer error message with more text, a lot more text, see if it will wrap correctly later...";
				mGui.showMessage(message);
			} else if (keycode == Input.Keys.F10) {
				mGui.showConflictWindow();
			} else if (keycode == Input.Keys.F11) {
				User.getGlobalUser().makeOffline();
			} else if (keycode == Input.Keys.F12) {
				handleException(new RuntimeException());
			} else if (KeyHelper.isDeletePressed(keycode) && KeyHelper.isCtrlPressed()) {
				ResourceLocalRepo.removeAll(ExternalTypes.LEVEL);
				ResourceLocalRepo.removeAll(ExternalTypes.BULLET_DEF);
				ResourceLocalRepo.removeAll(ExternalTypes.LEVEL_DEF);
				ResourceLocalRepo.removeAll(ExternalTypes.ENEMY_DEF);
				ResourceLocalRepo.removeAll(ExternalTypes.GAME_SAVE);
				ResourceLocalRepo.removeAll(ExternalTypes.GAME_SAVE_DEF);
				ResourceLocalRepo.removeAll(ExternalTypes.PLAYER_DEF);
				ResourceLocalRepo.setSyncDownloadDate(new Date(0));
				ResourceLocalRepo.setSyncUserResourceDate(new Date(0));
			} else if (keycode == Input.Keys.HOME) {
				mSynchronizer.synchronizeAll();
			}
		}
		return false;
	}

	// -- Play --
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
		SelectDefScene selectLevelScene = new SelectDefScene(ExternalTypes.LEVEL_DEF, "Play", false, true, false);
		SceneSwitcher.switchTo(selectLevelScene);
	}

	// -- Explore --
	/**
	 * Goes to the explore screen
	 */
	void gotoExplore() {
		SceneSwitcher.switchTo(new ExploreScene());
	}

	// -- Create/Editors --
	/**
	 * Go to campaign editor
	 */
	void gotoCampaignEditor() {
		SceneSwitcher.switchTo(new CampaignEditor());
	}

	/**
	 * Go to level editor
	 */
	void gotoLevelEditor() {
		SceneSwitcher.switchTo(new LevelEditor());
	}

	/**
	 * Go to enemy editor
	 */
	void gotoEnemyEditor() {
		SceneSwitcher.switchTo(new EnemyEditor());
	}

	/**
	 * Go to bullet editor
	 */
	void gotoBulletEditor() {
		SceneSwitcher.switchTo(new BulletEditor());
	}

	/**
	 * All different available menus
	 */
	enum Menus {
		/** Main menu, or first menu the player sees */
		MAIN(MainMenuGui.class),
		/** Play menu, visible after clicking play in main menu */
		PLAY(PlayMenuGui.class),
		/** Editor menu */
		EDITOR(EditorSelectionGui.class, InternalNames.UI_EDITOR),

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
		 * Load resources for this menu
		 */
		void loadResources() {
			for (InternalNames resource : mResources) {
				ResourceCacheFacade.load(resource);
			}
			ResourceCacheFacade.finishLoading();
		}

		/**
		 * Unload resources for this menu
		 */
		void unloadResources() {
			for (InternalNames resource : mResources) {
				ResourceCacheFacade.unload(resource);
			}
		}

		/**
		 * Creates the enumeration with a GUI class
		 * @param gui the GUI class to create for this menu
		 * @param resources extra resources to load for the menu
		 */
		private Menus(Class<? extends MenuGui> gui, InternalNames... resources) {
			mGuiType = gui;
			mResources = resources;
		}

		/** The GUI class to create for this menu */
		private Class<? extends MenuGui> mGuiType;
		/** Resources to load/unload for this menu */
		private InternalNames[] mResources;
	}

	/**
	 * Pushes another GUI menu to the stack
	 * @param menu the menu to push to the stack
	 */
	void pushMenu(Menus menu) {
		MenuGui newGui = menu.newInstance();
		if (newGui != null) {
			menu.loadResources();
			newGui.setMenuScene(this);
			newGui.initGui();
			mInputMultiplexer.removeProcessor(mGui.getStage());
			mGui.hideAllMessages();
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
		// Quit game
		else {
			((MainMenuGui) mGui).showQuitMsgBox();
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
			clearCurrentUser();
		}
		mUser.logout();
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
	/** Synchronizer */
	private static Synchronizer mSynchronizer = Synchronizer.getInstance();
	/** GUI stack */
	private LinkedList<Gui> mGuiStack = new LinkedList<Gui>();
}
