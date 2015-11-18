package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.Config;
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
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.sound.Music;
import com.spiddekauga.voider.sound.MusicInterpolations;
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

		ResourceCacheFacade.load(this, InternalNames.TXT_TERMS);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.GAME_SAVE_DEF, false);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.BUG_REPORT, true);
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

		case USER_CONNECTED:
		case USER_DISCONNECTED:
			updateUsername();
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
		eventDispatcher.connect(EventTypes.USER_CONNECTED, this);
		eventDispatcher.connect(EventTypes.USER_DISCONNECTED, this);
	}

	@Override
	protected void onDispose() {
		super.onDispose();
		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.disconnect(EventTypes.SYNC_COMMUNITY_DOWNLOAD_SUCCESS, this);
		eventDispatcher.disconnect(EventTypes.SYNC_USER_RESOURCES_UPLOAD_SUCCESS, this);
		eventDispatcher.disconnect(EventTypes.USER_CONNECTED, this);
		eventDispatcher.disconnect(EventTypes.USER_DISCONNECTED, this);
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

		updateUsername();
	}

	/**
	 * Update username
	 */
	private void updateUsername() {
		User user = User.getGlobalUser();
		getGui().resetUsername(user.getUsername(), user.isOnline());
	}

	/**
	 * Accept the terms
	 */
	void acceptTerms() {
		SettingInfoRepo infoRepo = SettingRepo.getInstance().info();
		infoRepo.acceptTerms();
	}

	/**
	 * Goto reddit community
	 */
	void gotoReddit() {
		Gdx.net.openURI(Config.Network.REDDIT_URL);
	}

	@Override
	public boolean onKeyDown(int keycode) {
		if (KeyHelper.isBackPressed(keycode)) {
			getGui().showQuitMsgBox();
			return true;
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
		/** User settings */
		USER(UserScene.class),

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

	private LinkedList<Gui> mGuiStack = new LinkedList<Gui>();
}
