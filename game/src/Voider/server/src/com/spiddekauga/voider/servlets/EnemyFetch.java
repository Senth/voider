package com.spiddekauga.voider.servlets;

import java.io.IOException;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.search.Document;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.voider.game.actors.AimTypes;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.resource.BulletDamageSearchRanges;
import com.spiddekauga.voider.network.resource.BulletSpeedSearchRanges;
import com.spiddekauga.voider.network.resource.CollisionDamageSearchRanges;
import com.spiddekauga.voider.network.resource.EnemyDefEntity;
import com.spiddekauga.voider.network.resource.EnemyFetchMethod;
import com.spiddekauga.voider.network.resource.EnemyFetchResponse;
import com.spiddekauga.voider.network.resource.EnemySpeedSearchRanges;
import com.spiddekauga.voider.network.resource.FetchStatuses;
import com.spiddekauga.voider.network.resource.UploadTypes;
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

		mResponse = new EnemyFetchResponse();
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (mUser.isLoggedIn()) {
			if (methodEntity instanceof EnemyFetchMethod) {
				mParameters = (EnemyFetchMethod) methodEntity;

				if (hasSearchOptions()) {
					mResponse.status = searchAndSetFoundDefs(SearchTables.ENEMY, mParameters.nextCursor, mResponse.enemies);
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

	@Override
	protected String buildSearchString() {
		SearchUtils.Builder builder = new SearchUtils.Builder();

		// Free text search
		if (mParameters.searchString != null && mParameters.searchString.length() >= ServerConfig.SEARCH_TEXT_LENGTH_MIN) {
			builder.text(mParameters.searchString);
		}

		// Movement Type
		appendSearchEnumArray(SEnemy.MOVEMENT_TYPE, mParameters.movementTypes, MovementTypes.values().length, builder);

		// Movement Speed
		if (mParameters.canUseMovementSpeed()) {
			appendSearchEnumArray(SEnemy.MOVEMENT_SPEED_CAT, mParameters.movementSpeedRanges, EnemySpeedSearchRanges.values().length, builder);
		}

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

		// Destroy on collide
		if (mParameters.destroyOnCollide != null) {
			builder.bool(SEnemy.DESTROY_ON_COLLIDE, mParameters.destroyOnCollide);
		}

		// Collision damage
		appendSearchEnumArray(SEnemy.COLLISION_DAMAGE_CAT, mParameters.collisionDamageRanges, CollisionDamageSearchRanges.values().length, builder);


		return builder.build();
	}

	/**
	 * Get all newest enemies first. Set this as the response
	 */
	private void getAndSetNewestEnemies() {
		getNewestActors(UploadTypes.ENEMY_DEF, mParameters.nextCursor, mResponse.enemies);
		mResponse.cursor = getNextCursor();
		mResponse.status = getFetchStatus();
	}

	/**
	 * Set search document information to an enemy definition
	 * @param document search document
	 * @param networkEntity
	 */
	private void searchToNetworkEntity(Document document, EnemyDefEntity networkEntity) {
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

			// Collision
			networkEntity.destroyOnCollide = SearchUtils.getBoolean(document, SEnemy.DESTROY_ON_COLLIDE);
			networkEntity.collisionDamage = SearchUtils.getFloat(document, SEnemy.COLLISION_DAMAGE);
		}
	}

	@Override
	protected void setAdditionalDefInformation(Entity publishedEntity, EnemyDefEntity networkEntity) {
		// From search
		String documentId = KeyFactory.keyToString(publishedEntity.getKey());
		Document document = SearchUtils.getDocument(SearchTables.ENEMY, documentId);
		searchToNetworkEntity(document, networkEntity);
	}

	@Override
	protected EnemyDefEntity newNetworkDef() {
		return new EnemyDefEntity();
	}

	private EnemyFetchMethod mParameters = null;
	private EnemyFetchResponse mResponse = null;

}
