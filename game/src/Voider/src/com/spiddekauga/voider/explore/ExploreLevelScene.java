package com.spiddekauga.voider.explore;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.CommentFetchMethod;
import com.spiddekauga.voider.network.entities.resource.CommentFetchMethodResponse;
import com.spiddekauga.voider.network.entities.resource.DefEntity;
import com.spiddekauga.voider.network.entities.resource.LevelFetchMethod;
import com.spiddekauga.voider.network.entities.resource.LevelFetchMethod.SortOrders;
import com.spiddekauga.voider.network.entities.resource.LevelFetchMethodResponse;
import com.spiddekauga.voider.network.entities.resource.LevelLengthSearchRanges;
import com.spiddekauga.voider.network.entities.resource.LevelSpeedSearchRanges;
import com.spiddekauga.voider.network.entities.stat.CommentEntity;
import com.spiddekauga.voider.network.entities.stat.LevelInfoEntity;
import com.spiddekauga.voider.network.entities.stat.Tags;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo.SettingDateRepo;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
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
	 * Hidden constructor. Create from ExploreFactory
	 * @param action the action to do when a level is selected
	 */
	ExploreLevelScene(ExploreActions action) {
		super(new ExploreLevelGui(), action, LevelDef.class);

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
	protected void onWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof LevelFetchMethodResponse) {
			mLevelFetch.handleWebResponse((LevelFetchMethod) method, (LevelFetchMethodResponse) response);
		} else if (response instanceof CommentFetchMethodResponse) {
			mCommentFetch.handleWebResponse((CommentFetchMethod) method, (CommentFetchMethodResponse) response);
		} else {
			super.onWebResponse(method, response);
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
	protected boolean hasMoreContent() {
		if (getView().isOnline()) {
			return mLevelFetch.hasMore();
		} else {
			return false;
		}
	}

	@Override
	protected void fetchMoreContent() {
		if (getView().isOnline()) {
			mLevelFetch.fetchMore();
		} else {
			// Does nothing
		}
	}

	@Override
	protected boolean isFetchingContent() {
		if (getView().isOnline()) {
			return mLevelFetch.isFetching();
		} else {
			return false;
		}
	}

	@Override
	protected void repopulateContent() {
		if (getView().isOnline()) {
			mLevelFetch.fetch();
		}
		super.repopulateContent();
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
	 * Update the search criteria from the temporary search criteria if they differ. New
	 * results will be fetched in the next update
	 */
	private void updateSearchCriteria() {
		if (!mSearchCriteriaTemp.equals(mSearchCriteria)) {
			mSearchCriteria = mSearchCriteriaTemp.copy();
			mNewSearchCriteria = true;
		}
	}

	/**
	 * Set sort order
	 * @param sort sorting order to get levels by
	 */
	void setSortOrder(SortOrders sort) {
		mSearchCriteriaTemp.sort = sort;
		updateSearchCriteria();
	}

	/**
	 * Set level speed category search filter
	 * @param levelSpeeds
	 */
	void setLevelSpeeds(ArrayList<LevelSpeedSearchRanges> levelSpeeds) {
		mSearchCriteriaTemp.levelSpeeds = levelSpeeds;
		updateSearchCriteria();
	}

	@Override
	protected void setSearchString(String searchString) {
		if (searchString.length() >= Config.Explore.SEARCH_LENGTH_MIN) {
			mSearchCriteriaTemp.searchString = searchString;
		} else {
			mSearchCriteriaTemp.searchString = "";
		}
		updateSearchCriteria();
	}

	/**
	 * @return current search string we're searching for
	 */
	@Override
	protected String getSearchString() {
		return mSearchCriteriaTemp.searchString;
	}

	/**
	 * Set all tags we should filter by
	 * @param tags
	 */
	void setTags(ArrayList<Tags> tags) {
		mSearchCriteriaTemp.tags = tags;
		updateSearchCriteria();
	}

	/**
	 * @return all tags we should filter by
	 */
	ArrayList<Tags> getTags() {
		return mSearchCriteriaTemp.tags;
	}

	/**
	 * @return selected level speed search categories
	 */
	ArrayList<LevelSpeedSearchRanges> getLevelSpeeds() {
		return mSearchCriteriaTemp.levelSpeeds;
	}

	/**
	 * Set level length category search filters
	 * @param levelLengths
	 */
	void setLevelLengths(ArrayList<LevelLengthSearchRanges> levelLengths) {
		mSearchCriteriaTemp.levelLengths = levelLengths;
		updateSearchCriteria();
	}

	/**
	 * @return selected level length search categories
	 */
	ArrayList<LevelLengthSearchRanges> getLevelLengths() {
		return mSearchCriteriaTemp.levelLengths;
	}

	@Override
	protected boolean defPassesFilter(DefEntity defEntity) {
		// TODO

		return super.defPassesFilter(defEntity);
	}

	@Override
	protected void onSelectAction(ExploreActions action) {
		downloadResource(mSelectedLevel.defEntity);
	}

	@Override
	protected void onResourceDownloaded(ExploreActions action) {
		switch (action) {
		case PLAY:
			runLevel();
			break;

		case LOAD:
			setOutcome(Outcomes.EXPLORE_LOAD, mSelectedLevel.defEntity);
			break;

		case SELECT:
			setOutcome(Outcomes.EXPLORE_SELECT, mSelectedLevel.defEntity);
			break;
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
		void handleWebResponse(CommentFetchMethod method, CommentFetchMethodResponse response) {
			// Only do something if it's comments for the currently selected level
			if (mSelectedLevel != null && mSelectedLevel.defEntity.resourceId.equals(method.resourceId)) {
				mIsFetching = false;
				((ExploreLevelGui) mGui).commentWaitIconRemove();

				if (response.isSuccessful()) {
					// Set user comment
					if (response.userComment != null) {
						String dateString = mDateRepo.getDate(response.userComment.date);
						((ExploreLevelGui) mGui).setUserComment(response.userComment.comment, dateString);
					}

					// Add comments
					for (CommentEntity commentEntity : response.comments) {
						String dateString = mDateRepo.getDate(commentEntity.date);
						((ExploreLevelGui) mGui).addComment(commentEntity.username, commentEntity.comment, dateString);
					}
				} else {
					handleFailedStatus(response.status);
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
		 * Fetch initial level again. Only does something if {@link #fetch(String)} or
		 * {@link #fetch(SortOrders, ArrayList)} has been invoked before this method.
		 */
		void fetch() {
			if (!mIsFetching && mUser.isOnline()) {
				if (mSearchString != null) {
					mIsFetching = true;
					((ExploreLevelGui) mGui).resetContent();
					mResourceWebRepo.getLevels(mSearchString, false, ExploreLevelScene.this);
				} else if (mSortOrder != null) {
					mIsFetching = true;
					((ExploreLevelGui) mGui).resetContent();
					mResourceWebRepo.getLevels(mSortOrder, mTags, false, ExploreLevelScene.this);
				}
			}
		}

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
				mTags = null;

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
		void handleWebResponse(LevelFetchMethod method, LevelFetchMethodResponse response) {
			// Only do something if this was the one we last called
			if (isLastMethod(method)) {
				mIsFetching = false;

				if (response.isSuccessful()) {
					createDrawables(response.levels);
					((ExploreLevelGui) mGui).addContent(response.levels);
				} else {
					handleFailedStatus(response.status);
				}
			}
		}

		/**
		 * Checks if this was the last method we called
		 * @param method
		 * @return true if this was the last method we called
		 */
		boolean isLastMethod(LevelFetchMethod method) {
			return method.equals(mLastFetch);
		}

		/**
		 * @return true if is fetching levels
		 */
		boolean isFetching() {
			return mIsFetching;
		}

		private boolean mIsFetching = false;
		private LevelFetchMethod mLastFetch = null;
	}

	private boolean mNewSearchCriteria = false;
	private LevelFetchMethod mSearchCriteriaTemp = new LevelFetchMethod();
	private LevelFetchMethod mSearchCriteria = new LevelFetchMethod();
	private SettingDateRepo mDateRepo = SettingRepo.getInstance().date();
	private final User mUser = User.getGlobalUser();
	private CommentFetch mCommentFetch = new CommentFetch();
	private LevelFetch mLevelFetch = new LevelFetch();
	private LevelInfoEntity mSelectedLevel = null;
	private ResourceWebRepo mResourceWebRepo = ResourceWebRepo.getInstance();
}