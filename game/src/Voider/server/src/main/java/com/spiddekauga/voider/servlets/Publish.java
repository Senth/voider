package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Document.Builder;
import com.spiddekauga.appengine.BlobUtils;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.misc.ChatMessage;
import com.spiddekauga.voider.network.misc.ChatMessage.MessageTypes;
import com.spiddekauga.voider.network.resource.BulletDamageSearchRanges;
import com.spiddekauga.voider.network.resource.BulletDefEntity;
import com.spiddekauga.voider.network.resource.BulletSpeedSearchRanges;
import com.spiddekauga.voider.network.resource.CampaignDefEntity;
import com.spiddekauga.voider.network.resource.CollisionDamageSearchRanges;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.network.resource.EnemyDefEntity;
import com.spiddekauga.voider.network.resource.EnemySpeedSearchRanges;
import com.spiddekauga.voider.network.resource.LevelDefEntity;
import com.spiddekauga.voider.network.resource.LevelLengthSearchRanges;
import com.spiddekauga.voider.network.resource.LevelSpeedSearchRanges;
import com.spiddekauga.voider.network.resource.PublishMethod;
import com.spiddekauga.voider.network.resource.PublishResponse;
import com.spiddekauga.voider.network.resource.UploadTypes;
import com.spiddekauga.voider.network.resource.PublishResponse.Statuses;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CDependency;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CLevelStat;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CSyncPublished;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserResources;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserResourcesDeleted;
import com.spiddekauga.voider.server.util.ServerConfig.SearchTables.SDef;
import com.spiddekauga.voider.server.util.ServerConfig.SearchTables.SEnemy;
import com.spiddekauga.voider.server.util.ServerConfig.SearchTables.SLevel;
import com.spiddekauga.voider.server.util.ServerConfig.TokenSizes;
import com.spiddekauga.voider.server.util.UserRepo;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Tries to publish one or more definitions
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Publish extends VoiderServlet {
	@Override
	protected void onInit() {
		mSearchDocumentsToAdd.clear();
		mMethod = null;
		mResponse = new PublishResponse();
		mResponse.status = Statuses.FAILED_SERVER_ERROR;
		mDatastoreKeys.clear();
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			mResponse.status = Statuses.FAILED_USER_NOT_LOGGED_IN;
			return mResponse;
		}

		if (methodEntity instanceof PublishMethod) {
			mMethod = (PublishMethod) methodEntity;
			mBlobKeys = getUploadedBlobs();

			// Check if any of the resources have been
			boolean alreadyPublished = hasBeenPublishedAlready();
			boolean success = !alreadyPublished;

			// Add entities to datastore and search
			if (success) {
				success = addEntitiesToDatastore();
			}

			// Add dependencies
			boolean addedDependencies = false;
			if (success) {
				success = addDependencies();
				addedDependencies = true;
			}

			// Add search documents
			boolean addedSearch = false;
			if (success) {
				success = addSearchDocuments();
				addedSearch = true;
			}


			// SUCCESS -> Send sync messages and removed user resources
			if (success) {
				mResponse.status = Statuses.SUCCESS;
				removeUserResourcesThatWasPublished();
				sendSyncMessages();
			}
			// FAILED - Remove all resources from published, dependencies, and search
			else {
				undoPublishedAndStats();

				if (addedDependencies) {
					undoDependencies();
				}

				if (addedSearch) {
					undoSearchDocuments();
				}
			}
		}

		return mResponse;
	}

	/**
	 * Remove all user resources that was published
	 */
	private void removeUserResourcesThatWasPublished() {
		// Things to remove
		ArrayList<BlobKey> blobsToRemove = new ArrayList<>();
		ArrayList<Key> entitiesToRemove = new ArrayList<>();
		ArrayList<Entity> entitiesToAdd = new ArrayList<>();

		Date date = new Date();

		// Find all revisions of the resources and delete them
		for (DefEntity def : mMethod.defs) {
			UUID removeId = def.resourceId;

			// Revisions to delete
			FilterWrapper idProperty = new FilterWrapper(CUserResources.RESOURCE_ID, removeId);
			List<Key> keys = DatastoreUtils.getKeys(DatastoreTables.USER_RESOURCES, mUser.getKey(), idProperty);
			entitiesToRemove.addAll(keys);

			// Blobs to delete
			for (Key key : keys) {
				Entity entity = DatastoreUtils.getEntity(key);
				BlobKey blobKey = (BlobKey) entity.getProperty(CUserResources.BLOB_KEY);
				blobsToRemove.add(blobKey);
			}

			// Add the resource to the deleted table
			Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.USER_RESOURCES_DELETED, mUser.getKey(), idProperty);
			if (entity == null) {
				entity = new Entity(DatastoreTables.USER_RESOURCES_DELETED, mUser.getKey());
				DatastoreUtils.setProperty(entity, CUserResourcesDeleted.RESOURCE_ID, removeId);
				DatastoreUtils.setProperty(entity, CUserResourcesDeleted.DATE, date);
				entitiesToAdd.add(entity);
			}
		}

		// Flush entities
		DatastoreUtils.put(entitiesToAdd);
		DatastoreUtils.delete(entitiesToRemove);
		BlobUtils.delete(blobsToRemove);
	}

	/**
	 * Undo added published resources and level statistics
	 */
	private void undoPublishedAndStats() {
		ArrayList<Key> keysToDelete = new ArrayList<>();
		for (DefEntity def : mMethod.defs) {
			Key defKey = mDatastoreKeys.get(def.resourceId);

			if (defKey != null) {
				keysToDelete.add(defKey);

				// Remove level statistics
				if (def instanceof LevelDefEntity) {
					Key statKey = DatastoreUtils.getSingleKey(DatastoreTables.LEVEL_STAT, defKey);
					if (statKey != null) {
						keysToDelete.add(statKey);
					}
				}
			}
		}

		DatastoreUtils.delete(keysToDelete);
	}

	/**
	 * Send sync messages
	 */
	private void sendSyncMessages() {
		sendMessage(new ChatMessage<>(MessageTypes.SYNC_USER_RESOURCES));
		sendMessage(new ChatMessage<>(MessageTypes.SYNC_COMMUNITY_DOWNLOAD));
	}

	/**
	 * Check if any of the resources have been published already
	 * @return true if any of the resources have been published already
	 */
	private boolean hasBeenPublishedAlready() {
		boolean alreadyPublished = false;
		for (DefEntity def : mMethod.defs) {
			if (DatastoreUtils.exists(DatastoreTables.PUBLISHED, new FilterWrapper(CPublished.RESOURCE_ID, def.resourceId))) {
				mResponse.alreadyPublished.add(def.resourceId);
				alreadyPublished = true;
			}
		}

		if (alreadyPublished) {
			mResponse.status = Statuses.FAILED_ALREADY_PUBLISHED;
		}

		return alreadyPublished;
	}

	/**
	 * Set sync download time
	 * @param publishKey key of the published resource
	 */
	private void setSyncDownloadDate(Key publishKey) {
		Entity entity = new Entity(DatastoreTables.SYNC_PUBLISHED, mUser.getKey());
		entity.setProperty(CSyncPublished.PUBLISHED_KEY, publishKey);
		entity.setProperty(CSyncPublished.DOWNLOAD_DATE, new Date());

		DatastoreUtils.put(entity);
	}

	/**
	 * Add all dependencies for all resources
	 * @return true if successful
	 */
	private boolean addDependencies() {
		mLogger.fine("Adding resource dependencies");
		for (DefEntity defEntity : mMethod.defs) {
			boolean success = addDependencies(defEntity);

			if (!success) {
				mLogger.severe("Failed to add all dependencies");
				return false;
			}
		}

		return true;
	}

	/**
	 * Add all dependencies for the specified resource
	 * @param defEntity the definition to add dependencies for
	 * @return true if successful
	 */
	private boolean addDependencies(DefEntity defEntity) {
		if (!defEntity.dependencies.isEmpty()) {
			Key datastoreKey = mDatastoreKeys.get(defEntity.resourceId);

			for (UUID dependency : defEntity.dependencies) {
				Key dependencyKey = getResourceKey(dependency, mDatastoreKeys);

				if (dependencyKey != null) {
					Entity entity = new Entity(DatastoreTables.DEPENDENCY, datastoreKey);
					entity.setProperty(CDependency.DEPENDENCY, dependencyKey);
					Key key = DatastoreUtils.put(entity);

					if (key == null) {
						mLogger.severe("Could not add dependency for " + dependency);
						return false;
					}
				} else {
					mLogger.severe("Could not find dependency key for " + dependency);
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Undo added published dependencies
	 */
	private void undoDependencies() {
		for (DefEntity def : mMethod.defs) {
			if (!def.dependencies.isEmpty()) {
				Key defKey = mDatastoreKeys.get(def.resourceId);

				List<Key> dependenciesKeys = DatastoreUtils.getKeys(DatastoreTables.DEPENDENCY, defKey);
				DatastoreUtils.delete(dependenciesKeys);
			}
		}
	}

	/**
	 * Tries to find the resource key, if it doesn't exist in the map it will try to find
	 * it in the Datastore instead and insert it to datastoreKeys
	 * @param resourceId the resource to get the blob key from
	 * @param resourceKeys all current known resource keys
	 * @return Entity key for the resource, null if not found
	 */
	private Key getResourceKey(UUID resourceId, Map<UUID, Key> resourceKeys) {
		Key resourceKey = resourceKeys.get(resourceId);

		// Search in datastore
		if (resourceKey == null) {
			resourceKey = DatastoreUtils.getSingleKey(DatastoreTables.PUBLISHED, new FilterWrapper(CPublished.RESOURCE_ID, resourceId));

			if (resourceKey != null) {
				resourceKeys.put(resourceId, resourceKey);
			}
		}

		return resourceKey;
	}

	/**
	 * Create empty level statistics
	 * @param key datastore key of the level entity to add empty statistics for
	 * @return true if successful, false otherwise
	 */
	private boolean createEmptyLevelStatistics(Key key) {
		Entity entity = new Entity(DatastoreTables.LEVEL_STAT.toString(), key);

		entity.setProperty(CLevelStat.PLAY_COUNT, 0);
		entity.setProperty(CLevelStat.BOOKMARS, 0);
		entity.setProperty(CLevelStat.RATING_SUM, 0);
		entity.setProperty(CLevelStat.RATINGS, 0);
		entity.setProperty(CLevelStat.RATING_AVG, 0.0);
		entity.setProperty(CLevelStat.CLEAR_COUNT, 0);

		Key statKey = DatastoreUtils.put(entity);

		return statKey != null;
	}

	/**
	 * Add all entities to the datastore
	 * @return true if successful
	 */
	private boolean addEntitiesToDatastore() {
		mLogger.fine("Add entities to datastore");

		for (DefEntity defEntity : mMethod.defs) {
			Key datastoreKey = addEntityToDatastore(defEntity);

			if (datastoreKey != null) {
				if (defEntity instanceof LevelDefEntity) {
					boolean success = createEmptyLevelStatistics(datastoreKey);
					if (!success) {
						return false;
					}
				}

				setSyncDownloadDate(datastoreKey);

				mLogger.fine("Create search document");
				createSearchDocument(defEntity, datastoreKey);
			} else {
				mLogger.severe("Failed to add all entities to the datastore, removing all");
				return false;
			}
		}

		return true;
	}

	/**
	 * Add an entity to the datastore
	 * @param defEntity the entity to add to the datastore
	 * @return datastore key of the def entity
	 */
	private Key addEntityToDatastore(DefEntity defEntity) {
		boolean success = false;
		Entity datastoreEntity = new Entity(DatastoreTables.PUBLISHED.toString(), mUser.getKey());

		switch (defEntity.type) {
		case BULLET_DEF:
			success = appendBulletDefEntity(datastoreEntity, (BulletDefEntity) defEntity);
			break;

		case CAMPAIGN_DEF:
			success = appendCampaignDefEntity(datastoreEntity, (CampaignDefEntity) defEntity);
			break;

		case ENEMY_DEF:
			success = appendEnemyDefEntity(datastoreEntity, (EnemyDefEntity) defEntity);
			break;

		case LEVEL_DEF:
			success = appendLevelDefEntity(datastoreEntity, (LevelDefEntity) defEntity);
			break;

		default:
			mLogger.severe("Not implemented def type: " + defEntity.type);
			break;
		}

		if (success) {
			Key key = DatastoreUtils.put(datastoreEntity);

			if (key != null) {
				mDatastoreKeys.put(defEntity.resourceId, key);
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
		mLogger.fine("Adding search documents");

		for (Entry<UploadTypes, ArrayList<Document>> entry : mSearchDocumentsToAdd.entrySet()) {
			String typeName = entry.getKey().toString();
			ArrayList<Document> documents = entry.getValue();

			boolean success = SearchUtils.indexDocuments(typeName, documents);

			if (!success) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Undo added search documents
	 */
	private void undoSearchDocuments() {
		mLogger.fine("Removing search documents");

		for (Entry<UploadTypes, ArrayList<Document>> entry : mSearchDocumentsToAdd.entrySet()) {
			String typeName = entry.getKey().toString();
			ArrayList<Document> documents = entry.getValue();
			SearchUtils.deleteDocuments(typeName, documents);
		}
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
		case BULLET_DEF:
			appendBulletDefEntity(builder, (BulletDefEntity) defEntity);
			break;

		case CAMPAIGN_DEF:
			appendCampaignDefEntity(builder, (CampaignDefEntity) defEntity);
			break;

		case ENEMY_DEF:
			appendEnemyDefEntity(builder, (EnemyDefEntity) defEntity);
			break;

		case LEVEL_DEF:
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
			mSearchDocumentsToAdd.put(defEntity.type, documentList);
		}

		documentList.add(builder.build());
	}

	/**
	 * Append DefEntity to the datastore entity
	 * @param datastoreEntity datastore entity
	 * @param defEntity the def entity to append to the datastore
	 * @return true if successful, false otherwise
	 */
	private boolean appendDefEntity(Entity datastoreEntity, DefEntity defEntity) {
		// Type
		if (defEntity.type != null) {
			DatastoreUtils.setProperty(datastoreEntity, CPublished.TYPE, defEntity.type.toId());
		} else {
			mLogger.severe("DefType is null for " + defEntity.resourceId);
			return false;
		}

		// Blob key
		BlobKey blobKey = mBlobKeys.get(defEntity.resourceId);
		if (blobKey != null) {
			DatastoreUtils.setProperty(datastoreEntity, CPublished.BLOB_KEY, blobKey);
		} else {
			mLogger.severe("Could not find blob key for " + defEntity.resourceId);
			return false;
		}

		// Original creator key
		try {
			DatastoreUtils.setProperty(datastoreEntity, CPublished.ORIGINAL_CREATOR_KEY, KeyFactory.stringToKey(defEntity.originalCreatorKey));
		} catch (IllegalArgumentException e) {
			return false;
		}


		// No-test properties
		DatastoreUtils.setProperty(datastoreEntity, CPublished.DATE, defEntity.date);
		DatastoreUtils.setProperty(datastoreEntity, CPublished.COPY_PARENT_ID, defEntity.copyParentId);
		DatastoreUtils.setProperty(datastoreEntity, CPublished.RESOURCE_ID, defEntity.resourceId);
		DatastoreUtils.setUnindexedProperty(datastoreEntity, CPublished.NAME, defEntity.name);
		DatastoreUtils.setUnindexedProperty(datastoreEntity, CPublished.DESCRIPTION, defEntity.description);
		DatastoreUtils.setUnindexedProperty(datastoreEntity, CPublished.PNG, defEntity.png);

		return true;
	}

	/**
	 * Append DefEntity to the search document
	 * @param builder the document builder
	 * @param defEntity the def entity to append to the document builder
	 */
	private void appendDefEntity(Builder builder, DefEntity defEntity) {
		SearchUtils.addField(builder, SDef.NAME, defEntity.name.toLowerCase(), TokenSizes.RESOURCE);
		SearchUtils.addField(builder, SDef.DATE, defEntity.date);

		// Add name of creators
		String creatorName = UserRepo.getUsername(KeyFactory.stringToKey(defEntity.revisedByKey));
		SearchUtils.addField(builder, SDef.CREATOR, creatorName, TokenSizes.RESOURCE);
		String originalCreatorName = UserRepo.getUsername(KeyFactory.stringToKey(defEntity.originalCreatorKey));
		SearchUtils.addField(builder, SDef.ORIGINAL_CREATOR, originalCreatorName, TokenSizes.RESOURCE);
	}

	/**
	 * Append EnemyDefEntity to the datastore entity
	 * @param datastoreEntity datastore entity
	 * @param enemyDefEntity the enemy def entity to append to the datastore
	 * @return true if successful, false otherwise
	 */
	private boolean appendEnemyDefEntity(Entity datastoreEntity, EnemyDefEntity enemyDefEntity) {
		return appendDefEntity(datastoreEntity, enemyDefEntity);
	}

	/**
	 * Append EnemyDefEntity to the search document
	 * @param builder the document builder
	 * @param enemyDefEntity the enemy def entity to append to the search document
	 */
	private void appendEnemyDefEntity(Builder builder, EnemyDefEntity enemyDefEntity) {
		appendDefEntity(builder, enemyDefEntity);

		// Movement
		SearchUtils.addFieldAtom(builder, SEnemy.MOVEMENT_TYPE, enemyDefEntity.movementType.toSearchId());
		if (enemyDefEntity.movementType != MovementTypes.STATIONARY) {
			SearchUtils.addField(builder, SEnemy.MOVEMENT_SPEED, enemyDefEntity.movementSpeed);
			EnemySpeedSearchRanges enemySpeedCat = EnemySpeedSearchRanges.getRange(enemyDefEntity.movementSpeed);
			if (enemySpeedCat != null) {
				SearchUtils.addFieldAtom(builder, SEnemy.MOVEMENT_SPEED_CAT, enemySpeedCat.toSearchId());
			} else {
				mLogger.severe("Enemy movement speed (" + enemyDefEntity.movementSpeed + ") is not in a valid search range");
			}
		}

		// Weapon
		SearchUtils.addField(builder, SEnemy.HAS_WEAPON, enemyDefEntity.hasWeapon);
		if (enemyDefEntity.hasWeapon) {
			SearchUtils.addField(builder, SEnemy.BULLET_SPEED, enemyDefEntity.bulletSpeed);
			SearchUtils.addField(builder, SEnemy.BULLET_DAMAGE, enemyDefEntity.bulletDamage);
			SearchUtils.addFieldAtom(builder, SEnemy.AIM_TYPE, enemyDefEntity.aimType.toSearchId());

			BulletSpeedSearchRanges bulletSpeedCat = BulletSpeedSearchRanges.getRange(enemyDefEntity.bulletSpeed);
			if (bulletSpeedCat != null) {
				SearchUtils.addFieldAtom(builder, SEnemy.BULLET_SPEED_CAT, bulletSpeedCat.toSearchId());
			} else {
				mLogger.severe("Enemy bullet speed (" + enemyDefEntity.bulletSpeed + ") is not in a valid range");
			}

			BulletDamageSearchRanges bulletDamageCat = BulletDamageSearchRanges.getRange(enemyDefEntity.bulletDamage);
			if (bulletDamageCat != null) {
				SearchUtils.addFieldAtom(builder, SEnemy.BULLET_DAMAGE_CAT, bulletDamageCat.toSearchId());
			} else {
				mLogger.severe("Enemy bullet damage (" + enemyDefEntity.bulletDamage + ") is not in a valid range");
			}
		}

		// Collision
		SearchUtils.addField(builder, SEnemy.DESTROY_ON_COLLIDE, enemyDefEntity.destroyOnCollide);
		SearchUtils.addField(builder, SEnemy.COLLISION_DAMAGE, enemyDefEntity.collisionDamage);;

		CollisionDamageSearchRanges collisionDamageCat = CollisionDamageSearchRanges.getRange(enemyDefEntity.collisionDamage);
		if (collisionDamageCat != null) {
			SearchUtils.addFieldAtom(builder, SEnemy.COLLISION_DAMAGE_CAT, collisionDamageCat.toSearchId());
		}

	}

	/**
	 * Append LevelDefEntity to the datastore entity
	 * @param datastoreEntity datastore entity
	 * @param LevelDefEntity the level def entity to append to the datastore
	 * @return true if successful, false otherwise
	 */
	private boolean appendLevelDefEntity(Entity datastoreEntity, LevelDefEntity LevelDefEntity) {
		// Get blob key for level
		BlobKey blobKey = mBlobKeys.get(LevelDefEntity.levelId);
		if (blobKey != null) {
			DatastoreUtils.setUnindexedProperty(datastoreEntity, CPublished.LEVEL_BLOB_KEY, blobKey);
		} else {
			mLogger.severe("Could not find blob key for level: " + LevelDefEntity.levelId);
			return false;
		}


		// No-test properties
		DatastoreUtils.setUnindexedProperty(datastoreEntity, CPublished.LEVEL_ID, LevelDefEntity.levelId);

		return appendDefEntity(datastoreEntity, LevelDefEntity);
	}

	/**
	 * Append LevelDefEntity to the search document
	 * @param builder the document builder
	 * @param levelDefEntity the level def entity to append to the search document
	 */
	private void appendLevelDefEntity(Builder builder, LevelDefEntity levelDefEntity) {
		appendDefEntity(builder, levelDefEntity);

		SearchUtils.addField(builder, SLevel.LEVEL_LENGTH, levelDefEntity.levelLength);
		SearchUtils.addField(builder, SLevel.LEVEL_SPEED, levelDefEntity.levelSpeed);

		LevelLengthSearchRanges lengthCat = LevelLengthSearchRanges.getRange(levelDefEntity.levelLength);
		if (lengthCat != null) {
			SearchUtils.addFieldAtom(builder, SLevel.LEVEL_LENGTH_CAT, lengthCat.toSearchId());
		} else {
			mLogger.severe("Level length (" + levelDefEntity.levelLength + ") isn't in a valid range");
		}

		LevelSpeedSearchRanges speedCat = LevelSpeedSearchRanges.getRange(levelDefEntity.levelSpeed);
		if (speedCat != null) {
			SearchUtils.addFieldAtom(builder, SLevel.LEVEL_SPEED_CAT, speedCat.toSearchId());
		} else {
			mLogger.severe("Level speed (" + levelDefEntity.levelSpeed + ") isn't in a valid range");
		}
	}

	/**
	 * Append BulletDefEntity to the datastore entity
	 * @param datastoreEntity datastore entity
	 * @param bulletDefEntity the def entity to append to the datastore
	 * @return true if successful, false otherwise
	 */
	private boolean appendBulletDefEntity(Entity datastoreEntity, BulletDefEntity bulletDefEntity) {
		return appendDefEntity(datastoreEntity, bulletDefEntity);
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
	 * @return true if successful, false otherwise
	 */
	private boolean appendCampaignDefEntity(Entity datastoreEntity, CampaignDefEntity campaignDefEntity) {
		return appendDefEntity(datastoreEntity, campaignDefEntity);
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
	private HashMap<UploadTypes, ArrayList<Document>> mSearchDocumentsToAdd = new HashMap<>();
	private Map<UUID, BlobKey> mBlobKeys = null;
	/** For getting datastore keys faster when adding dependencies */
	private Map<UUID, Key> mDatastoreKeys = new HashMap<>();
	private PublishMethod mMethod = null;
	private PublishResponse mResponse = null;
}
