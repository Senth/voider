package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.search.Document;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.voider.game.actors.AimTypes;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.BulletDamageSearchRanges;
import com.spiddekauga.voider.network.entities.resource.BulletSpeedSearchRanges;
import com.spiddekauga.voider.network.entities.resource.EnemyDefEntity;
import com.spiddekauga.voider.network.entities.resource.EnemyFetchMethod;
import com.spiddekauga.voider.network.entities.resource.EnemyFetchMethodResponse;
import com.spiddekauga.voider.network.entities.resource.EnemySpeedSearchRanges;
import com.spiddekauga.voider.network.entities.resource.FetchStatuses;
import com.spiddekauga.voider.network.entities.resource.UploadTypes;
import com.spiddekauga.voider.network.util.ISearchStore;
import com.spiddekauga.voider.server.util.ActorFetch;
import com.spiddekauga.voider.server.util.ServerConfig;
import com.spiddekauga.voider.server.util.ServerConfig.SearchTables;
import com.spiddekauga.voider.server.util.ServerConfig.SearchTables.SEnemy;

/**
 * Servlet for fetching enemy information
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class EnemyFetch extends ActorFetch<EnemyDefEntity> {
	@Override
	protected void onInit() {
		super.onInit();

		mResponse = new EnemyFetchMethodResponse();
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (mUser.isLoggedIn()) {
			if (methodEntity instanceof EnemyFetchMethod) {
				mParameters = (EnemyFetchMethod) methodEntity;

				if (hasSearchOptions()) {
					searchAndSetFoundEnemies();
				} else {
					getAndSetNewestEnemies();
				}
			}
		} else {
			mResponse.status = FetchStatuses.FAILED_USER_NOT_LOGGED_IN;
		}

		return mResponse;
	}

	/**
	 * Checks if we should search for enemies or just get them
	 * @return true if we should perform a custom search, if false just get the newest
	 *         enemies
	 */
	private boolean hasSearchOptions() {
		if (mParameters.searchString != null && mParameters.searchString.length() >= ServerConfig.SEARCH_TEXT_LENGTH_MIN) {
			return true;
		}
		if (!mParameters.movementTypes.isEmpty() && mParameters.movementTypes.size() != MovementTypes.values().length) {
			return true;
		}
		if (!mParameters.movementSpeedRanges.isEmpty() && mParameters.movementSpeedRanges.size() != EnemySpeedSearchRanges.values().length) {
			return true;
		}
		if (mParameters.hasWeapon != null) {
			return true;
		}

		return false;
	}

	/**
	 * Search and set the enemies as a response
	 */
	private void searchAndSetFoundEnemies() {
		String searchString = buildSearchString();
	}

	/**
	 * Build and return the search string to use
	 * @return search string to use
	 */
	private String buildSearchString() {
		SearchUtils.Builder builder = new SearchUtils.Builder();

		// Free text search
		if (mParameters.searchString != null && mParameters.searchString.length() >= ServerConfig.SEARCH_TEXT_LENGTH_MIN) {
			builder.text(mParameters.searchString);
		}

		// Movement Type
		appendSearchEnumArray(SEnemy.MOVEMENT_TYPE, mParameters.movementTypes, MovementTypes.values().length, builder);

		// Movement Speed
		appendSearchEnumArray(SEnemy.MOVEMENT_SPEED_CAT, mParameters.movementSpeedRanges, EnemySpeedSearchRanges.values().length, builder);

		// Weapon
		if (mParameters.hasWeapon != null) {
			// Has weapon
			builder.bool(SEnemy.HAS_WEAPON, mParameters.hasWeapon);

			// Only if we have any weapon
			if (mParameters.hasWeapon) {
				// Bullet speed
				appendSearchEnumArray(SEnemy.BULLET_SPEED_CAT, mParameters.bulletSpeedRanges, BulletSpeedSearchRanges.values().length, builder);

				// Bullet damage
				appendSearchEnumArray(SEnemy.BULLET_DAMAGE_CAT, mParameters.bulletDamageRanges, BulletDamageSearchRanges.values().length, builder);

				// Aim types
				appendSearchEnumArray(SEnemy.AIM_TYPE, mParameters.aimTypes, AimTypes.values().length, builder);
			}
		}

		return builder.build();
	}

	/**
	 * Add an array of values into the search builder
	 * @param fieldName name of the field
	 * @param array the array to add
	 * @param maxLength maximum length of the array, if the array is of this size nothing
	 *        will be added
	 * @param builder the builder to add to
	 */
	private void appendSearchEnumArray(String fieldName, ArrayList<? extends ISearchStore> array, int maxLength, SearchUtils.Builder builder) {
		if (!array.isEmpty() && array.size() != maxLength) {
			String[] searchFor = new String[array.size()];
			for (int i = 0; i < searchFor.length; i++) {
				searchFor[i] = array.get(i).getSearchId();
			}
			builder.text(fieldName, searchFor);
		}
	}

	/**
	 * Get all newest enemies first. Set this as the response
	 */
	private void getAndSetNewestEnemies() {
		getNewestActors(UploadTypes.ENEMY_DEF, mParameters.nextCursor, mResponse.enemies);
		mResponse.cursor = getNextCursor();
		mResponse.status = getFetchStatus();
	}

	@Override
	protected void setAdditionalActorInformation(Entity publishedEntity, EnemyDefEntity networkEntity) {
		// From search
		String documentId = KeyFactory.keyToString(publishedEntity.getKey());
		Document document = SearchUtils.getDocument(SearchTables.ENEMY, documentId);
		if (document != null) {
			// Movement
			String movementTypeId = SearchUtils.getText(document, SEnemy.MOVEMENT_TYPE);
			if (movementTypeId != null) {
				networkEntity.movementType = MovementTypes.fromId(movementTypeId);

				// Movement speed
				if (networkEntity.movementType != MovementTypes.STATIONARY) {
					networkEntity.movementSpeed = SearchUtils.getFloat(document, SEnemy.MOVEMENT_SPEED);
				}
			}

			// Weapon
			networkEntity.hasWeapon = SearchUtils.getBoolean(document, SEnemy.HAS_WEAPON);
			if (networkEntity.hasWeapon) {
				networkEntity.bulletSpeed = SearchUtils.getFloat(document, SEnemy.BULLET_SPEED);
				networkEntity.bulletDamage = SearchUtils.getFloat(document, SEnemy.BULLET_DAMAGE);
				String aimTypeId = SearchUtils.getText(document, SEnemy.AIM_TYPE);
				if (aimTypeId != null) {
					networkEntity.aimType = AimTypes.fromId(aimTypeId);
				}
			}
		}
	}

	@Override
	protected EnemyDefEntity newActorDef() {
		return new EnemyDefEntity();
	}

	private EnemyFetchMethod mParameters = null;
	private EnemyFetchMethodResponse mResponse = null;

}
