package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.app.PrototypeScene;
import com.spiddekauga.voider.explore.ExploreActions;
import com.spiddekauga.voider.explore.ExploreFactory;
import com.spiddekauga.voider.game.GameSaveDef;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo.SettingInfoRepo;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
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
public class MainMenu extends MenuScene implements IEventListener {
	/**
	 * Default constructor for main menu
	 */
	public MainMenu() {
		super(new MainMenuGui());
		getGui().setScene(this);
		mGuiStack.add(getGui());
	}

	@Override
	protected void loadResources() {
		super.loadResources();

		ResourceCacheFacade.load(InternalNames.TXT_TERMS);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.GAME_SAVE_DEF, false);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.BUG_REPORT, true);
	}

	@Override
	protected void unloadResources() {
		ResourceCacheFacade.unload(InternalNames.TXT_TERMS);

		super.unloadResources();
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

		// Show if logged in online
		if (outcome == Outcomes.LOGGED_IN) {
			// Synchronize
			if (mUser.isOnline()) {
				Synchronizer.getInstance().synchronizeAll();
			}

			SettingInfoRepo infoRepo = SettingRepo.getInstance().info();


			// Check if the client was updated since last login
			if (infoRepo.isClientVersionNewSinceLastLogin()) {
				getGui().showChangesSinceLastLogin(infoRepo.getNewChangesSinceLastLogin());
				infoRepo.updateClientVersion();
			}

			// Check for new terms
			if (infoRepo.isTermsNew()) {
				String terms = ResourceCacheFacade.get(InternalNames.TXT_TERMS);
				getGui().showTerms(terms);
			}
		}
	}

	/**
	 * Accept the terms
	 */
	void acceptTerms() {
		SettingInfoRepo infoRepo = SettingRepo.getInstance().info();
		infoRepo.acceptTerms();
	}

	@Override
	public boolean onKeyDown(int keycode) {
		if (KeyHelper.isBackPressed(keycode)) {
			getGui().showQuitMsgBox();
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


	/**
	 * All different available menus
	 */
	enum Scenes {
		/** Editor menu */
		EDITOR(EditorSelectScene.class),
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
		private Scenes(Class<? extends Scene> type) {
			mType = type;
		}

		/** The GUI or Scene class to create for this menu */
		private Class<? extends Scene> mType = null;
	}

	/**
	 * Pushes another GUI menu to the stack
	 * @param scene the menu to push to the stack
	 */
	void gotoScene(Scenes scene) {
		Object newObject = scene.newInstance();
		if (newObject instanceof Scene) {
			Scene newScene = (Scene) newObject;
			SceneSwitcher.switchTo(newScene);
		}
	}

	@Override
	protected MainMenuGui getGui() {
		return (MainMenuGui) super.getGui();
	}

	private static final User mUser = User.getGlobalUser();
	private LinkedList<Gui> mGuiStack = new LinkedList<Gui>();
}
