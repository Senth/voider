package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.scene.ui.UiFactory;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.LevelGetAllMethod;
import com.spiddekauga.voider.network.entities.resource.LevelGetAllMethod.SortOrders;
import com.spiddekauga.voider.network.entities.resource.LevelGetAllMethodResponse;
import com.spiddekauga.voider.network.entities.resource.ResourceCommentGetMethod;
import com.spiddekauga.voider.network.entities.resource.ResourceCommentGetMethodResponse;
import com.spiddekauga.voider.network.entities.resource.ResourceDownloadMethod;
import com.spiddekauga.voider.network.entities.resource.ResourceDownloadMethodResponse;
import com.spiddekauga.voider.network.entities.stat.LevelInfoEntity;
import com.spiddekauga.voider.network.entities.stat.ResourceCommentEntity;
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
import com.spiddekauga.voider.utils.User;

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

		// Testing - DEV_SERVER
		if (Config.Debug.isBuildOrBelow(Builds.DEV_SERVER)) {
			if (keycode == Input.Keys.F5) {
				((ExploreGui) mGui).addComment("Senth", "This is a very very long test comment to see how it is shown in the panel",
						"2014-09-18 11:24");
			} else if (keycode == Input.Keys.F4) {
				((ExploreGui) mGui).resetComments();
			}
		}

		return false;
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		try {
			mWebResponses.put(new WebWrapper(method, response));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles existing web responses
	 */
	private void handleWepResponses() {
		while (!mWebResponses.isEmpty()) {
			try {
				WebWrapper webWrapper = mWebResponses.take();
				IEntity response = webWrapper.response;


				if (response instanceof LevelGetAllMethodResponse) {
					mLevelFetch.handleWebResponse((LevelGetAllMethod) webWrapper.method, (LevelGetAllMethodResponse) response);
				} else if (response instanceof ResourceDownloadMethodResponse) {
					handleResourceDownloadResponse((ResourceDownloadMethod) webWrapper.method, (ResourceDownloadMethodResponse) response);
				} else if (response instanceof ResourceCommentGetMethodResponse) {
					mCommentFetch.handleWebResponse((ResourceCommentGetMethod) webWrapper.method, (ResourceCommentGetMethodResponse) response);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
		mSelectedLevel = null;
		mLevelFetch.fetch(sort, tags);
	}

	/**
	 * Fetch levels from the server by the specified search string
	 * @param searchString the text to search for
	 */
	void fetchInitialLevels(String searchString) {
		mSelectedLevel = null;
		mLevelFetch.fetch(searchString);
	}

	/**
	 * @return true if we're currently fetching levels
	 */
	boolean isFetchingLevels() {
		return mLevelFetch.isFetching();
	}

	/**
	 * Fetch initial comments
	 */
	void fetchInitialComments() {
		mCommentFetch.fetch(false);
	}

	/**
	 * Fetch more comments
	 */
	void fetchMoreComments() {
		if (mCommentFetch.hasMore()) {
			mCommentFetch.fetch(true);
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

		((ExploreGui) mGui).resetInfo();

		// Get comments
		mCommentFetch.fetch(false);
	}

	/**
	 * Class for fetching comments for the current level
	 */
	private class CommentFetch {
		/**
		 * Fetch initial comments including user comments for current level
		 * @param fetchMore if more should be fetched
		 */
		void fetch(boolean fetchMore) {
			if (mSelectedLevel != null) {
				if (!fetchMore) {
					((ExploreGui) mGui).resetComments();
				}
				((ExploreGui) mGui).commentWaitIconAdd();
				UUID resourceId = mSelectedLevel.defEntity.resourceId;
				mResourceWebRepo.getComments(resourceId, fetchMore, ExploreScene.this);
				mIsFetching = true;
			}
		}

		/**
		 * @return true if more comments exist to fetch
		 */
		boolean hasMore() {
			if (mIsFetching || mSelectedLevel == null) {
				return false;
			}

			UUID resourceId = mSelectedLevel.defEntity.resourceId;
			return mResourceWebRepo.hasMoreComments(resourceId);
		}

		/**
		 * Handle web response
		 * @param method
		 * @param response
		 */
		void handleWebResponse(ResourceCommentGetMethod method, ResourceCommentGetMethodResponse response) {
			// Only do something if it's comments for the currently selected level
			if (mSelectedLevel != null && mSelectedLevel.defEntity.resourceId.equals(method.resourceId)) {
				mIsFetching = false;
				((ExploreGui) mGui).commentWaitIconRemove();

				switch (response.status) {
				case FAILED_CONNECTION:
					mGui.showErrorMessage("Failed to connect to the server");
					break;

				case FAILED_INTERNAL:
					mGui.showErrorMessage("Internal server error");
					break;

				case FAILED_USER_NOT_LOGGED_IN:
					mGui.showErrorMessage("You have been logged out");
					break;

				case SUCCESS_FETCHED_ALL:
				case SUCCESS_MORE_EXISTS:
					// Set user comment
					if (response.userComment != null) {
						String dateString = mUser.dateToString(response.userComment.date);
						((ExploreGui) mGui).setUserComment(response.userComment.comment, dateString);
					}

					// Add comments
					for (ResourceCommentEntity commentEntity : response.comments) {
						String dateString = mUser.dateToString(commentEntity.date);
						((ExploreGui) mGui).addComment(commentEntity.username, commentEntity.comment, dateString);
					}

					break;

				}
			}
		}

		User mUser = User.getGlobalUser();
		boolean mIsFetching = false;
	}

	/**
	 * Class for fetching levels
	 */
	private class LevelFetch {
		/**
		 * Fetch levels from search string
		 * @param searchString
		 */
		void fetch(String searchString) {
			((ExploreGui) mGui).resetContent();

			if (searchString != null && searchString.length() >= Config.Explore.SEARCH_LENGTH_MIN) {
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
				mIsFetching = true;
				mSearchString = null;
				mSortOrder = sortOrder;
				mTags = tags;

				((ExploreGui) mGui).resetContent();

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
					mGui.showErrorMessage("You have been logged out");
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

	private CommentFetch mCommentFetch = new CommentFetch();
	private LevelFetch mLevelFetch = new LevelFetch();
	private LevelInfoEntity mSelectedLevel = null;
	private ResourceWebRepo mResourceWebRepo = ResourceWebRepo.getInstance();
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
	/** Synchronized web responses */
	private BlockingQueue<WebWrapper> mWebResponses = new LinkedBlockingQueue<WebWrapper>();

}