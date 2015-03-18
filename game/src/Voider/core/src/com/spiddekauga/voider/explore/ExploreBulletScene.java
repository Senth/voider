package com.spiddekauga.voider.explore;

import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.resource.BulletFetchMethod;
import com.spiddekauga.voider.network.resource.BulletFetchResponse;
import com.spiddekauga.voider.repo.resource.ResourceWebRepo;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.spiddekauga.voider.utils.User;

/**
 * Scene for finding or loading bullets
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreBulletScene extends ExploreActorScene {
	/**
	 * Hidden constructor. Create from ExploreFactory
	 * @param action the action to do when a bullet is selected
	 */
	ExploreBulletScene(ExploreActions action) {
		super(new ExploreBulletGui(), action, BulletActorDef.class);

		getGui().setExploreBulletScene(this);
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		if (!User.getGlobalUser().isOnline()) {
			UiFactory.getInstance().msgBox.goOnline();
		}
	}

	@Override
	protected boolean isFetchingContent() {
		if (getView().isOnline()) {
			return mBulletFetch.isFetching();
		} else {
			return false;
		}
	}

	@Override
	protected boolean hasMoreContent() {
		if (getView().isOnline()) {
			return mBulletFetch.hasMore();
		} else {
			return false;
		}
	}

	@Override
	protected void fetchMoreContent() {
		if (getView().isOnline()) {
			mBulletFetch.fetchMore();
		} else {
			// Does nothing
		}
	}

	@Override
	protected void repopulateContent() {
		if (getView().isOnline()) {
			mBulletFetch.fetch();
		}

		super.repopulateContent();
	}

	/**
	 * Fetches new bullets with this search string
	 * @param searchString
	 */
	@Override
	protected void setSearchString(String searchString) {
		if (getView().isOnline()) {
			if (searchString.length() >= Config.Explore.SEARCH_LENGTH_MIN) {
				mBulletFetch.fetch(searchString);
			} else {
				mBulletFetch.fetch("");
			}
		} else {
			super.setSearchString(searchString);
		}
	}

	@Override
	protected String getSearchString() {
		if (getView().isOnline()) {
			return mBulletFetch.mLastSearch;
		} else {
			return super.getSearchString();
		}
	}

	@Override
	protected void onWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof BulletFetchResponse) {
			mBulletFetch.handleWebResponse((BulletFetchMethod) method, (BulletFetchResponse) response);
		} else {
			super.onWebResponse(method, response);
		}
	}

	@Override
	protected ExploreBulletGui getGui() {
		return (ExploreBulletGui) super.getGui();
	}

	private class BulletFetch {
		/**
		 * Fetch initial bullets again
		 */
		void fetch() {
			if (!mIsFetching && mUser.isOnline()) {
				setSelected(null);
			}
			mIsFetching = true;
			mResourceWebRepo.getBullets(mLastSearch, false, ExploreBulletScene.this);
		}

		/**
		 * Fetch bullets from search criteria
		 * @param searchCriteria
		 */
		void fetch(String searchCriteria) {
			if (mUser.isOnline() && !mLastSearch.equals(searchCriteria)) {
				setSelected(null);
				getGui().resetContent();

				mIsFetching = true;
				mLastSearch = searchCriteria;
				mResourceWebRepo.getBullets(mLastSearch, false, ExploreBulletScene.this);
			}
		}

		/**
		 * Fetch more bullets of the last search
		 */
		void fetchMore() {
			if (hasMore()) {
				mResourceWebRepo.getBullets(mLastSearch, true, ExploreBulletScene.this);
			}
		}

		/**
		 * @return true if more bullets can be fetched
		 */
		boolean hasMore() {
			if (!mIsFetching && mUser.isOnline()) {
				return mResourceWebRepo.hasMoreBullets(mLastSearch);
			}

			return false;
		}

		/**
		 * Handle web response from the server
		 * @param method parameters to the server
		 * @param response server response
		 */
		void handleWebResponse(BulletFetchMethod method, BulletFetchResponse response) {
			// Only do something if this was the last one we called
			if (isLastMethod(method)) {
				mIsFetching = false;

				// Create drawables and add content
				if (response.isSuccessful()) {
					createDrawables(response.bullets);
					getGui().addContent(response.bullets);
				} else {
					handleFailedStatus(response.status);
				}
			}
		}

		/**
		 * Checks if this was the last method we called
		 * @param method check if this was the last one that was called
		 * @return true if this was the last method that was called
		 */
		boolean isLastMethod(BulletFetchMethod method) {
			return method.searchString.equals(mLastSearch);
		}

		/**
		 * @return true if is fetching bullets
		 */
		boolean isFetching() {
			return mIsFetching;
		}

		private String mLastSearch = "";
		private ResourceWebRepo mResourceWebRepo = ResourceWebRepo.getInstance();
		private boolean mIsFetching = false;
		private User mUser = User.getGlobalUser();
	}

	private BulletFetch mBulletFetch = new BulletFetch();
}
