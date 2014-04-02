package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Document.Builder;
import com.google.appengine.api.search.Field;
import com.spiddekauga.appengine.BlobUtils;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.voider.network.entities.BulletDefEntity;
import com.spiddekauga.voider.network.entities.CampaignDefEntity;
import com.spiddekauga.voider.network.entities.DefEntity;
import com.spiddekauga.voider.network.entities.EnemyDefEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelDefEntity;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.network.entities.method.PublishMethod;
import com.spiddekauga.voider.network.entities.method.PublishMethodResponse;
import com.spiddekauga.voider.network.entities.method.PublishMethodResponse.Statuses;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.TokenSizes;
import com.spiddekauga.voider.server.util.UserRepo;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Tries to publish one or more definitions
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Publish extends VoiderServlet {
	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			return;
		}

		PublishMethodResponse methodResponse = new PublishMethodResponse();
		boolean success = false;

		byte[] byteEntity = NetworkGateway.getEntity(request);
		IEntity networkEntity = NetworkEntitySerializer.deserializeEntity(byteEntity);

		if (networkEntity instanceof PublishMethod) {
			Map<UUID, BlobKey> blobKeys = BlobUtils.getBlobKeysFromUpload(request);
			Map<UUID, Key> datastoreKeys = new HashMap<>();

			// Add entities to datastore and search
			success = true;
			for (DefEntity defEntity : ((PublishMethod) networkEntity).defs) {
				Key datastoreKey = addEntityToDatastore(defEntity, blobKeys, datastoreKeys);

				if (datastoreKey != null) {
					if (defEntity instanceof LevelDefEntity) {
						success = createEmptyLevelStatistics(datastoreKey);
					}

					createSearchDocument(defEntity, datastoreKey);
				} else {
					success = false;
					mLogger.severe("Failed to add all entities to the datastore, removing all");
					break;
				}
			}

			// Add dependencies
			if (success) {
				for (DefEntity defEntity : ((PublishMethod) networkEntity).defs) {
					success = addDependencies(defEntity, datastoreKeys, blobKeys);

					if (!success) {
						methodResponse.status = Statuses.FAILED_SERVER_ERROR;
						mLogger.severe("Failed to add all dependencies");
						break;
					}
				}
			}

			// Add search documents
			if (success) {
				success = addSearchDocuments();
			}

			// FAILED - TODO remove all resources from published, dependencies, and search
			if (!success) {

			}

			// Set method response status
			methodResponse.status = success ? Statuses.SUCCESS : Statuses.FAILED_SERVER_ERROR;
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
					Entity entity = new Entity(DatastoreTables.DEPENDENCY.toString(), datastoreKey);
					entity.setProperty("dependency", blobKey);
					Key key = DatastoreUtils.put(entity);

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
	 * Create empty level statistics
	 * @param key datastore key of the level entity to add empty statistics for
	 * @return true if successful, false otherwise
	 */
	private boolean createEmptyLevelStatistics(Key key) {
		Entity entity = new Entity(DatastoreTables.LEVEL_STAT.toString(), key);

		entity.setProperty("play_count", 0);
		entity.setProperty("likes", 0);
		entity.setProperty("rating_sum", 0);
		entity.setProperty("ratings", 0);
		entity.setProperty("rating_avg", 0.0);
		entity.setProperty("clear_count", 0);

		Key statKey = DatastoreUtils.put(entity);

		return statKey != null;
	}

	/**
	 * Add an entity to the datastore
	 * @param defEntity the entity to add to the datastore
	 * @param blobKeys all blob keys
	 * @param datastoreKeys adds the added datastore key to this map
	 * @return datastore key of the def entity
	 */
	private Key addEntityToDatastore(DefEntity defEntity, Map<UUID, BlobKey> blobKeys, Map<UUID, Key> datastoreKeys) {
		boolean success = false;
		Entity datastoreEntity = new Entity(DatastoreTables.PUBLISHED.toString(), mUser.getKey());

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
			Key key = DatastoreUtils.put(datastoreEntity);

			if (key != null) {
				datastoreKeys.put(defEntity.resourceId, key);
				return key;
			}
		}

		return null;
	}

	/**
	 * Add all search documents that were created
	 * @return true if all search documents were added
	 */
	private boolean addSearchDocuments() {
		boolean success = true;

		for (Entry<String, ArrayList<Document>> entry : mSearchDocumentsToAdd.entrySet()) {
			String typeName = entry.getKey();
			ArrayList<Document> documents = entry.getValue();

			success = SearchUtils.indexDocuments(typeName, documents);

			if (!success) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Create search document to be added later
	 * @param defEntity the entity to create search document for
	 * @param datastoreKey the key of the entity that was added
	 */
	private void createSearchDocument(DefEntity defEntity, Key datastoreKey) {
		Builder builder = Document.newBuilder();

		builder.setId(KeyFactory.keyToString(datastoreKey));

		switch (defEntity.type) {
		case BULLET:
			appendBulletDefEntity(builder, (BulletDefEntity) defEntity);
			break;

		case CAMPAIGN:
			appendCampaignDefEntity(builder, (CampaignDefEntity) defEntity);
			break;

		case ENEMY:
			appendEnemyDefEntity(builder, (EnemyDefEntity) defEntity);
			break;

		case LEVEL:
			appendLevelDefEntity(builder, (LevelDefEntity) defEntity);
			break;

		default:
			mLogger.severe("Not implemented def type: " + defEntity.type);
			break;

		}


		// Get array list
		ArrayList<Document> documentList = mSearchDocumentsToAdd.get(defEntity.type.toString());
		if (documentList == null) {
			documentList = new ArrayList<>();
			mSearchDocumentsToAdd.put(defEntity.type.toString(), documentList);
		}

		documentList.add(builder.build());
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

		// Original creator key
		try {
			DatastoreUtils.setProperty(datastoreEntity, "original_creator_key", KeyFactory.stringToKey(defEntity.originalCreatorKey));
		} catch (IllegalArgumentException e) {
			return false;
		}


		// No-test properties
		DatastoreUtils.setProperty(datastoreEntity, "date", defEntity.date);
		DatastoreUtils.setProperty(datastoreEntity, "copy_parent_id", defEntity.copyParentId);
		DatastoreUtils.setProperty(datastoreEntity, "resource_id", defEntity.resourceId);
		DatastoreUtils.setUnindexedProperty(datastoreEntity, "name", defEntity.name);
		DatastoreUtils.setUnindexedProperty(datastoreEntity, "description", defEntity.description);
		DatastoreUtils.setUnindexedProperty(datastoreEntity, "png", defEntity.png);

		return true;
	}

	/**
	 * Append DefEntity to the search document
	 * @param builder the document builder
	 * @param defEntity the def entity to append to the document builder
	 */
	private void appendDefEntity(Builder builder, DefEntity defEntity) {
		String nameTokens = SearchUtils.tokenizeAutocomplete(defEntity.name.toLowerCase(), TokenSizes.RESOURCE);
		builder.addField(Field.newBuilder().setName("name").setText(nameTokens).build());
		builder.addField(Field.newBuilder().setName("published").setDate(defEntity.date).build());

		// Add name of creators
		String creatorName = UserRepo.getUsername(KeyFactory.stringToKey(defEntity.creatorKey));
		String creatorNameTokens = SearchUtils.tokenizeAutocomplete(creatorName.toLowerCase(), TokenSizes.RESOURCE);
		builder.addField(Field.newBuilder().setName("creator").setText(creatorNameTokens));
		String originalCreatorName = UserRepo.getUsername(KeyFactory.stringToKey(defEntity.originalCreatorKey));
		String originalCreatorNameTokens = SearchUtils.tokenizeAutocomplete(originalCreatorName.toLowerCase(), TokenSizes.RESOURCE);
		builder.addField(Field.newBuilder().setName("original_creator").setText(originalCreatorNameTokens));
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
	 * Append EnemyDefEntity to the search document
	 * @param builder the document builder
	 * @param enemyDefEntity the enemy def entity to append to the search document
	 */
	private void appendEnemyDefEntity(Builder builder, EnemyDefEntity enemyDefEntity) {
		appendDefEntity(builder, enemyDefEntity);
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
	 * Append LevelDefEntity to the search document
	 * @param builder the document builder
	 * @param levelDefEntity the level def entity to append to the search document
	 */
	private void appendLevelDefEntity(Builder builder, LevelDefEntity levelDefEntity) {
		appendDefEntity(builder, levelDefEntity);
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
	 * Append BulletDefEntity to the search document
	 * @param builder the document builder
	 * @param bulletDefEntity the def entity to append to the search document
	 */
	private void appendBulletDefEntity(Builder builder, BulletDefEntity bulletDefEntity) {
		appendDefEntity(builder, bulletDefEntity);
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

	/**
	 * Append CampaignDefEntity to the search document
	 * @param builder document builder
	 * @param campaignDefEntity the def entity to append to the search document
	 */
	private void appendCampaignDefEntity(Builder builder, CampaignDefEntity campaignDefEntity) {
		appendDefEntity(builder, campaignDefEntity);
	}

	/** Logger */
	private Logger mLogger = Logger.getLogger(Publish.class.getName());
	/** Created search documents */
	private HashMap<String, ArrayList<Document>> mSearchDocumentsToAdd = new HashMap<>();
}
