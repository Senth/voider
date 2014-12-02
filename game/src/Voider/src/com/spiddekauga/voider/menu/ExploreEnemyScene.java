package com.spiddekauga.voider.menu;

import java.util.ArrayList;

import com.spiddekauga.voider.game.actors.AimTypes;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.BulletDamageSearchRanges;
import com.spiddekauga.voider.network.entities.resource.BulletSpeedSearchRanges;
import com.spiddekauga.voider.network.entities.resource.CollisionDamageSearchRanges;
import com.spiddekauga.voider.network.entities.resource.EnemyFetchMethod;
import com.spiddekauga.voider.network.entities.resource.EnemyFetchMethodResponse;
import com.spiddekauga.voider.network.entities.resource.EnemySpeedSearchRanges;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
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
	protected void loadResources() {
		super.loadResources();

		ResourceCacheFacade.load(InternalNames.UI_EDITOR);
	}

	@Override
	protected void unloadResources() {
		ResourceCacheFacade.unload(InternalNames.UI_EDITOR);

		super.unloadResources();
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		if (mNewSearchCriteria) {
			mNewSearchCriteria = false;

			if (mOnlineSearch) {
				mEnemyFetch.fetch(mSearchCriteria);
			}
		}
	}

	@Override
	boolean isFetchingContent() {
		if (mOnlineSearch) {
			return mEnemyFetch.isFetching();
		} else {
			return false;
		}
	}

	@Override
	boolean hasMoreContent() {
		if (mOnlineSearch) {
			return mEnemyFetch.hasMore();
		} else {
			return false;
		}
	}

	@Override
	void fetchMoreContent() {
		if (mOnlineSearch) {
			mEnemyFetch.fetchMore();
		} else {
			// Does nothing
		}
	}

	@Override
	void repopulateContent() {
		if (mOnlineSearch) {
			mEnemyFetch.fetch();
		} else {
			// TODO
		}
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

	/**
	 * Sets the movement type filters
	 * @param movementTypes
	 */
	void setMovementType(ArrayList<MovementTypes> movementTypes) {
		mSearchCriteriaTemp.movementTypes = movementTypes;
		updateSearchCriteria();
	}

	/**
	 * @return all movement types that are checked
	 */
	ArrayList<MovementTypes> getMovementTypes() {
		return mSearchCriteriaTemp.movementTypes;
	}

	/**
	 * Sets the movement speed category filters
	 * @param movementSpeeds
	 */
	void setMovementSpeeds(ArrayList<EnemySpeedSearchRanges> movementSpeeds) {
		mSearchCriteriaTemp.movementSpeedRanges = movementSpeeds;
		updateSearchCriteria();
	}

	/**
	 * @return all movement speed categories that are checked
	 */
	ArrayList<EnemySpeedSearchRanges> getMovementSpeeds() {
		return mSearchCriteriaTemp.movementSpeedRanges;
	}

	/**
	 * Sets the collision damage search ranges to search for
	 * @param collisionDamages
	 */
	void setCollisionDamages(ArrayList<CollisionDamageSearchRanges> collisionDamages) {
		mSearchCriteriaTemp.collisionDamageRanges = collisionDamages;
		updateSearchCriteria();
	}

	/**
	 * @return all search criteria damage ranges
	 */
	ArrayList<CollisionDamageSearchRanges> getCollisionDamages() {
		return mSearchCriteriaTemp.collisionDamageRanges;
	}

	/**
	 * Sets if we want to search for destroy on collide or not
	 * @param destroyOnCollide null to skip, true only destroyed, false only not destroyed
	 */
	void setDestroyOnCollide(Boolean destroyOnCollide) {
		mSearchCriteriaTemp.destroyOnCollide = destroyOnCollide;
		updateSearchCriteria();
	}

	/**
	 * @return if we want to search for destroy on collide
	 */
	Boolean getDestroyOnCollide() {
		return mSearchCriteriaTemp.destroyOnCollide;
	}

	/**
	 * Sets if we should search for weapons
	 * @param hasWeapon true to only search for weapons, false only if no weapon, null
	 *        search both
	 */
	void setHasWeapon(Boolean hasWeapon) {
		mSearchCriteriaTemp.hasWeapon = hasWeapon;
		updateSearchCriteria();
	}

	/**
	 * @return if we should search for weapons
	 */
	Boolean getHasWeapon() {
		return mSearchCriteriaTemp.hasWeapon;
	}

	/**
	 * Sets bullet speed ranges to search for
	 * @param bulletSpeeds
	 */
	void setBulletSpeeds(ArrayList<BulletSpeedSearchRanges> bulletSpeeds) {
		mSearchCriteriaTemp.bulletSpeedRanges = bulletSpeeds;
		updateSearchCriteria();
	}

	/**
	 * @return all bullet speeds that we're searching for
	 */
	ArrayList<BulletSpeedSearchRanges> getBulletSpeeds() {
		return mSearchCriteriaTemp.bulletSpeedRanges;
	}

	/**
	 * Sets bullet damage ranges to search for
	 * @param bulletDamages
	 */
	void setBulletDamages(ArrayList<BulletDamageSearchRanges> bulletDamages) {
		mSearchCriteriaTemp.bulletDamageRanges = bulletDamages;
		updateSearchCriteria();
	}

	/**
	 * @return all bullet damage ranges that we're searching for
	 */
	ArrayList<BulletDamageSearchRanges> getBulletDamages() {
		return mSearchCriteriaTemp.bulletDamageRanges;
	}

	/**
	 * Sets all aim types we're searching for
	 * @param aimTypes
	 */
	void setAimTypes(ArrayList<AimTypes> aimTypes) {
		mSearchCriteriaTemp.aimTypes = aimTypes;
		updateSearchCriteria();
	}

	/**
	 * @return all aim types we're searching for
	 */
	ArrayList<AimTypes> getAimTypes() {
		return mSearchCriteriaTemp.aimTypes;
	}

	/**
	 * Updates the search criteria from the temporary criteria if they differ. Also
	 * fetches new results
	 */
	private void updateSearchCriteria() {
		if (!mSearchCriteriaTemp.equals(mSearchCriteria)) {
			mSearchCriteria = mSearchCriteriaTemp.copy();
			mNewSearchCriteria = true;
		}
	}

	/**
	 * Clear/Reset search criteria
	 */
	void resetSearchCriteria() {
		EnemyFetchMethod emptySearchCriteria = new EnemyFetchMethod();
		if (!mSearchCriteriaTemp.equals(emptySearchCriteria)) {
			mSearchCriteriaTemp = emptySearchCriteria;
			mSearchCriteria = mSearchCriteriaTemp.copy();
			mNewSearchCriteria = true;
		}
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
			if (!mIsFetching && mUser.isOnline()) {
				setSelectedActor(null);
				if (mLastFetch == null) {
					mLastFetch = mSearchCriteria.copy();
				}
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

			setSelectedActor(null);
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

	private boolean mNewSearchCriteria = false;
	private boolean mOnlineSearch = true;
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
	private EnemyFetch mEnemyFetch = new EnemyFetch();
	/** For comparing with search criteria */
	private EnemyFetchMethod mSearchCriteriaTemp = new EnemyFetchMethod();
	private EnemyFetchMethod mSearchCriteria = new EnemyFetchMethod();
}
