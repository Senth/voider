package com.spiddekauga.voider.explore;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.GameSaveDef;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.resource.CommentFetchMethod;
import com.spiddekauga.voider.network.resource.CommentFetchResponse;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.network.resource.LevelDefEntity;
import com.spiddekauga.voider.network.resource.LevelFetchMethod;
import com.spiddekauga.voider.network.resource.LevelFetchMethod.SortOrders;
import com.spiddekauga.voider.network.resource.LevelFetchResponse;
import com.spiddekauga.voider.network.resource.LevelLengthSearchRanges;
import com.spiddekauga.voider.network.resource.LevelSpeedSearchRanges;
import com.spiddekauga.voider.network.stat.CommentEntity;
import com.spiddekauga.voider.network.stat.LevelInfoEntity;
import com.spiddekauga.voider.network.stat.Tags;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo.SettingDateRepo;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceWebRepo;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.ui.UiFactory;

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

		getGui().setExploreLevelScene(this);
	}

	@Override
	protected void loadResources() {
		super.loadResources();

		ResourceCacheFacade.loadAllOf(this, ExternalTypes.GAME_SAVE_DEF, false);
	}

	@Override
	protected void reloadResourcesOnActivate(Outcomes outcome, Object message) {
		super.reloadResourcesOnActivate(outcome, message);

		ResourceCacheFacade.loadAllOf(this, ExternalTypes.GAME_SAVE_DEF, false);
		ResourceCacheFacade.finishLoading();
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		// Set view
		if (loadingOutcome == Outcomes.LOADING_SUCCEEDED) {
			// Ask to go online?
			if (!User.getGlobalUser().isOnline()) {
				UiFactory.getInstance().msgBox.goOnline();
				setView(ExploreViews.LOCAL);
			}
			// User is online -> Set correct view
			else {
				switch (getSelectedAction()) {
				case LOAD:
				case SELECT:
					setView(ExploreViews.LOCAL);
					break;

				case PLAY:
					setView(ExploreViews.ONLINE_BROWSE);
					break;
				}
			}
			getGui().resetViewButtons();
		}
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		if (mNewSearchCriteria) {
			mNewSearchCriteria = false;

			if (getView().isOnline()) {
				mLevelFetch.fetch(mSearchCriteria);
			} else {
				super.repopulateContent();
			}
		}
	}

	@Override
	protected void onWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof LevelFetchResponse) {
			mLevelFetch.handleWebResponse((LevelFetchMethod) method, (LevelFetchResponse) response);
		} else if (response instanceof CommentFetchResponse) {
			mCommentFetch.handleWebResponse((CommentFetchMethod) method, (CommentFetchResponse) response);
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
		super.repopulateContent();
		if (getView().isOnline()) {
			mLevelFetch.fetch();
		}
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

	@Override
	protected void setView(ExploreViews view) {
		if (view == ExploreViews.ONLINE_BROWSE) {
			mSearchCriteriaTemp.search = false;
			mSearchCriteria.search = false;
		} else {
			mSearchCriteriaTemp.search = true;
			mSearchCriteria.search = true;
		}
		mLevelFetch.mLastFetch = mSearchCriteria.copy();

		super.setView(view);
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
		if (getView().isOnline()) {
			if (searchString.length() >= Config.Explore.SEARCH_LENGTH_MIN) {
				mSearchCriteriaTemp.searchString = searchString;
			} else {
				mSearchCriteriaTemp.searchString = "";
			}
			updateSearchCriteria();
		} else {
			super.setSearchString(searchString);
		}
	}

	/**
	 * @return current search string we're searching for
	 */
	@Override
	protected String getSearchString() {
		if (getView().isOnline()) {
			return mSearchCriteriaTemp.searchString;
		} else {
			return super.getSearchString();
		}
	}

	/**
	 * Set all tags we should filter by
	 * @param tags
	 * @param fetch set true to fetch new results
	 */
	void setTags(ArrayList<Tags> tags, boolean fetch) {
		mSearchCriteriaTemp.tags = tags;
		if (fetch) {
			updateSearchCriteria();
		}
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
		LevelDefEntity levelDefEntity = (LevelDefEntity) defEntity;

		// Level length
		if (!isObjectInFilterList(mSearchCriteria.levelLengths, LevelLengthSearchRanges.getRange(levelDefEntity.levelLength))) {
			return false;
		}

		// Level speed
		if (!isObjectInFilterList(mSearchCriteria.levelSpeeds, LevelSpeedSearchRanges.getRange(levelDefEntity.levelSpeed))) {
			return false;
		}

		return super.defPassesFilter(defEntity);
	}

	@Override
	protected void onSelectAction(ExploreActions action) {
		downloadResource(getSelected());
	}

	@Override
	protected void onResourceDownloaded(ExploreActions action) {
		switch (action) {
		case PLAY:
			runLevel();
			break;

		case LOAD:
			setOutcome(Outcomes.EXPLORE_LOAD, getSelected());
			break;

		case SELECT:
			setOutcome(Outcomes.EXPLORE_SELECT, getSelected());
			break;
		}
	}

	/**
	 * @return true if we can resume a level
	 */
	boolean hasResumeLevel() {
		return !ResourceCacheFacade.getAll(ExternalTypes.GAME_SAVE_DEF).isEmpty();
	}

	/**
	 * Resumes a previously started level
	 */
	void resumeLevel() {
		ArrayList<GameSaveDef> gameSaves = ResourceCacheFacade.getAll(ExternalTypes.GAME_SAVE_DEF);
		if (!gameSaves.isEmpty()) {
			GameSaveDef gameSaveDef = gameSaves.get(0);
			GameScene gameScene = new GameScene(false, false);
			gameScene.setGameToResume(gameSaveDef);
			SceneSwitcher.switchTo(gameScene);
		}
	}

	/**
	 * Run the actual level
	 */
	private void runLevel() {
		GameScene gameScene = new GameScene(false, false);
		ResourceCacheFacade.load(gameScene, getSelected().resourceId, false);
		ResourceCacheFacade.finishLoading();
		LevelDef levelDef = ResourceCacheFacade.get(getSelected().resourceId);
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
					getGui().resetComments();
				}
				getGui().commentWaitIconAdd();
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
		void handleWebResponse(CommentFetchMethod method, CommentFetchResponse response) {
			// Only do something if it's comments for the currently selected level
			if (mSelectedLevel != null && mSelectedLevel.defEntity.resourceId.equals(method.resourceId)) {
				mIsFetching = false;
				getGui().commentWaitIconRemove();

				if (response.isSuccessful()) {
					// Set user comment
					if (response.userComment != null) {
						String dateString = mDateRepo.getDate(response.userComment.date);
						getGui().setUserComment(response.userComment.comment, dateString);
					}

					// Add comments
					for (CommentEntity commentEntity : response.comments) {
						String dateString = mDateRepo.getDate(commentEntity.date);
						getGui().addComment(commentEntity.username, commentEntity.comment, dateString);
					}
				} else {
					handleFailedStatus(response.status);
				}
			}
		}

		boolean mIsFetching = false;
	}

	@Override
	protected ExploreLevelGui getGui() {
		return (ExploreLevelGui) super.getGui();
	}

	/**
	 * Class for fetching levels
	 */
	private class LevelFetch {
		/**
		 * Fetch initial levels (again).
		 */
		void fetch() {
			if (mUser.isOnline()) {
				if (mLastFetch == null) {
					mLastFetch = mSearchCriteria.copy();
				}
				fetch(mLastFetch);
			}
		}

		/**
		 * Fetch levels from the search or sort criteria
		 * @param searchSortCriteria
		 */
		void fetch(LevelFetchMethod searchSortCriteria) {
			if (mUser.isOnline()) {
				if (mLastFetch != searchSortCriteria) {
					mLastFetch = searchSortCriteria.copy();
				}

				setSelectedLevel(null);
				setSelected(null);
				mIsFetching = true;
				getGui().resetContent();
				mResourceWebRepo.getLevels(mLastFetch, false, ExploreLevelScene.this);
			}
		}

		/**
		 * Fetch more of same
		 */
		void fetchMore() {
			if (hasMore()) {
				mResourceWebRepo.getLevels(mLastFetch, true, ExploreLevelScene.this);
			}
		}

		/**
		 * @return true if more levels can be fetched
		 */
		boolean hasMore() {
			if (!mIsFetching && mUser.isOnline() && mLastFetch != null) {
				return mResourceWebRepo.hasMoreLevels(mLastFetch);
			}

			return false;
		}

		/**
		 * Handle response from server
		 * @param method
		 * @param response
		 */
		void handleWebResponse(LevelFetchMethod method, LevelFetchResponse response) {
			// Only do something if this was the one we last called
			if (mIsFetching && isLastMethod(method)) {
				mIsFetching = false;
				if (response.isSuccessful()) {
					createDrawables(response.levels);
					getGui().addContent(response.levels);
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