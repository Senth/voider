package com.spiddekauga.voider.explore;

import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.AimTypes;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.resource.BulletDamageSearchRanges;
import com.spiddekauga.voider.network.resource.BulletSpeedSearchRanges;
import com.spiddekauga.voider.network.resource.CollisionDamageSearchRanges;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.network.resource.EnemyDefEntity;
import com.spiddekauga.voider.network.resource.EnemyFetchMethod;
import com.spiddekauga.voider.network.resource.EnemyFetchResponse;
import com.spiddekauga.voider.network.resource.EnemySpeedSearchRanges;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceWebRepo;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.scene.ui.UiFactory;

import java.util.ArrayList;

/**
 * Scene for finding or loading enemies
 */
public class ExploreEnemyScene extends ExploreActorScene {
private boolean mNewSearchCriteria = false;
private EnemyFetch mEnemyFetch = new EnemyFetch();
private EnemyFetchMethod mSearchCriteriaTemp = new EnemyFetchMethod();
private EnemyFetchMethod mSearchCriteria = new EnemyFetchMethod();

/**
 * Hidden constructor. Create from ExploreFactory
 * @param action the action to do when an enemy is selected
 */
ExploreEnemyScene(ExploreActions action) {
	super(new ExploreEnemyGui(), action, EnemyActorDef.class);

	getGui().setExploreEnemyScene(this);
}

@Override
protected ExploreEnemyGui getGui() {
	return (ExploreEnemyGui) super.getGui();
}

@Override
protected boolean isFetchingContent() {
	if (getView().isOnline()) {
		return mEnemyFetch.isFetching();
	} else {
		return false;
	}
}

@Override
protected boolean hasMoreContent() {
	if (getView().isOnline()) {
		return mEnemyFetch.hasMore();
	} else {
		return false;
	}
}

@Override
protected void fetchMoreContent() {
	if (getView().isOnline()) {
		mEnemyFetch.fetchMore();
	} else {
		// Does nothing
	}
}

@Override
protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
	super.onActivate(outcome, message, loadingOutcome);

	if (!User.getGlobalUser().isOnline()) {
		UiFactory.getInstance().msgBox.goOnline();
	}
}

@Override
protected void update(float deltaTime) {
	super.update(deltaTime);

	if (mNewSearchCriteria) {
		mNewSearchCriteria = false;

		if (getView().isOnline()) {
			mEnemyFetch.fetch(mSearchCriteria);
		} else {
			super.repopulateContent();
		}
	}
}

@Override
protected void loadResources() {
	super.loadResources();
	ResourceCacheFacade.load(this, InternalDeps.UI_EDITOR);
}

@Override
protected void onWebResponse(IMethodEntity method, IEntity response) {
	if (response instanceof EnemyFetchResponse) {
		mEnemyFetch.handleWebResponse((EnemyFetchMethod) method, (EnemyFetchResponse) response);
	} else {
		super.onWebResponse(method, response);
	}
}

@Override
protected void repopulateContent() {
	if (getView().isOnline()) {
		mEnemyFetch.fetch();
	}

	super.repopulateContent();
}

@Override
protected boolean defPassesFilter(DefEntity defEntity) {
	EnemyDefEntity enemyDefEntity = (EnemyDefEntity) defEntity;

	// Movement type
	if (!isObjectInFilterList(mSearchCriteria.movementTypes, enemyDefEntity.movementType)) {
		return false;
	}

	// Movement Speed
	if (enemyDefEntity.movementType != MovementTypes.STATIONARY) {
		if (!isObjectInFilterList(mSearchCriteria.movementSpeedRanges, EnemySpeedSearchRanges.getRange(enemyDefEntity.movementSpeed))) {
			return false;
		}
	}

	// Weapon
	if (mSearchCriteria.hasWeapon != null) {
		// Has weapon
		if (mSearchCriteria.hasWeapon != enemyDefEntity.hasWeapon) {
			return false;
		}

		// Only if we have any weapon
		if (mSearchCriteria.hasWeapon) {
			// Bullet speed
			if (!isObjectInFilterList(mSearchCriteria.bulletSpeedRanges, BulletSpeedSearchRanges.getRange(enemyDefEntity.bulletSpeed))) {
				return false;
			}

			// Bullet damage
			if (!isObjectInFilterList(mSearchCriteria.bulletDamageRanges, BulletDamageSearchRanges.getRange(enemyDefEntity.bulletDamage))) {
				return false;
			}

			// Aim type
			if (!isObjectInFilterList(mSearchCriteria.aimTypes, enemyDefEntity.aimType)) {
				return false;
			}
		}
	}

	// Destroy on collide
	if (mSearchCriteria.destroyOnCollide != null && mSearchCriteria.destroyOnCollide != enemyDefEntity.destroyOnCollide) {
		return false;
	}

	// Collision damage
	if (!isObjectInFilterList(mSearchCriteria.collisionDamageRanges, CollisionDamageSearchRanges.getRange(enemyDefEntity.collisionDamage))) {
		return false;
	}

	return super.defPassesFilter(defEntity);
}

/**
 * @return current search string we're searching for
 */
@Override
protected String getSearchString() {
	return mSearchCriteriaTemp.searchString;
}

/**
 * Set the search string to search after
 * @param searchString
 */
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
 * Updates the search criteria from the temporary criteria if they differ. New results will be
 * fetched in the next update
 */
private void updateSearchCriteria() {
	if (!mSearchCriteriaTemp.equals(mSearchCriteria)) {
		mSearchCriteria = mSearchCriteriaTemp.copy();
		mNewSearchCriteria = true;
	}
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
 * @return all movement speed categories that are checked
 */
ArrayList<EnemySpeedSearchRanges> getMovementSpeeds() {
	return mSearchCriteriaTemp.movementSpeedRanges;
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
 * @return all search criteria damage ranges
 */
ArrayList<CollisionDamageSearchRanges> getCollisionDamages() {
	return mSearchCriteriaTemp.collisionDamageRanges;
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
 * @return if we want to search for destroy on collide
 */
Boolean getDestroyOnCollide() {
	return mSearchCriteriaTemp.destroyOnCollide;
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
 * @return if we should search for weapons
 */
Boolean getHasWeapon() {
	return mSearchCriteriaTemp.hasWeapon;
}

/**
 * Sets if we should search for weapons
 * @param hasWeapon true to only search for weapons, false only if no weapon, null search both
 */
void setHasWeapon(Boolean hasWeapon) {
	mSearchCriteriaTemp.hasWeapon = hasWeapon;
	updateSearchCriteria();
}

/**
 * @return all bullet speeds that we're searching for
 */
ArrayList<BulletSpeedSearchRanges> getBulletSpeeds() {
	return mSearchCriteriaTemp.bulletSpeedRanges;
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
 * @return all bullet damage ranges that we're searching for
 */
ArrayList<BulletDamageSearchRanges> getBulletDamages() {
	return mSearchCriteriaTemp.bulletDamageRanges;
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
 * @return all aim types we're searching for
 */
ArrayList<AimTypes> getAimTypes() {
	return mSearchCriteriaTemp.aimTypes;
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

/**
 * Class for fetching Enemies online
 */
private class EnemyFetch {
	private ResourceWebRepo mResourceWebRepo = ResourceWebRepo.getInstance();
	private EnemyFetchMethod mLastFetch = null;
	private boolean mIsFetching = false;
	private User mUser = User.getGlobalUser();

	/**
	 * Fetch initial enemies again.
	 */
	void fetch() {
		if (!mIsFetching && mUser.isOnline()) {
			setSelected(null);
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
		if (mUser.isOnline()) {
			setSelected(null);
			getGui().resetContent();

			mIsFetching = true;
			mLastFetch = searchCriteria.copy();
			mResourceWebRepo.getEnemies(searchCriteria, false, ExploreEnemyScene.this);
		}
	}

	/**
	 * Fetch more enemies of the last search
	 */
	void fetchMore() {
		if (hasMore()) {
			mIsFetching = true;
			mResourceWebRepo.getEnemies(mLastFetch, true, ExploreEnemyScene.this);
		}
	}

	/**
	 * @return true if more enemies can be fetched
	 */
	boolean hasMore() {
		if (!mIsFetching && mUser.isOnline() && mLastFetch != null) {
			return mResourceWebRepo.hasMoreEnemies(mLastFetch);
		}

		return false;
	}

	/**
	 * Handle web response from the server
	 * @param method parameters to the server
	 * @param response server response
	 */
	void handleWebResponse(EnemyFetchMethod method, EnemyFetchResponse response) {
		// Only do something if this was the last one we called
		if (isLastMethod(method)) {
			mIsFetching = false;

			// Create drawables and add content
			if (response.isSuccessful()) {
				createDrawables(response.enemies);
				getGui().addContent(response.enemies);
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
	 * @return true if is fetching enemies
	 */
	boolean isFetching() {
		return mIsFetching;
	}
}
}