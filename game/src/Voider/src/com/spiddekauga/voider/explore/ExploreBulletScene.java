package com.spiddekauga.voider.explore;

import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.BulletFetchMethod;
import com.spiddekauga.voider.network.entities.resource.BulletFetchMethodResponse;
import com.spiddekauga.voider.repo.resource.ResourceWebRepo;
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
		super(new ExploreBulletGui(), action);

		((ExploreBulletGui) mGui).setExploreBulletScene(this);
	}

	/**
	 * Sets if we should search online or offline
	 * @param online true for online
	 */
	void setSearchOnline(boolean online) {
		if (mOnlineSearch != online) {
			mOnlineSearch = online;
			repopulateContent();
		}
	}

	/**
	 * @return true if we should search online
	 */
	boolean isSearchingOnline() {
		return mOnlineSearch;
	}

	@Override
	boolean isFetchingContent() {
		if (mOnlineSearch) {
			return mBulletFetch.isFetching();
		} else {
			return false;
		}
	}

	@Override
	boolean hasMoreContent() {
		if (mOnlineSearch) {
			return mBulletFetch.hasMore();
		} else {
			return false;
		}
	}

	@Override
	void fetchMoreContent() {
		if (mOnlineSearch) {
			mBulletFetch.fetchMore();
		} else {
			// Does nothing
		}
	}

	@Override
	void repopulateContent() {
		if (mOnlineSearch) {
			mBulletFetch.fetch();
		} else {
			// TODO
		}
	}

	/**
	 * Fetches new bullets with this search string
	 * @param searchString
	 */
	void fetch(String searchString) {
		if (mOnlineSearch) {
			if (searchString.length() >= Config.Explore.SEARCH_LENGTH_MIN) {
				mBulletFetch.fetch(searchString);
			} else {
				mBulletFetch.fetch("");
			}
		} else {
			// TODO
		}
	}

	@Override
	protected void onWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof BulletFetchMethodResponse) {
			mBulletFetch.handleWebResponse((BulletFetchMethod) method, (BulletFetchMethodResponse) response);
		} else {
			super.onWebResponse(method, response);
		}
	}

	private class BulletFetch {
		/**
		 * Fetch initial bullets again
		 */
		void fetch() {
			if (!mIsFetching && mUser.isOnline()) {
				setSelectedActor(null);
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
				setSelectedActor(null);
				((ExploreBulletGui) mGui).resetContent();

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
		void handleWebResponse(BulletFetchMethod method, BulletFetchMethodResponse response) {
			// Only do something if this was the last one we called
			if (isLastMethod(method)) {
				mIsFetching = false;

				// Create drawables and add content
				if (response.isSuccessful()) {
					createDrawables(response.bullets);
					((ExploreBulletGui) mGui).addContent(response.bullets);
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
	private boolean mOnlineSearch = true;
}
