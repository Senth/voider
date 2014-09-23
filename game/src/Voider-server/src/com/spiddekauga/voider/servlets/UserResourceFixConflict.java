package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.spiddekauga.appengine.BlobUtils;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.ResourceConflictEntity;
import com.spiddekauga.voider.network.entities.resource.ResourceRevisionBlobEntity;
import com.spiddekauga.voider.network.entities.resource.UploadTypes;
import com.spiddekauga.voider.network.entities.resource.UserResourceFixConflictMethod;
import com.spiddekauga.voider.network.entities.resource.UserResourceFixConflictMethodResponse;
import com.spiddekauga.voider.network.entities.resource.UserResourceFixConflictMethodResponse.Statuses;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserResources;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserResourcesDeleted;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * For fixing conflicts (and syncing to client again)
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class UserResourceFixConflict extends VoiderServlet {
	@Override
	protected void onInit() {
		mResponse = new UserResourceFixConflictMethodResponse();
		mResponse.status = Statuses.FAILED_INTERNAL;
		mResponse.syncTime = new Date();
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			mResponse.status = Statuses.FAILED_USER_NOT_LOGGED_IN;
			return mResponse;
		}

		if (methodEntity instanceof UserResourceFixConflictMethod) {
			syncDeletedToClient((UserResourceFixConflictMethod) methodEntity);
			fixConflicts((UserResourceFixConflictMethod) methodEntity);
			syncNewToClient((UserResourceFixConflictMethod) methodEntity);
		}


		return mResponse;
	}

	private void fixConflicts(UserResourceFixConflictMethod method) {
		if (method.keepClient) {
			fixConflictsKeepClient(method);
		} else {
			fixConflictsKeepServer(method);
		}
	}

	private void fixConflictsKeepClient(UserResourceFixConflictMethod method) {
		ArrayList<BlobKey> blobsToRemove = new ArrayList<>();
		ArrayList<Key> entitiesToRemove = new ArrayList<>();

		// Get revisions to remove from server
		for (Entry<UUID, ResourceConflictEntity> entry : method.conflicts.entrySet()) {
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


		Map<UUID, Map<Integer, BlobKey>> blobResources = getUploadedRevisionBlobs();

		// Add new resources that was uploaded from the client
		for (Entry<UUID, ResourceConflictEntity> entry : method.conflicts.entrySet()) {
			ResourceConflictEntity conflictEntity = entry.getValue();
			Map<Integer, BlobKey> blobKeys = blobResources.get(conflictEntity.resourceId);


		}
	}

	private void fixConflictsKeepServer(UserResourceFixConflictMethod method) {
		for (Entry<UUID, ResourceConflictEntity> entry : method.conflicts.entrySet()) {
			ResourceConflictEntity conflictEntity = entry.getValue();

			if (!mResponse.resourcesToRemove.contains(conflictEntity.resourceId)) {
				// All revision from the conflict
				FilterWrapper resourceFilter = new FilterWrapper(CUserResources.RESOURCE_ID, conflictEntity.resourceId);
				FilterWrapper revisionFilter = new FilterWrapper(CUserResources.REVISION, FilterOperator.GREATER_THAN_OR_EQUAL,
						conflictEntity.fromRevision);

				Iterable<Entity> entities = DatastoreUtils
						.getEntities(DatastoreTables.USER_RESOURCES, mUser.getKey(), resourceFilter, revisionFilter);

				ArrayList<ResourceRevisionBlobEntity> revisions = new ArrayList<>();
				mResponse.blobsToDownload.put(conflictEntity.resourceId, revisions);

				for (Entity entity : entities) {
					ResourceRevisionBlobEntity blobEntity = new ResourceRevisionBlobEntity();
					blobEntity.resourceId = DatastoreUtils.getUuidProperty(entity, CUserResources.RESOURCE_ID);
					blobEntity.revision = DatastoreUtils.getIntProperty(entity, CUserResources.REVISION);
					blobEntity.uploadType = UploadTypes.fromId(DatastoreUtils.getIntProperty(entity, CUserResources.TYPE));
					blobEntity.blobKey = ((BlobKey) entity.getProperty(CUserResources.BLOB_KEY)).getKeyString();
					blobEntity.created = (Date) entity.getProperty(CUserResources.CREATED);

					revisions.add(blobEntity);
				}
			}
		}
	}

	/**
	 * Send deleted resources to the client
	 * @param method parameters sent to the server
	 */
	private void syncDeletedToClient(UserResourceFixConflictMethod method) {
		// Get all resources that were deleted after sync
		Query query = new Query(DatastoreTables.USER_RESOURCES_DELETED, mUser.getKey());

		// Only older than latest sync
		Filter filter = new Query.FilterPredicate(CUserResourcesDeleted.DATE, FilterOperator.GREATER_THAN, method.lastSync);
		query.setFilter(filter);

		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);

		for (Entity entity : preparedQuery.asIterable()) {
			UUID resourceId = DatastoreUtils.getUuidProperty(entity, CUserResourcesDeleted.RESOURCE_ID);

			mResponse.resourcesToRemove.add(resourceId);
		}
	}

	/**
	 * Add resources that should be downloaded
	 * @param method parameters sent to the server
	 */
	private void syncNewToClient(UserResourceFixConflictMethod method) {
		// Get all resources that were uploaded after latest sync
		Query query = new Query(DatastoreTables.USER_RESOURCES, mUser.getKey());

		// Only older than latest sync
		Filter filter = new Query.FilterPredicate(CUserResources.UPLOADED, FilterOperator.GREATER_THAN, method.lastSync);
		query.setFilter(filter);

		query.addSort(CUserResources.UPLOADED);
		query.addSort(CUserResources.REVISION);

		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);

		for (Entity entity : preparedQuery.asIterable()) {
			UUID resourceId = DatastoreUtils.getUuidProperty(entity, CUserResources.RESOURCE_ID);

			// Only add if resource isn't in conflict (we have added it already then)
			if (!method.conflicts.containsKey(resourceId) && !mResponse.resourcesToRemove.contains(resourceId)) {
				mLogger.fine("Found sync resource: " + resourceId);

				ResourceRevisionBlobEntity blobEntity = new ResourceRevisionBlobEntity();
				blobEntity.resourceId = resourceId;
				blobEntity.revision = DatastoreUtils.getIntProperty(entity, CUserResources.REVISION);
				blobEntity.uploadType = UploadTypes.fromId(DatastoreUtils.getIntProperty(entity, CUserResources.TYPE));
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

	private UserResourceFixConflictMethodResponse mResponse = null;
}
