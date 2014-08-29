package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.Iterator;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.scene.ui.UiFactory;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelInfoEntity;
import com.spiddekauga.voider.network.entities.Tags;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod.SortOrders;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethodResponse;
import com.spiddekauga.voider.network.entities.method.ResourceDownloadMethod;
import com.spiddekauga.voider.network.entities.method.ResourceDownloadMethodResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.InternalNames;
import com.spiddekauga.voider.repo.ResourceCacheFacade;
import com.spiddekauga.voider.repo.ResourceLocalRepo;
import com.spiddekauga.voider.repo.ResourceRepo;
import com.spiddekauga.voider.repo.ResourceWebRepo;
import com.spiddekauga.voider.repo.WebWrapper;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Graphics;
import com.spiddekauga.voider.utils.Pools;

/**
 * Scene for exploring new content
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreScene extends Scene implements IResponseListener {
	/**
	 * Default constructor
	 */
	public ExploreScene() {
		super(new ExploreGui());

		setClearColor(UiFactory.getInstance().getStyles().color.sceneBackground);

		((ExploreGui) mGui).setExploreScene(this);
	}

	@Override
	public void onResize(int width, int height) {
		super.onResize(width, height);

		mGui.resetValues();
	}

	@Override
	protected void loadResources() {
		super.loadResources();

		ResourceCacheFacade.load(InternalNames.UI_GENERAL);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();

		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);

		handleWepResponses();
	}

	@Override
	public boolean onKeyDown(int keycode) {
		super.onKeyDown(keycode);

		if (KeyHelper.isBackPressed(keycode)) {
			setOutcome(Outcomes.NOT_APPLICAPLE);
		}

		return false;
	}

	@Override
	public synchronized void handleWebResponse(IMethodEntity method, IEntity response) {
		mWebResponses.add(new WebWrapper(method, response));
	}

	/**
	 * Handles existing web responses
	 */
	private synchronized void handleWepResponses() {
		Iterator<WebWrapper> webIt = mWebResponses.iterator();

		while (webIt.hasNext()) {
			WebWrapper webWrapper = webIt.next();
			IEntity response = webWrapper.response;


			if (response instanceof LevelGetAllMethodResponse) {
				handleLevelGetAllResponse((LevelGetAllMethod) webWrapper.method, (LevelGetAllMethodResponse) response);
			} else if (response instanceof ResourceDownloadMethodResponse) {
				handleResourceDownloadResponse((ResourceDownloadMethod) webWrapper.method, (ResourceDownloadMethodResponse) response);
			}

			webIt.remove();
		}
	}

	/**
	 * Handles a response from downloading a level
	 * @param method parameters to the server method that was called
	 * @param response server response
	 */
	private void handleResourceDownloadResponse(ResourceDownloadMethod method, ResourceDownloadMethodResponse response) {
		mGui.hideWaitWindow();
		if (response.status.isSuccessful()) {
			runLevel();
		} else {
			switch (response.status) {
			case FAILED_CONNECTION:
				mGui.showErrorMessage("Could not connect to the server");
				break;
			case FAILED_DOWNLOAD:
				mGui.showErrorMessage("Download failed, please retry");
				break;
			case FAILED_SERVER_INTERAL:
				mGui.showErrorMessage("Internal server error, please file a bug report");
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Handles a level get response (fetched level info)
	 * @param method the method that was called
	 * @param response response from the server
	 */
	private void handleLevelGetAllResponse(LevelGetAllMethod method, LevelGetAllMethodResponse response) {
		if (method == mLastFetchMethod) {
			mFetchingLevels = false;
		}

		switch (response.status) {
		case FAILED_SERVER_CONNECTION:
			mGui.showErrorMessage("Failed to connect to the server");
			break;

		case FAILED_SERVER_ERROR:
			mGui.showErrorMessage("Internal server error");
			break;

		case FAILED_USER_NOT_LOGGED_IN:
			mGui.showErrorMessage("You are not logged in to the server");
			break;

		case SUCCESS_FETCHED_ALL:
		case SUCCESS_MORE_EXISTS:
			createDrawables(response.levels);
			((ExploreGui) mGui).addContent(response.levels);
			break;
		}
	}

	/**
	 * Creates drawables for all levels that are missing the drawables
	 * @param levels all levels to create a drawable for
	 */
	private void createDrawables(ArrayList<LevelInfoEntity> levels) {
		for (LevelInfoEntity level : levels) {
			if (level.defEntity.drawable == null && level.defEntity.png != null) {
				level.defEntity.drawable = Graphics.pngToDrawable(level.defEntity.png);
			}
		}
	}

	/**
	 * @return true if the server has more levels
	 */
	boolean hasMoreLevels() {
		if (mLastFetchMethod != null) {
			return mResourceWebRepo.hasMoreLevels(mLastFetchMethod);
		} else {
			return false;
		}
	}

	/**
	 * Fetch more levels of the currently displayed type
	 */
	void fetchMoreLevels() {
		if (mLastFetchMethod != null) {
			if (mResourceWebRepo.hasMoreLevels(mLastFetchMethod)) {
				if (mLastFetchMethod.searchString != null) {
					mLastFetchMethod = mResourceWebRepo.getLevels(this, mLastFetchMethod.searchString);
				} else {
					mLastFetchMethod = mResourceWebRepo.getLevels(this, mLastFetchMethod.sort, mLastFetchMethod.tagFilter);
				}

				mFetchingLevels = true;
			}
		}
	}

	/**
	 * Fetch initial levels from the server by the specified sort
	 * @param sort sorting order to get levels by
	 * @param tags selected tags
	 */
	void fetchInitialLevels(SortOrders sort, ArrayList<Tags> tags) {
		if (sort != null) {
			ArrayList<LevelInfoEntity> cachedLevels = mResourceWebRepo.getCachedLevels(sort, tags);

			if (cachedLevels.isEmpty()) {
				mLastFetchMethod = mResourceWebRepo.getLevels(this, sort, tags);
				mFetchingLevels = true;
				((ExploreGui) mGui).resetContent();
			} else {
				mLastFetchMethod = new LevelGetAllMethod();
				mLastFetchMethod.sort = sort;
				mLastFetchMethod.tagFilter = tags;
				((ExploreGui) mGui).resetContent(cachedLevels);
			}
		}
	}

	/**
	 * Fetch levels from the server by the specified search string
	 * @param searchString the text to search for
	 */
	void fetchInitialLevels(String searchString) {
		if (searchString.length() >= Config.Explore.SEARCH_LENGTH_MIN) {
			ArrayList<LevelInfoEntity> cachedLevels = mResourceWebRepo.getCachedLevels(searchString);

			if (cachedLevels.isEmpty()) {
				mLastFetchMethod = mResourceWebRepo.getLevels(this, searchString);
				mFetchingLevels = true;
				((ExploreGui) mGui).resetContent();
			} else {
				mLastFetchMethod = new LevelGetAllMethod();
				mLastFetchMethod.searchString = searchString;
				((ExploreGui) mGui).resetContent(cachedLevels);
			}
		} else {
			mLastFetchMethod = new LevelGetAllMethod();
			((ExploreGui) mGui).resetContent();
		}
	}

	/**
	 * Go back to main menu
	 */
	void gotoMainMenu() {
		setOutcome(Outcomes.NOT_APPLICAPLE);
	}

	/**
	 * Play the selected level
	 */
	void play() {
		if (mSelectedLevel != null) {
			// Already exists just start playing
			if (ResourceLocalRepo.exists(mSelectedLevel.defEntity.resourceId)) {
				runLevel();
			} else {
				mResourceRepo.download(this, mSelectedLevel.defEntity.resourceId);
				mGui.showWaitWindow("Downloading level...");
			}
		}
	}

	/**
	 * Run the actual level
	 */
	private void runLevel() {
		GameScene gameScene = new GameScene(false, false);
		ResourceCacheFacade.load(gameScene, mSelectedLevel.defEntity.resourceId, false);
		ResourceCacheFacade.finishLoading();
		LevelDef levelDef = ResourceCacheFacade.get(mSelectedLevel.defEntity.resourceId);
		if (levelDef != null) {
			gameScene.setLevelToLoad(levelDef);
			SceneSwitcher.switchTo(gameScene);
		} else {
			mGui.showErrorMessage("Could not load level, please send a bug report :)");
		}
	}

	/**
	 * @return all fetched levels
	 */
	@SuppressWarnings("unchecked")
	ArrayList<LevelInfoEntity> getLevels() {
		if (mLastFetchMethod != null) {
			return mResourceWebRepo.getCachedLevels(mLastFetchMethod);
		} else {
			return Pools.arrayList.obtain();
		}
	}

	/**
	 * @return the selected level, null if none are selected
	 */
	LevelInfoEntity getSelectedLevel() {
		return mSelectedLevel;
	}

	/**
	 * Sets the selected level
	 * @param level new selected level
	 */
	void setSelectedLevel(LevelInfoEntity level) {
		mSelectedLevel = level;
	}

	/**
	 * @return true if we're currently fetching levels
	 */
	boolean isFetchingLevels() {
		return mFetchingLevels;
	}

	/** Selected level */
	LevelInfoEntity mSelectedLevel = null;
	/** Resource web repository */
	private ResourceWebRepo mResourceWebRepo = ResourceWebRepo.getInstance();
	/** Resource repository */
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
	/** Synchronized web responses */
	private ArrayList<WebWrapper> mWebResponses = new ArrayList<>();
	/** Last method parameters that was used */
	private LevelGetAllMethod mLastFetchMethod = null;
	/** If we're fetching levels */
	private boolean mFetchingLevels = false;
}