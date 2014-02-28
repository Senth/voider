package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.spiddekauga.appengine.BlobUtils;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.entities.BulletDefEntity;
import com.spiddekauga.voider.network.entities.CampaignDefEntity;
import com.spiddekauga.voider.network.entities.DefEntity;
import com.spiddekauga.voider.network.entities.EnemyDefEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelDefEntity;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.network.entities.method.PublishMethod;
import com.spiddekauga.voider.network.entities.method.PublishMethodResponse;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Tries to publish one or more definitions
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class Publish extends VoiderServlet {
	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			return;
		}

		PublishMethodResponse methodResponse = new PublishMethodResponse();

		byte[] byteEntity = NetworkGateway.getEntity(request);
		IEntity networkEntity = NetworkEntitySerializer.deserializeEntity(byteEntity);

		if (networkEntity instanceof PublishMethod) {
			Map<UUID, BlobKey> blobKeys = BlobUtils.getBlobKeysFromUpload(request);
			Map<UUID, Key> datastoreKeys = new HashMap<>();

			// Add entities to datastore
			methodResponse.success = true;
			for (DefEntity defEntity : ((PublishMethod) networkEntity).defs) {
				methodResponse.success = addEntityToDatastore(defEntity, blobKeys, datastoreKeys);

				if (!methodResponse.success) {
					mLogger.severe("Failed to add all entities to the datastore, removing all");
					break;
				}
			}

			// Add dependencies
			if (methodResponse.success) {
				for (DefEntity defEntity : ((PublishMethod) networkEntity).defs) {
					methodResponse.success = addDependencies(defEntity, datastoreKeys, blobKeys);

					if (!methodResponse.success) {
						mLogger.severe("Failed to add all dependencies");
						break;
					}
				}

				// FAILED - TODO remove all dependencies
				if (!methodResponse.success) {

				}
			}

			// FAILED - TODO remove all published resources
			if (!methodResponse.success) {

			}
		}


		byte[] byteResponse = NetworkEntitySerializer.serializeEntity(methodResponse);
		NetworkGateway.sendResponse(response, byteResponse);
	}

	/**
	 * Add all dependencies for the specified resource
	 * @param defEntity the definition to add dependencies for
	 * @param datastoreKeys all datastore keys
	 * @param blobKeys all blob keys (dependencies)
	 * @return true if successful
	 */
	private boolean addDependencies(DefEntity defEntity, Map<UUID, Key> datastoreKeys, Map<UUID, BlobKey> blobKeys) {
		if (!defEntity.dependencies.isEmpty()) {
			Key datastoreKey = datastoreKeys.get(defEntity.resourceId);

			for (UUID dependency : defEntity.dependencies) {
				BlobKey blobKey = getBlobKey(dependency, blobKeys);

				if (blobKey != null) {
					Entity entity = new Entity(DatastoreTables.DEPENDENCY.toString());
					entity.setProperty("def_key", datastoreKey);
					entity.setProperty("dependency", blobKey);
					Key key = DatastoreUtils.mDatastore.put(entity);

					if (key == null) {
						mLogger.severe("Could not add dependency for " + dependency);
						return false;
					}
				} else {
					mLogger.severe("Could not find blob key for " + dependency);
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Tries to find the blob key, if it doesn't exist in the map it will
	 * try to find it in the Datastore instead and insert it to blobKeys
	 * @param resourceId the resource to get the blob key from
	 * @param blobKeys all current known blob keys
	 * @return Blob key for the resource, null if not found
	 */
	private BlobKey getBlobKey(UUID resourceId, Map<UUID, BlobKey> blobKeys) {
		BlobKey blobKey = blobKeys.get(resourceId);

		// Try datastore instead
		if (blobKey == null) {
			Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.PUBLISHED.toString(), "resource-id", resourceId);
			blobKey = (BlobKey) entity.getProperty("blob_key");

			if (blobKey != null) {
				blobKeys.put(resourceId, blobKey);
			}
		}

		return blobKey;
	}

	/**
	 * Add an entity to the datastore
	 * @param defEntity the entity to add to the datastore
	 * @param blobKeys all blob keys
	 * @param datastoreKeys adds the added datastore key to this map
	 * @return true if successful
	 */
	private boolean addEntityToDatastore(DefEntity defEntity, Map<UUID, BlobKey> blobKeys, Map<UUID, Key> datastoreKeys) {
		boolean success = false;
		Entity datastoreEntity = new Entity(DatastoreTables.PUBLISHED.toString());

		switch (defEntity.type) {
		case BULLET:
			success = appendBulletDefEntity(datastoreEntity, (BulletDefEntity) defEntity, blobKeys);
			break;

		case CAMPAIGN:
			success = appendCampaignDefEntity(datastoreEntity, (CampaignDefEntity) defEntity, blobKeys);
			break;

		case ENEMY:
			success = appendEnemyDefEntity(datastoreEntity, (EnemyDefEntity) defEntity, blobKeys);
			break;

		case LEVEL:
			success = appendLevelDefEntity(datastoreEntity, (LevelDefEntity) defEntity, blobKeys);
			break;

		default:
			mLogger.severe("Not implemented def type: " + defEntity.type);
			break;
		}

		if (success) {
			Key key = DatastoreUtils.mDatastore.put(datastoreEntity);

			if (key != null) {
				datastoreKeys.put(defEntity.resourceId, key);
			} else {
				success = false;
			}
		}

		return success;
	}

	/**
	 * Append DefEntity to the datastore entity
	 * @param datastoreEntity datastore entity
	 * @param defEntity the def entity to append to the datastore
	 * @param blobKeys all blob keys
	 * @return true if successful, false otherwise
	 */
	private boolean appendDefEntity(Entity datastoreEntity, DefEntity defEntity, Map<UUID, BlobKey> blobKeys) {
		// Type
		if (defEntity.type != null) {
			DatastoreUtils.setProperty(datastoreEntity, "type", defEntity.type.getId());
		} else {
			mLogger.severe("DefType is null for " + defEntity.resourceId);
			return false;
		}

		// Blob key
		BlobKey blobKey = blobKeys.get(defEntity.resourceId);
		if (blobKey != null) {
			DatastoreUtils.setProperty(datastoreEntity, "blob_key", blobKey);
		} else {
			mLogger.severe("Could not find blob key for " + defEntity.resourceId);
			return false;
		}

		// Creator key
		try {
			DatastoreUtils.setProperty(datastoreEntity, "creator_key", KeyFactory.stringToKey(defEntity.creatorKey));
			DatastoreUtils.setProperty(datastoreEntity, "original_creator_key", KeyFactory.stringToKey(defEntity.originalCreatorKey));
		} catch (IllegalArgumentException e) {
			return false;
		}


		// No-test properties
		DatastoreUtils.setProperty(datastoreEntity, "name", defEntity.name);
		DatastoreUtils.setProperty(datastoreEntity, "description", defEntity.description);
		DatastoreUtils.setProperty(datastoreEntity, "date", defEntity.date);
		DatastoreUtils.setProperty(datastoreEntity, "copy_parent_id", defEntity.copyParentId);
		DatastoreUtils.setProperty(datastoreEntity, "resource_id", defEntity.resourceId);

		// PNG image
		if (defEntity.png != null) {
			Blob blob = new Blob(defEntity.png);
			DatastoreUtils.setUnindexedProperty(datastoreEntity, "png", blob);
		}


		return true;
	}

	/**
	 * Append EnemyDefEntity to the datastore entity
	 * @param datastoreEntity datastore entity
	 * @param enemyDefEntity the enemy def entity to append to the datastore
	 * @param blobKeys all blob keys
	 * @return true if successful, false otherwise
	 */
	private boolean appendEnemyDefEntity(Entity datastoreEntity, EnemyDefEntity enemyDefEntity, Map<UUID, BlobKey> blobKeys) {
		if (enemyDefEntity.enemyMovementType != null) {
			DatastoreUtils.setProperty(datastoreEntity, "enemy_movement_type", enemyDefEntity.enemyMovementType.getId());
		} else {
			mLogger.severe("MovementType is null for " + enemyDefEntity.resourceId);
			return false;
		}


		// No-test properties
		DatastoreUtils.setProperty(datastoreEntity, "enemy_has_weapon", enemyDefEntity.enemyHasWeapon);


		return appendDefEntity(datastoreEntity, enemyDefEntity, blobKeys);
	}

	/**
	 * Append LevelDefEntity to the datastore entity
	 * @param datastoreEntity datastore entity
	 * @param LevelDefEntity the level def entity to append to the datastore
	 * @param blobKeys all blob keys
	 * @return true if successful, false otherwise
	 */
	private boolean appendLevelDefEntity(Entity datastoreEntity, LevelDefEntity LevelDefEntity, Map<UUID, BlobKey> blobKeys) {
		// Get blob key for level
		BlobKey blobKey = blobKeys.get(LevelDefEntity.levelId);
		if (blobKey != null) {
			DatastoreUtils.setProperty(datastoreEntity, "level_blob_key", blobKey);
		} else {
			mLogger.severe("Could not find blob key for level: " + LevelDefEntity.levelId);
			return false;
		}


		// No-test properties
		DatastoreUtils.setProperty(datastoreEntity, "level_length", LevelDefEntity.levelLength);


		return appendDefEntity(datastoreEntity, LevelDefEntity, blobKeys);
	}

	/**
	 * Append BulletDefEntity to the datastore entity
	 * @param datastoreEntity datastore entity
	 * @param bulletDefEntity the def entity to append to the datastore
	 * @param blobKeys all blob keys
	 * @return true if successful, false otherwise
	 */
	private boolean appendBulletDefEntity(Entity datastoreEntity, BulletDefEntity bulletDefEntity, Map<UUID, BlobKey> blobKeys) {
		return appendDefEntity(datastoreEntity, bulletDefEntity, blobKeys);
	}

	/**
	 * Append CampaignDefEntity to the datastore entity
	 * @param datastoreEntity datastore entity
	 * @param campaignDefEntity the def entity to append to the datastore
	 * @param blobKeys all blob keys
	 * @return true if successful, false otherwise
	 */
	private boolean appendCampaignDefEntity(Entity datastoreEntity, CampaignDefEntity campaignDefEntity, Map<UUID, BlobKey> blobKeys) {
		return appendDefEntity(datastoreEntity, campaignDefEntity, blobKeys);
	}

	/** Logger */
	private Logger mLogger = Logger.getLogger(Publish.class.getName());
}
