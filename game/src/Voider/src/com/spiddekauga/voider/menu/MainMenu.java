package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.ClientVersions;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.app.TestUiScene;
import com.spiddekauga.voider.editor.BulletEditor;
import com.spiddekauga.voider.editor.CampaignEditor;
import com.spiddekauga.voider.editor.EnemyEditor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.editor.ShipEditor;
import com.spiddekauga.voider.explore.ExploreActions;
import com.spiddekauga.voider.explore.ExploreFactory;
import com.spiddekauga.voider.game.GameSaveDef;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo.SettingInfoRepo;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.user.UserLocalRepo;
import com.spiddekauga.voider.repo.user.UserWebRepo;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.spiddekauga.voider.sound.Music;
import com.spiddekauga.voider.sound.MusicInterpolations;
import com.spiddekauga.voider.sound.MusicPlayer;
import com.spiddekauga.voider.sound.SoundPlayer;
import com.spiddekauga.voider.sound.Sounds;
import com.spiddekauga.voider.utils.Synchronizer;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;
import com.spiddekauga.voider.utils.event.UpdateEvent;

/**
 * Main menu of the scene
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MainMenu extends Scene implements IEventListener {
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
		ResourceCacheFacade.load(InternalNames.MUSIC_TITLE);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.GAME_SAVE_DEF, false);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.BUG_REPORT, true);
		ResourceCacheFacade.load(InternalDeps.UI_SFX);

		// REMOVE
		if (Config.Debug.isBuildOrBelow(Builds.NIGHTLY_DEV)) {
			ResourceCacheFacade.load(InternalDeps.GAME_SFX);
			ResourceCacheFacade.load(InternalNames.MUSIC_GAME_OVER_INTRO);
			ResourceCacheFacade.load(InternalNames.MUSIC_GAME_OVER_LOOP);
		}
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
		ResourceCacheFacade.unload(InternalNames.MUSIC_TITLE);
		ResourceCacheFacade.unload(InternalDeps.UI_SFX);
	}

	@Override
	protected void reloadResourcesOnActivate(Outcomes outcome, Object message) {
		super.reloadResourcesOnActivate(outcome, message);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.GAME_SAVE_DEF, false);
		ResourceCacheFacade.finishLoading();
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.type) {
		case SYNC_COMMUNITY_DOWNLOAD_SUCCESS:
		case SYNC_USER_RESOURCES_DOWNLOAD_SUCCESS:
			ResourceCacheFacade.loadAllOf(this, ExternalTypes.GAME_SAVE_DEF, false);
			ResourceCacheFacade.finishLoading();
			break;

		default:
			break;
		}
	}

	@Override
	protected void onInit() {
		super.onInit();

		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.connect(EventTypes.SYNC_COMMUNITY_DOWNLOAD_SUCCESS, this);
		eventDispatcher.connect(EventTypes.SYNC_USER_RESOURCES_UPLOAD_SUCCESS, this);
	}

	@Override
	protected void onDispose() {
		super.onDispose();
		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.disconnect(EventTypes.SYNC_COMMUNITY_DOWNLOAD_SUCCESS, this);
		eventDispatcher.disconnect(EventTypes.SYNC_USER_RESOURCES_UPLOAD_SUCCESS, this);
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		mMusicPlayer.play(Music.TITLE, MusicInterpolations.FADE_IN);

		if (outcome == Outcomes.EXPLORE_LOAD) {
			if (message instanceof DefEntity) {
				DefEntity defEntity = (DefEntity) message;
				defEntity.revision = 0;
			}
			if (message instanceof ResourceItem) {
				GameScene gameScene = new GameScene(false, false);
				ResourceCacheFacade.load(gameScene, ((ResourceItem) message).id, false);
				ResourceCacheFacade.finishLoading();
				LevelDef loadedLevelDef = ResourceCacheFacade.get(((ResourceItem) message).id);
				gameScene.setLevelToLoad(loadedLevelDef);
				SceneSwitcher.switchTo(gameScene);
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
				Synchronizer.getInstance().synchronizeAll();
			}

			SettingInfoRepo infoRepo = SettingRepo.getInstance().info();

			if (message instanceof UpdateEvent) {
				UpdateEvent event = (UpdateEvent) message;
				switch (event.type) {
				case UPDATE_AVAILABLE:
					((MainMenuGui) mGui).showUpdateAvailable(event.latestClientVersion, event.changeLog);
					break;

				case UPDATE_REQUIRED:
					((MainMenuGui) mGui).showUpdateNeeded(event.latestClientVersion, event.changeLog);
					break;

				default:
					break;
				}
			}
			// Check if the client was updated since last login
			else if (infoRepo.isClientVersionNewSinceLastLogin()) {
				((MainMenuGui) mGui).showChangesSinceLastLogin(infoRepo.getNewChangesSinceLastLogin());
				infoRepo.updateClientVersion();
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
		if (Config.Debug.isBuildOrBelow(Builds.NIGHTLY_DEV)) {
			if (keycode == Input.Keys.F5) {
				if (KeyHelper.isAltPressed()) {
					SceneSwitcher.switchTo(ExploreFactory.create(BulletActorDef.class, ExploreActions.LOAD));
				} else if (KeyHelper.isShiftPressed()) {
					SceneSwitcher.switchTo(ExploreFactory.create(EnemyActorDef.class, ExploreActions.LOAD));
				} else {
					SceneSwitcher.switchTo(new TestUiScene());
				}
			} else if (keycode == Input.Keys.F6) {
				String message = "This is a longer error message with more text, a lot more text, see if it will wrap correctly later...";
				mNotification.show(message);
			} else if (KeyHelper.isAltPressed() && keycode == Input.Keys.F7) {
				String changeLog = ClientVersions.getChangeLogs(ClientVersions.V0_4_0);
				UiFactory.getInstance().msgBox.changeLog("ChangeLog", "Test", changeLog);
			} else if (KeyHelper.isShiftPressed() && keycode == Input.Keys.F7) {
			} else if (keycode == Input.Keys.F7) {
				UiFactory.getInstance().msgBox.bugReport();
			} else if (keycode == Input.Keys.F10) {
			} else if (keycode == Input.Keys.F11) {
			} else if (keycode == Input.Keys.F12) {
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

			}
			// Sounds
			else if (keycode == Input.Keys.A) {
				SoundPlayer.getInstance().stopAll();
			} else if (keycode == Input.Keys.O) {
				SoundPlayer.getInstance().play(Sounds.SHIP_LOW_HEALTH);
			} else if (keycode == Input.Keys.E) {
				SoundPlayer.getInstance().play(Sounds.BULLET_HIT_PLAYER);
			} else if (keycode == Input.Keys.U) {
				SoundPlayer.getInstance().play(Sounds.SHIP_COLLIDE);
			} else if (keycode == Input.Keys.I) {
				SoundPlayer.getInstance().play(Sounds.ENEMY_EXPLODES);
			} else if (keycode == Input.Keys.D) {
				SoundPlayer.getInstance().play(Sounds.SHIP_LOST);
			} else if (keycode == Input.Keys.H) {
				SoundPlayer.getInstance().play(Sounds.UI_BUTTON_HOVER);
			} else if (keycode == Input.Keys.T) {
				SoundPlayer.getInstance().play(Sounds.UI_BUTTON_CLICK);
			} else if (keycode == Input.Keys.M) {
				MusicPlayer.getInstance().play(Music.GAME_OVER_INTRO, MusicInterpolations.CROSSFADE);
				MusicPlayer.getInstance().queue(Music.GAME_OVER_LOOP);
			}
		}
		return super.onKeyDown(keycode);
	}

	// -- Play --
	/**
	 * @return true if there is a game to resume
	 */
	boolean hasResumeGame() {
		ArrayList<GameSaveDef> gameSaves = ResourceCacheFacade.getAll(ExternalTypes.GAME_SAVE_DEF);
		boolean hasSaves = gameSaves.size() > 0;
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
	}

	// -- Explore --
	/**
	 * Goes to the explore screen
	 */
	void gotoExplore() {
		SceneSwitcher.switchTo(ExploreFactory.create(LevelDef.class, ExploreActions.PLAY));
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
	 * Go to ship editor
	 */
	void gotoShipEditor() {
		SceneSwitcher.switchTo(new ShipEditor());
	}

	/**
	 * All different available menus
	 */
	enum Menus {
		/** Main menu, or first menu the player sees */
		MAIN(MainMenuGui.class),
		/** Editor menu */
		EDITOR(EditorSelectionGui.class),
		/** Credits */
		CREDITS(CreditScene.class),
		/** Game settings */
		SETTINGS(SettingsScene.class),

		;

		/**
		 * @return new instance of this menu
		 */
		Object newInstance() {
			try {
				return mType.getConstructor().newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * Creates the enumeration with a GUI or Scene class
		 * @param type can either be a MenuGui class or Scene class
		 */
		private Menus(Class<?> type) {
			if (MenuGui.class.isAssignableFrom(type)) {
				mType = type;
			} else if (Scene.class.isAssignableFrom(type)) {
				mType = type;
			}
		}

		/** The GUI or Scene class to create for this menu */
		private Class<?> mType = null;
	}

	/**
	 * Pushes another GUI menu to the stack
	 * @param menu the menu to push to the stack
	 */
	void pushMenu(Menus menu) {
		Object newObject = menu.newInstance();
		if (newObject instanceof MenuGui) {
			MenuGui newGui = (MenuGui) newObject;
			if (newGui != null) {
				newGui.setMenuScene(this);
				newGui.initGui();
				mInputMultiplexer.removeProcessor(mGui.getStage());
				mGui = newGui;
				mGuiStack.push(newGui);
				mInputMultiplexer.addProcessor(0, newGui.getStage());
				newGui.resetValues();
			}
		} else if (newObject instanceof Scene) {
			Scene newScene = (Scene) newObject;
			SceneSwitcher.switchTo(newScene);
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
			UserWebRepo.getInstance().logout();
		}
		clearCurrentUser();
		mUser.logout();
	}

	/**
	 * Removes saved variables for the current user
	 */
	private void clearCurrentUser() {
		UserLocalRepo.getInstance().removeLastUser();
		setNextScene(new LoginScene());
		setOutcome(Outcomes.LOGGED_OUT);
	}

	private static final User mUser = User.getGlobalUser();
	private LinkedList<Gui> mGuiStack = new LinkedList<Gui>();
}
