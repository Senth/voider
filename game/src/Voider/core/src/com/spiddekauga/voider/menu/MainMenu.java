package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;

import net._01001111.text.LoremIpsum;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.app.PrototypeScene;
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
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.network.misc.Motd.MotdTypes;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo.SettingInfoRepo;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.sound.Music;
import com.spiddekauga.voider.sound.MusicInterpolations;
import com.spiddekauga.voider.sound.SoundPlayer;
import com.spiddekauga.voider.sound.Sounds;
import com.spiddekauga.voider.utils.Synchronizer;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

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
		getGui().setMenuScene(this);
		mGuiStack.add(getGui());
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

		getGui().dispose();
		getGui().initGui();
		getGui().resetValues();


		// Show if logged in online
		if (outcome == Outcomes.LOGGED_IN) {
			// Synchronize
			if (mUser.isOnline()) {
				Synchronizer.getInstance().synchronizeAll();
			}

			SettingInfoRepo infoRepo = SettingRepo.getInstance().info();

			if (message instanceof LoginInfo) {
				LoginInfo loginInfo = (LoginInfo) message;

				// Update information
				if (loginInfo.updateInfo != null) {
					switch (loginInfo.updateInfo.type) {
					case UPDATE_AVAILABLE:
					case UPDATE_REQUIRED:
						((MainMenuGui) getGui()).showUpdateInfo(loginInfo.updateInfo);
						break;

					default:
						break;
					}
				}
				// Check if the client was updated since last login
				else if (infoRepo.isClientVersionNewSinceLastLogin()) {
					((MainMenuGui) getGui()).showChangesSinceLastLogin(infoRepo.getNewChangesSinceLastLogin());
					infoRepo.updateClientVersion();
				}

				// MOTD
				if (loginInfo.motds != null && !loginInfo.motds.isEmpty()) {
					showNewMotd(loginInfo.motds);
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
		if (Config.Debug.isBuildOrBelow(Builds.NIGHTLY_DEV)) {
			if (keycode == Input.Keys.F5) {
				if (KeyHelper.isCtrlPressed()) {

				} else if (KeyHelper.isShiftPressed()) {
					SceneSwitcher.switchTo(new PrototypeScene());
				} else {
					throw new RuntimeException("Test Bug Report");
				}
			} else if (keycode == Input.Keys.F6) {
			} else if (KeyHelper.isAltPressed() && keycode == Input.Keys.F7) {
			} else if (KeyHelper.isShiftPressed() && keycode == Input.Keys.F7) {
			} else if (keycode == Input.Keys.F7) {
				Motd motd = new Motd();
				LoremIpsum loremIpsum = new LoremIpsum();
				motd.content = loremIpsum.paragraphs(2);
				motd.title = loremIpsum.randomWord();
				motd.created = new Date();
				motd.type = MotdTypes.INFO;

				ArrayList<Motd> motds = new ArrayList<>();
				motds.add(motd);
				((MainMenuGui) getGui()).showMotds(motds);
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
			} else if (keycode == Input.Keys.U) {
				SoundPlayer.getInstance().play(Sounds.SHIP_COLLIDE);
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
				mInputMultiplexer.removeProcessor(getGui().getStage());
				setGui(newGui);
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
		if (getGui().getClass() != MainMenuGui.class) {
			mInputMultiplexer.removeProcessor(getGui().getStage());
			mGuiStack.pop().dispose();
			setGui(mGuiStack.peek());
			mInputMultiplexer.addProcessor(0, getGui().getStage());
		}
		// Quit game
		else {
			((MainMenuGui) getGui()).showQuitMsgBox();
		}
	}

	/**
	 * Show new MOTD
	 * @param motds
	 */
	private void showNewMotd(ArrayList<Motd> motds) {
		// Remove MOTD we already have shown the user
		SettingInfoRepo infoRepo = SettingRepo.getInstance().info();
		infoRepo.filterMotds(motds);

		// Sort these by created date (oldest first)
		Collections.sort(motds, new Comparator<Motd>() {
			@Override
			public int compare(Motd o1, Motd o2) {
				if (o1.created.before(o2.created)) {
					return -1;
				}
				if (o1.created.after(o2.created)) {
					return 1;
				}
				return 0;
			}
		});

		((MainMenuGui) getGui()).showMotds(motds);
	}

	/**
	 * Call this once a message of the day has been clicked away
	 * @param motd the MOTD
	 */
	void motdViewed(Motd motd) {
		SettingInfoRepo infoRepo = SettingRepo.getInstance().info();
		infoRepo.setLatestMotdDate(motd);
	}

	@Override
	protected MenuGui getGui() {
		return (MenuGui) super.getGui();
	}

	private static final User mUser = User.getGlobalUser();
	private LinkedList<Gui> mGuiStack = new LinkedList<Gui>();
}
