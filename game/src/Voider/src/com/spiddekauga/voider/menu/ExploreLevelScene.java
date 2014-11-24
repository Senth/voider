package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
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
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo.SettingDateRepo;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.repo.resource.ResourceWebRepo;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.spiddekauga.voider.utils.User;

/**
 * Scene for exploring new content
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreLevelScene extends ExploreScene implements IResponseListener {
	/**
	 * Default constructor
	 */
	public ExploreLevelScene() {
		super(new ExploreLevelGui());

		setClearColor(UiFactory.getInstance().getStyles().color.sceneBackground);

		((ExploreLevelGui) mGui).setExploreLevelScene(this);
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		// Ask to go online?
		if (!User.getGlobalUser().isOnline()) {
			((ExploreLevelGui) mGui).showGoOnlineDialog();
		}
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
	public boolean onKeyDown(int keycode) {
		super.onKeyDown(keycode);

		if (KeyHelper.isBackPressed(keycode)) {
			setOutcome(Outcomes.NOT_APPLICAPLE);
		}

		// Testing - DEV_SERVER
		if (Config.Debug.isBuildOrBelow(Builds.DEV_SERVER)) {
			if (keycode == Input.Keys.F5) {
				((ExploreLevelGui) mGui).addComment("Senth", "This is a very very long test comment to see how it is shown in the panel",
						"2014-09-18 11:24");
			} else if (keycode == Input.Keys.F4) {
				((ExploreLevelGui) mGui).resetComments();
			}
		}

		return false;
	}

	@Override
	protected void onWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof LevelGetAllMethodResponse) {
			mLevelFetch.handleWebResponse((LevelGetAllMethod) method, (LevelGetAllMethodResponse) response);
		} else if (response instanceof ResourceDownloadMethodResponse) {
			handleResourceDownloadResponse((ResourceDownloadMethod) method, (ResourceDownloadMethodResponse) response);
		} else if (response instanceof ResourceCommentGetMethodResponse) {
			mCommentFetch.handleWebResponse((ResourceCommentGetMethod) method, (ResourceCommentGetMethodResponse) response);
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
				mNotification.show(NotificationTypes.ERROR, "Could not connect to the server");
				break;
			case FAILED_DOWNLOAD:
				mNotification.show(NotificationTypes.ERROR, "Download failed, please retry");
				break;
			case FAILED_SERVER_INTERAL:
				mNotification.show(NotificationTypes.ERROR, "Internal server error, please file a bug report");
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
			createDrawable(level.defEntity);
		}
	}

	@Override
	boolean hasMoreContent() {
		return mLevelFetch.hasMore();
	}

	@Override
	void fetchMoreContent() {
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

	@Override
	boolean isFetchingContent() {
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
			mNotification.show(NotificationTypes.ERROR, "Could not load level, please send a bug report :)");
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

		((ExploreLevelGui) mGui).resetInfo();

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
			if (mSelectedLevel != null && mUser.isOnline()) {
				if (!fetchMore) {
					((ExploreLevelGui) mGui).resetComments();
				}
				((ExploreLevelGui) mGui).commentWaitIconAdd();
				UUID resourceId = mSelectedLevel.defEntity.resourceId;
				mResourceWebRepo.getComments(resourceId, fetchMore, ExploreLevelScene.this);
				mIsFetching = true;
			}
		}

		/**
		 * @return true if more comments exist to fetch
		 */
		boolean hasMore() {
			if (mIsFetching || mSelectedLevel == null || !mUser.isOnline()) {
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
				((ExploreLevelGui) mGui).commentWaitIconRemove();

				switch (response.status) {
				case FAILED_CONNECTION:
					mNotification.show(NotificationTypes.ERROR, "Failed to connect to the server");
					break;

				case FAILED_INTERNAL:
					mNotification.show(NotificationTypes.ERROR, "Internal server error");
					break;

				case FAILED_USER_NOT_LOGGED_IN:
					mNotification.show(NotificationTypes.ERROR, "You have been logged out");
					break;

				case SUCCESS_FETCHED_ALL:
				case SUCCESS_MORE_EXISTS:
					// Set user comment
					if (response.userComment != null) {
						String dateString = mDateRepo.getDate(response.userComment.date);
						((ExploreLevelGui) mGui).setUserComment(response.userComment.comment, dateString);
					}

					// Add comments
					for (ResourceCommentEntity commentEntity : response.comments) {
						String dateString = mDateRepo.getDate(commentEntity.date);
						((ExploreLevelGui) mGui).addComment(commentEntity.username, commentEntity.comment, dateString);
					}

					break;

				}
			}
		}

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
			if (!mUser.isOnline()) {
				return;
			}

			((ExploreLevelGui) mGui).resetContent();

			if (searchString != null && searchString.length() >= Config.Explore.SEARCH_LENGTH_MIN) {
				mIsFetching = true;
				mSearchString = searchString;
				mSortOrder = null;
				mTags.clear();

				mResourceWebRepo.getLevels(searchString, false, ExploreLevelScene.this);
			}
		}

		/**
		 * Fetch levels from sort order and tags
		 * @param sortOrder
		 * @param tags
		 */
		void fetch(SortOrders sortOrder, ArrayList<Tags> tags) {
			if (!mUser.isOnline()) {
				return;
			}

			if (sortOrder != null) {
				mIsFetching = true;
				mSearchString = null;
				mSortOrder = sortOrder;
				mTags = tags;

				((ExploreLevelGui) mGui).resetContent();

				mResourceWebRepo.getLevels(sortOrder, tags, false, ExploreLevelScene.this);
			}
		}

		/**
		 * Fetch more of same
		 */
		void fetchMore() {
			if (!mIsFetching && mUser.isOnline()) {
				// Search string
				if (mSearchString != null) {
					mResourceWebRepo.getLevels(mSearchString, true, ExploreLevelScene.this);
				}
				// Sort order
				else if (mSortOrder != null) {
					mResourceWebRepo.getLevels(mSortOrder, mTags, true, ExploreLevelScene.this);
				}
			}
		}

		/**
		 * @return true if more levels can be fetched
		 */
		boolean hasMore() {
			if (mIsFetching || !mUser.isOnline()) {
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
					mNotification.show(NotificationTypes.ERROR, "Failed to connect to the server");
					break;

				case FAILED_SERVER_ERROR:
					mNotification.show(NotificationTypes.ERROR, "Internal server error");
					break;

				case FAILED_USER_NOT_LOGGED_IN:
					mNotification.show(NotificationTypes.ERROR, "You have been logged out");
					break;

				case SUCCESS_FETCHED_ALL:
				case SUCCESS_MORE_EXISTS:
					createDrawables(response.levels);
					((ExploreLevelGui) mGui).addContent(response.levels);
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

	private SettingDateRepo mDateRepo = SettingRepo.getInstance().date();
	private final User mUser = User.getGlobalUser();
	private CommentFetch mCommentFetch = new CommentFetch();
	private LevelFetch mLevelFetch = new LevelFetch();
	private LevelInfoEntity mSelectedLevel = null;
	private ResourceWebRepo mResourceWebRepo = ResourceWebRepo.getInstance();
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();


}