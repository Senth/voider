package com.spiddekauga.voider.menu;

import java.util.ArrayList;
import java.util.Iterator;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelInfoEntity;
import com.spiddekauga.voider.network.entities.Tags;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod.SortOrders;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethodResponse;
import com.spiddekauga.voider.repo.ICallerResponseListener;
import com.spiddekauga.voider.repo.ResourceWebRepo;
import com.spiddekauga.voider.repo.WebWrapper;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.Graphics;
import com.spiddekauga.voider.utils.Pools;

/**
 * Scene for exploring new content
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreScene extends Scene implements ICallerResponseListener {
	/**
	 * Default constructor
	 */
	public ExploreScene() {
		super(new ExploreGui());

		((ExploreGui)mGui).setExploreScene(this);
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
	public boolean keyDown(int keycode) {
		super.keyDown(keycode);

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

			if (webWrapper.method == mLastMethod) {
				mFetchingLevels = false;
			}

			if (response instanceof LevelGetAllMethodResponse) {
				switch (((LevelGetAllMethodResponse) response).status) {
				case FAILED_SERVER_CONNECTION:
					mGui.showErrorMessage("Failed to connect to the server");
					break;

				case FAILED_SERVER_ERROR:
					mGui.showErrorMessage("Internal server error");
					break;

				case SUCCESS_FETCHED_ALL:
				case SUCCESS_MORE_EXISTS:
					createDrawables(((LevelGetAllMethodResponse) response).levels);
					((ExploreGui)mGui).addContent(((LevelGetAllMethodResponse) response).levels);
					break;
				}
			}

			webIt.remove();
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
		if (mLastMethod != null) {
			return mResourceWebRepo.hasMoreLevels(mLastMethod);
		} else {
			return false;
		}
	}

	/**
	 * Fetch more levels of the currently displayed type
	 */
	void fetchMoreLevels() {
		if (mLastMethod != null) {
			if (mResourceWebRepo.hasMoreLevels(mLastMethod)) {
				if (mLastMethod.searchString != null) {
					mLastMethod = mResourceWebRepo.getLevels(this, mLastMethod.searchString);
				} else {
					mLastMethod = mResourceWebRepo.getLevels(this, mLastMethod.sort, mLastMethod.tagFilter);
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
				mLastMethod = mResourceWebRepo.getLevels(this, sort, tags);
				mFetchingLevels = true;
				((ExploreGui)mGui).resetContent();
			} else {
				mLastMethod = new LevelGetAllMethod();
				mLastMethod.sort = sort;
				mLastMethod.tagFilter = tags;
				((ExploreGui)mGui).resetContent(cachedLevels);
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
				mLastMethod = mResourceWebRepo.getLevels(this, searchString);
				mFetchingLevels = true;
				((ExploreGui)mGui).resetContent();
			} else {
				mLastMethod = new LevelGetAllMethod();
				mLastMethod.searchString = searchString;
				((ExploreGui)mGui).resetContent(cachedLevels);
			}
		} else {
			mLastMethod = new LevelGetAllMethod();
			((ExploreGui)mGui).resetContent();
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
		// TODO
	}

	/**
	 * @return all fetched levels
	 */
	@SuppressWarnings("unchecked")
	ArrayList<LevelInfoEntity> getLevels() {
		if (mLastMethod != null) {
			return mResourceWebRepo.getCachedLevels(mLastMethod);
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
	/** Synchronized web responses */
	private ArrayList<WebWrapper> mWebResponses = new ArrayList<>();
	/** Last method parameters that was used */
	private LevelGetAllMethod mLastMethod = null;
	/** If we're fetching levels */
	private boolean mFetchingLevels = false;
}