package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.Iterator;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.scene.ui.UiFactory;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.LevelGetAllMethod;
import com.spiddekauga.voider.network.entities.resource.LevelGetAllMethod.SortOrders;
import com.spiddekauga.voider.network.entities.resource.LevelGetAllMethodResponse;
import com.spiddekauga.voider.network.entities.resource.ResourceDownloadMethod;
import com.spiddekauga.voider.network.entities.resource.ResourceDownloadMethodResponse;
import com.spiddekauga.voider.network.entities.stat.LevelInfoEntity;
import com.spiddekauga.voider.network.entities.stat.Tags;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebWrapper;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.repo.resource.ResourceWebRepo;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Graphics;

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
				mLevelFetch.handleWebResponse((LevelGetAllMethod) webWrapper.method, (LevelGetAllMethodResponse) response);
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
		return mLevelFetch.hasMore();
	}

	/**
	 * Fetch more levels of the currently displayed type
	 */
	void fetchMoreLevels() {
		mLevelFetch.fetchMore();
	}

	/**
	 * Fetch initial levels from the server by the specified sort
	 * @param sort sorting order to get levels by
	 * @param tags selected tags
	 */
	void fetchInitialLevels(SortOrders sort, ArrayList<Tags> tags) {
		mLevelFetch.fetch(sort, tags);
	}

	/**
	 * Fetch levels from the server by the specified search string
	 * @param searchString the text to search for
	 */
	void fetchInitialLevels(String searchString) {
		mLevelFetch.fetch(searchString);
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
		return mLevelFetch.isFetching();
	}

	/**
	 * Last fetch level parameters
	 */
	private class LevelFetch {
		/**
		 * Fetch levels from search string
		 * @param searchString
		 */
		void fetch(String searchString) {
			if (searchString != null && searchString.length() >= Config.Explore.SEARCH_LENGTH_MIN) {
				((ExploreGui) mGui).resetContent();

				mIsFetching = true;
				mSearchString = searchString;
				mSortOrder = null;
				mTags.clear();

				mResourceWebRepo.getLevels(searchString, false, ExploreScene.this);
			}
		}

		/**
		 * Fetch levels from sort order and tags
		 * @param sortOrder
		 * @param tags
		 */
		void fetch(SortOrders sortOrder, ArrayList<Tags> tags) {
			if (sortOrder != null) {
				((ExploreGui) mGui).resetContent();

				mIsFetching = true;
				mSearchString = null;
				mSortOrder = sortOrder;
				mTags = tags;

				mResourceWebRepo.getLevels(sortOrder, tags, false, ExploreScene.this);
			}
		}

		/**
		 * Fetch more of same
		 */
		void fetchMore() {
			if (!mIsFetching) {
				// Search string
				if (mSearchString != null) {
					mResourceWebRepo.getLevels(mSearchString, true, ExploreScene.this);
				}
				// Sort order
				else if (mSortOrder != null) {
					mResourceWebRepo.getLevels(mSortOrder, mTags, true, ExploreScene.this);
				}
			}
		}

		/**
		 * @return true if more levels can be fetched
		 */
		boolean hasMore() {
			if (mIsFetching) {
				return false;
			}

			// Search
			if (mSearchString != null) {
				return mResourceWebRepo.hasMoreLevels(mSearchString);
			}
			// Sort order
			else if (mSortOrder != null) {
				return mResourceWebRepo.hasMoreLevels(mSortOrder, mTags);
			}

			return false;
		}

		/**
		 * Handle response from server
		 * @param method
		 * @param response
		 */
		void handleWebResponse(LevelGetAllMethod method, LevelGetAllMethodResponse response) {
			// Only do something if this was the one we last called
			if (isLastMethod(method)) {
				mIsFetching = false;

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
		}

		/**
		 * Checks if this was the last method we called
		 * @param method
		 * @return true if this was the last method we called
		 */
		boolean isLastMethod(LevelGetAllMethod method) {
			// Search string
			if (mSearchString != null && method.searchString != null) {
				return mSearchString.equals(method.searchString);
			}
			// Sort order and tags
			if (mSortOrder != null && method.sort != null) {
				if (mSortOrder == method.sort) {
					// Check tags
					return mTags.equals(method.tagFilter);
				}
			}

			return false;
		}

		/**
		 * @return true if is fetching levels
		 */
		boolean isFetching() {
			return mIsFetching;
		}

		private boolean mIsFetching = false;
		private String mSearchString = null;
		private SortOrders mSortOrder = null;
		private ArrayList<Tags> mTags = new ArrayList<>();
	}

	/** Level fetch helper */
	LevelFetch mLevelFetch = new LevelFetch();
	/** Selected level */
	LevelInfoEntity mSelectedLevel = null;
	/** Resource web repository */
	private ResourceWebRepo mResourceWebRepo = ResourceWebRepo.getInstance();
	/** Resource repository */
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
	/** Synchronized web responses */
	private ArrayList<WebWrapper> mWebResponses = new ArrayList<>();
}