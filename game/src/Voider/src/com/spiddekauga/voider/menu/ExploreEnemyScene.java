package com.spiddekauga.voider.menu;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.EnemyFetchMethod;
import com.spiddekauga.voider.network.entities.resource.EnemyFetchMethodResponse;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.repo.resource.ResourceWebRepo;
import com.spiddekauga.voider.utils.User;

/**
 * Scene for finding or loading enemies
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ExploreEnemyScene extends ExploreActorScene {
	/**
	 * Hidden constructor. Create from ExploreFactory
	 */
	ExploreEnemyScene() {
		super(new ExploreEnemyGui());

		((ExploreEnemyGui) mGui).setExploreEnemyScene(this);
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);
	}

	@Override
	boolean isFetchingContent() {
		return mEnemyFetch.isFetching();
	}

	@Override
	boolean hasMoreContent() {
		return mEnemyFetch.hasMore();
	}

	@Override
	void fetchMoreContent() {
		mEnemyFetch.fetchMore();
	}

	@Override
	void repopulateContent() {
		mEnemyFetch.fetch();
	}

	/**
	 * Fetch default content
	 */
	void fetchDefault() {
		mEnemyFetch.fetch(new EnemyFetchMethod());
	}

	@Override
	protected void onWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof EnemyFetchMethodResponse) {
			mEnemyFetch.handleWebResponse((EnemyFetchMethod) method, (EnemyFetchMethodResponse) response);
		} else {
			super.onWebResponse(method, response);
		}
	}

	@Override
	protected void onSelectAction() {
		// TODO
	}

	/**
	 * Class for fetching Enemies online
	 */
	private class EnemyFetch {
		/**
		 * Fetch initial enemies again.
		 */
		void fetch() {
			if (!mIsFetching && !mUser.isOnline() && mLastFetch != null) {
				mIsFetching = true;
				mResourceWebRepo.getEnemies(mLastFetch, false, ExploreEnemyScene.this);
			}
		}

		/**
		 * Fetch enemies from search criteria
		 * @param searchCriteria
		 */
		void fetch(EnemyFetchMethod searchCriteria) {
			if (!mUser.isOnline()) {
				return;
			}

			((ExploreEnemyGui) mGui).resetContent();

			mIsFetching = true;
			mLastFetch = searchCriteria.copy();
			mResourceWebRepo.getEnemies(searchCriteria, false, ExploreEnemyScene.this);
		}

		/**
		 * Fetch more enemies of the last search
		 */
		void fetchMore() {
			if (hasMore()) {
				mResourceWebRepo.getEnemies(mLastFetch, true, ExploreEnemyScene.this);
			}
		}

		/**
		 * @return true if more enemies can be fetched
		 */
		boolean hasMore() {
			if (mIsFetching || mUser.isOnline() || mLastFetch == null) {
				return false;
			}

			return mResourceWebRepo.hasMoreEnemies(mLastFetch);
		}

		/**
		 * Handle web response from the server
		 * @param method parameters to the server
		 * @param response server response
		 */
		void handleWebResponse(EnemyFetchMethod method, EnemyFetchMethodResponse response) {
			// Only do something if this was the last one we called
			if (isLastMethod(method)) {
				mIsFetching = false;

				// Create drawables and add content
				if (response.isSuccessful()) {
					createDrawables(response.enemies);
					((ExploreEnemyGui) mGui).addContent(response.enemies);
				} else {
					handleFailedStatus(response.status);
				}
			}
		}

		/**
		 * Checks if this was the last method we called
		 * @param method check if this method was the last one that was called
		 * @return true if this was the last method that was called
		 */
		boolean isLastMethod(EnemyFetchMethod method) {
			return method.equals(mLastFetch);
		}

		/**
		 * @return true if is fetching levels
		 */
		boolean isFetching() {
			return mIsFetching;
		}

		private ResourceWebRepo mResourceWebRepo = ResourceWebRepo.getInstance();
		private EnemyFetchMethod mLastFetch = null;
		private boolean mIsFetching = false;
		private User mUser = User.getGlobalUser();
	}

	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
	private EnemyFetch mEnemyFetch = new EnemyFetch();
	private EnemyFetchMethod mSearchCriteria = new EnemyFetchMethod();
}
