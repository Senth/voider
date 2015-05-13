package com.spiddekauga.voider.servlets.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.spiddekauga.appengine.BlobUtils;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.misc.ChatMessage;
import com.spiddekauga.voider.network.misc.ChatMessage.MessageTypes;
import com.spiddekauga.voider.network.resource.ResourceConflictEntity;
import com.spiddekauga.voider.network.resource.ResourceRevisionBlobEntity;
import com.spiddekauga.voider.network.resource.ResourceRevisionEntity;
import com.spiddekauga.voider.network.resource.RevisionEntity;
import com.spiddekauga.voider.network.resource.UploadTypes;
import com.spiddekauga.voider.network.resource.UserResourceSyncMethod;
import com.spiddekauga.voider.network.resource.UserResourceSyncResponse;
import com.spiddekauga.voider.network.resource.UserResourceSyncResponse.UploadStatuses;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserResources;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserResourcesDeleted;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Synchronizes user resource revisions
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class UserResourceSync extends VoiderServlet {

	/**
	 * Initializes the sync
	 */
	@Override
	protected void onInit() {
		mResponse = new UserResourceSyncResponse();
		mResponse.uploadStatus = UploadStatuses.FAILED_INTERNAL;
		mSyncDate = new Date();
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			mResponse.uploadStatus = UploadStatuses.FAILED_USER_NOT_LOGGED_IN;
			return mResponse;
		}

		if (methodEntity instanceof UserResourceSyncMethod) {
			UserResourceSyncMethod userMethod = (UserResourceSyncMethod) methodEntity;

			syncDeletedToClient(userMethod);
			syncDeletedToServer(userMethod);

			// Conflicts
			checkForConflicts(userMethod);

			if (userMethod.keepLocalConflicts()) {
				fixConflictsKeepClient(userMethod);
			} else if (userMethod.keepServerConflicts()) {
				fixConflictsKeepServer(userMethod);
			}

			syncNewToClient(userMethod);
			syncNewToServer(userMethod);
			mResponse.syncTime = mSyncDate;

			// Send sync message
			if (mResponse.isSuccessful()) {
				if (!userMethod.resources.isEmpty() || !userMethod.resourceToRemove.isEmpty()
						|| (userMethod.conflictKeepLocal != null && userMethod.conflictKeepLocal)) {
					ChatMessage<Object> chatMessage = new ChatMessage<>(MessageTypes.SYNC_USER_RESOURCES, mUser.getClientId());
					sendMessage(chatMessage);
				}
			}
		}

		return mResponse;
	}

	private void fixConflictsKeepClient(UserResourceSyncMethod method) {
		ArrayList<BlobKey> blobsToRemove = new ArrayList<>();
		ArrayList<Key> entitiesToRemove = new ArrayList<>();

		// Get revisions to remove from server
		for (Entry<UUID, ResourceConflictEntity> entry : method.conflictsToFix.entrySet()) {
			ResourceConflictEntity conflictEntity = entry.getValue();

			// Get from datastore
			FilterWrapper resourceFilter = new FilterWrapper(CUserResources.RESOURCE_ID, conflictEntity.resourceId);
			FilterWrapper revisionFilter = new FilterWrapper(CUserResources.REVISION, FilterOperator.GREATER_THAN_OR_EQUAL,
					conflictEntity.fromRevision);

			Iterable<Entity> entities = DatastoreUtils.getEntities(DatastoreTables.USER_RESOURCES, mUser.getKey(), resourceFilter, revisionFilter);

			// Add blobs to remove for these conflicts
			for (Entity entity : entities) {
				BlobKey blobKey = (BlobKey) entity.getProperty(CUserResources.BLOB_KEY);
				blobsToRemove.add(blobKey);
				entitiesToRemove.add(entity.getKey());
			}
		}


		// Remove blobs and datastore entries
		DatastoreUtils.delete(entitiesToRemove);
		BlobUtils.delete(blobsToRemove);


		// Resources are added through syncNewToServer()
	}

	private void fixConflictsKeepServer(UserResourceSyncMethod method) {
		for (Entry<UUID, ResourceConflictEntity> entry : method.conflictsToFix.entrySet()) {
			ResourceConflictEntity conflictEntity = entry.getValue();

			if (!mResponse.resourcesToRemove.contains(conflictEntity.resourceId)) {
				ArrayList<ResourceRevisionBlobEntity> revisions = new ArrayList<>();
				mResponse.blobsToDownload.put(conflictEntity.resourceId, revisions);

				Query query = new Query(DatastoreTables.USER_RESOURCES, mUser.getKey());

				// All revision from the conflict
				FilterWrapper resourceFilter = new FilterWrapper(CUserResources.RESOURCE_ID, conflictEntity.resourceId);
				FilterWrapper revisionFilter = new FilterWrapper(CUserResources.REVISION, FilterOperator.GREATER_THAN_OR_EQUAL,
						conflictEntity.fromRevision);

				query.setFilter(DatastoreUtils.createCompositeFilter(CompositeFilterOperator.AND, resourceFilter, revisionFilter));

				query.addSort(CUserResources.REVISION);

				PreparedQuery preparedQuery = DatastoreUtils.prepare(query);

				for (Entity entity : preparedQuery.asIterable()) {
					ResourceRevisionBlobEntity blobEntity = new ResourceRevisionBlobEntity();
					blobEntity.resourceId = DatastoreUtils.getPropertyUuid(entity, CUserResources.RESOURCE_ID);
					blobEntity.revision = DatastoreUtils.getPropertyInt(entity, CUserResources.REVISION, 0);
					blobEntity.uploadType = DatastoreUtils.getPropertyIdStore(entity, CUserResources.TYPE, UploadTypes.class);
					blobEntity.blobKey = ((BlobKey) entity.getProperty(CUserResources.BLOB_KEY)).getKeyString();
					blobEntity.created = (Date) entity.getProperty(CUserResources.CREATED);

					revisions.add(blobEntity);
				}
			}
		}
	}

	/**
	 * Check for conflicts
	 * @param methodEntity parameters sent to the server
	 */
	private void checkForConflicts(UserResourceSyncMethod methodEntity) {
		// Where to add conflicts
		HashMap<UUID, ResourceConflictEntity> newConflicts;
		if (methodEntity.conflictKeepLocal != null && methodEntity.conflictsToFix != null) {
			newConflicts = methodEntity.conflictsToFix;
		} else {
			newConflicts = mResponse.conflicts;
		}


		// Iterate through each resource id
		for (ResourceRevisionEntity entity : methodEntity.resources) {

			// Skip resources that we know are in conflict
			if (methodEntity.conflictsToFix != null && methodEntity.conflictsToFix.containsKey(entity.resourceId)) {
				continue;
			}

			FilterWrapper resourceProp = new FilterWrapper(CUserResources.RESOURCE_ID, entity.resourceId);

			if (!entity.revisions.isEmpty()) {
				RevisionEntity revisionEntity = entity.revisions.get(0);
				FilterWrapper revisionProp = new FilterWrapper(CUserResources.REVISION, revisionEntity.revision);

				// Check for conflicts
				if (DatastoreUtils.exists(DatastoreTables.USER_RESOURCES, resourceProp, revisionProp)) {
					// From what revision is the conflict?
					ResourceConflictEntity conflict = new ResourceConflictEntity();
					conflict.resourceId = entity.resourceId;
					conflict.fromRevision = revisionEntity.revision;

					newConflicts.put(conflict.resourceId, conflict);
				}
			}
		}
	}

	/**
	 * Delete the resources sent from the server
	 * @param methodEntity parameters sent to the server
	 */
	private void syncDeletedToServer(UserResourceSyncMethod methodEntity) {
		// Things to remove
		ArrayList<BlobKey> blobsToRemove = new ArrayList<>();
		ArrayList<Key> entitiesToRemove = new ArrayList<>();
		ArrayList<Entity> entitiesToAdd = new ArrayList<>();


		// Find all revisions of the resources and delete them
		for (UUID removeId : methodEntity.resourceToRemove) {
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
				DatastoreUtils.setProperty(entity, CUserResourcesDeleted.DATE, mResponse.syncTime);
				entitiesToAdd.add(entity);
			}
		}

		// Flush entities
		DatastoreUtils.put(entitiesToAdd);
		DatastoreUtils.delete(entitiesToRemove);
		BlobUtils.delete(blobsToRemove);
	}

	/**
	 * Synchronize the upload
	 * @param methodEntity parameters sent to the server
	 */
	private void syncNewToServer(UserResourceSyncMethod methodEntity) {
		Map<UUID, Map<Integer, BlobKey>> blobResources = getUploadedRevisionBlobs();

		ArrayList<BlobKey> blobsToRemove = new ArrayList<>();

		// Iterate through each resource id
		for (ResourceRevisionEntity entity : methodEntity.resources) {
			Map<Integer, BlobKey> blobKeys = blobResources.get(entity.resourceId);
			if (blobKeys == null) {
				mLogger.severe("Couldn't find any blobs for resource: " + entity.resourceId);
				addFailedUpload(entity.resourceId, -1);
				continue;
			}


			ArrayList<Key> revisions = new ArrayList<>();
			// Add revision with blob key to datastore
			if (!mResponse.conflicts.containsKey(entity.resourceId)) {
				for (RevisionEntity revisionEntity : entity.revisions) {
					Key addedRevision = insertUserResourceRevision(entity, revisionEntity, blobKeys);
					if (addedRevision != null) {
						revisions.add(addedRevision);
					} else {
						addFailedUpload(entity.resourceId, revisionEntity.revision);
					}
				}
			}
			// Remove uploaded blob keys
			else {
				blobsToRemove.addAll(blobKeys.values());
			}
		}

		if (!blobsToRemove.isEmpty()) {
			BlobUtils.delete(blobsToRemove);
		}

		if (mResponse.conflicts.isEmpty() && mResponse.failedUploads.isEmpty()) {
			mResponse.uploadStatus = UploadStatuses.SUCCESS_ALL;
		} else if (!mResponse.conflicts.isEmpty()) {
			mResponse.uploadStatus = UploadStatuses.SUCCESS_CONFLICTS;
		} else if (!mResponse.failedUploads.isEmpty()) {
			mResponse.uploadStatus = UploadStatuses.SUCCESS_PARTIAL;
		}
	}

	/**
	 * Add a failed upload
	 * @param resourceId
	 * @param revision the revision. If set to -1 the revision isn't used
	 */
	private void addFailedUpload(UUID resourceId, int revision) {
		HashSet<Integer> revisions = mResponse.failedUploads.get(resourceId);

		if (revisions == null) {
			// New revisions
			if (revision != -1) {
				revisions = new HashSet<>();
				mResponse.failedUploads.put(resourceId, revisions);
			}
			// All failed
			else {
				mResponse.failedUploads.put(resourceId, null);
			}
		}

		if (revision != -1) {
			revisions.add(revision);
		}
	}

	/**
	 * Send deleted resources to the client
	 * @param methodEntity parameters sent to the server
	 */
	private void syncDeletedToClient(UserResourceSyncMethod methodEntity) {
		// Get all resources that were deleted after sync
		Query query = new Query(DatastoreTables.USER_RESOURCES_DELETED, mUser.getKey());

		// Only older than latest sync
		Filter filter = new Query.FilterPredicate(CUserResourcesDeleted.DATE, FilterOperator.GREATER_THAN, methodEntity.lastSync);
		query.setFilter(filter);

		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);

		for (Entity entity : preparedQuery.asIterable()) {
			UUID resourceId = DatastoreUtils.getPropertyUuid(entity, CUserResourcesDeleted.RESOURCE_ID);

			mResponse.resourcesToRemove.add(resourceId);
		}
	}

	/**
	 * Add resources that should be downloaded
	 * @param methodEntity parameters sent to the server
	 */
	private void syncNewToClient(UserResourceSyncMethod methodEntity) {
		// Get all resources that were uploaded after latest sync
		Query query = new Query(DatastoreTables.USER_RESOURCES, mUser.getKey());

		// Only older than latest sync
		Filter filter = new Query.FilterPredicate(CUserResources.UPLOADED, FilterOperator.GREATER_THAN, methodEntity.lastSync);
		query.setFilter(filter);

		query.addSort(CUserResources.UPLOADED);
		query.addSort(CUserResources.REVISION);

		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);

		for (Entity entity : preparedQuery.asIterable()) {
			UUID resourceId = DatastoreUtils.getPropertyUuid(entity, CUserResources.RESOURCE_ID);

			// Only add if resource isn't in conflict
			if (!mResponse.conflicts.containsKey(resourceId) && !mResponse.resourcesToRemove.contains(resourceId)
					&& (methodEntity.conflictsToFix == null || !methodEntity.conflictsToFix.containsKey(resourceId))) {
				mLogger.fine("Found sync resource: " + resourceId);

				ResourceRevisionBlobEntity blobEntity = new ResourceRevisionBlobEntity();
				blobEntity.resourceId = resourceId;
				blobEntity.revision = DatastoreUtils.getPropertyInt(entity, CUserResources.REVISION, 0);
				blobEntity.uploadType = DatastoreUtils.getPropertyIdStore(entity, CUserResources.TYPE, UploadTypes.class);
				blobEntity.blobKey = ((BlobKey) entity.getProperty(CUserResources.BLOB_KEY)).getKeyString();
				blobEntity.created = (Date) entity.getProperty(CUserResources.CREATED);

				ArrayList<ResourceRevisionBlobEntity> revisions = mResponse.blobsToDownload.get(resourceId);
				if (revisions == null) {
					revisions = new ArrayList<>();
					mResponse.blobsToDownload.put(resourceId, revisions);
				}
				revisions.add(blobEntity);
			}
		}
	}

	/**
	 * Creates a user resource revision Entity
	 * @param resourceRevisionEntity
	 * @param revisionEntity
	 * @param blobKeys all revision blobs
	 * @return datastore key for the user resource revision
	 */
	private Key insertUserResourceRevision(ResourceRevisionEntity resourceRevisionEntity, RevisionEntity revisionEntity,
			Map<Integer, BlobKey> blobKeys) {
		if (blobKeys != null) {
			Entity entity = new Entity(DatastoreTables.USER_RESOURCES, mUser.getKey());
			DatastoreUtils.setProperty(entity, CUserResources.RESOURCE_ID, resourceRevisionEntity.resourceId);
			entity.setProperty(CUserResources.REVISION, revisionEntity.revision);
			entity.setUnindexedProperty(CUserResources.TYPE, resourceRevisionEntity.type.toId());
			entity.setProperty(CUserResources.CREATED, revisionEntity.date);
			entity.setProperty(CUserResources.UPLOADED, mSyncDate);
			entity.setUnindexedProperty(CUserResources.BLOB_KEY, blobKeys.get(revisionEntity.revision));

			return DatastoreUtils.put(entity);
		} else {
			mLogger.severe("Could not find blob for resource: " + resourceRevisionEntity.resourceId + ", rev: " + revisionEntity.revision);
			return null;
		}
	}

	/** Sync date */
	private Date mSyncDate = null;
	/** Response */
	private UserResourceSyncResponse mResponse = null;
}
