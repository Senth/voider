package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.PropertyWrapper;
import com.spiddekauga.voider.network.entities.ChatMessage;
import com.spiddekauga.voider.network.entities.ChatMessage.MessageTypes;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ResourceBlobEntity;
import com.spiddekauga.voider.network.entities.ResourceConflictEntity;
import com.spiddekauga.voider.network.entities.ResourceRevisionBlobEntity;
import com.spiddekauga.voider.network.entities.ResourceRevisionEntity;
import com.spiddekauga.voider.network.entities.RevisionEntity;
import com.spiddekauga.voider.network.entities.UploadTypes;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.SyncUserResourcesMethod;
import com.spiddekauga.voider.network.entities.method.SyncUserResourcesMethodResponse;
import com.spiddekauga.voider.network.entities.method.SyncUserResourcesMethodResponse.UploadStatuses;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Synchronizes user resource revisions
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class SyncUserResources extends VoiderServlet {

	@Override
	public void init() {
		mResponse = new SyncUserResourcesMethodResponse();
		mResponse.uploadStatus = UploadStatuses.FAILED_INTERNAL;
		mSyncDate = new Date();
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			mResponse.uploadStatus = UploadStatuses.FAILED_USER_NOT_LOGGED_IN;
			return mResponse;
		}

		if (methodEntity instanceof SyncUserResourcesMethod) {
			checkForConflicts((SyncUserResourcesMethod) methodEntity);
			syncToClient((SyncUserResourcesMethod) methodEntity);
			syncToServer((SyncUserResourcesMethod) methodEntity);
			mResponse.syncTime = mSyncDate;
		}

		return mResponse;
	}

	/**
	 * Check for conflicts
	 * @param methodEntity parameters sent to the server
	 */
	private void checkForConflicts(SyncUserResourcesMethod methodEntity) {
		// Iterate through each resource id
		for (ResourceRevisionEntity entity : methodEntity.resources) {
			PropertyWrapper resourceProp = new PropertyWrapper("resource_id", entity.resourceId);

			// Add uploaded blob revisions to the datastore
			if (entity.revisions.isEmpty()) {
				RevisionEntity revisionEntity = entity.revisions.get(0);
				PropertyWrapper revisionProp = new PropertyWrapper("revision", revisionEntity.revision);

				// Check for conflicts
				if (DatastoreUtils.exists("user_resources", mUser.getKey(), resourceProp, revisionProp)) {
					// From what revision is the conflict?
					ResourceConflictEntity conflict = new ResourceConflictEntity();
					conflict.resourceId = entity.resourceId;
					conflict.fromRevision = revisionEntity.revision;

					// When was that?
					Entity datastoreEntity = DatastoreUtils.getSingleEntity("user_resources", mUser.getKey(), resourceProp, revisionProp);
					conflict.fromDate = (Date) datastoreEntity.getProperty("created");
					conflict.latestDate = getLatestRevisionDate(entity.resourceId);

					mResponse.conflicts.put(conflict.resourceId, conflict);
				}
			}
		}
	}

	/**
	 * Synchronize the upload
	 * @param methodEntity parameters sent to the server
	 */
	private void syncToServer(SyncUserResourcesMethod methodEntity) {
		Map<UUID, Map<Integer, BlobKey>> blobResources = getUploadedRevisionBlobs();

		boolean uploadedSomething = false;

		// Iterate through each resource id
		for (ResourceRevisionEntity entity : methodEntity.resources) {
			Map<Integer, BlobKey> blobKeys = blobResources.get(entity.resourceId);
			ArrayList<Key> revisions = new ArrayList<>();

			// Add uploaded blob revisions to the datastore
			for (RevisionEntity revisionEntity : entity.revisions) {
				if (!mResponse.conflicts.containsKey(entity.resourceId)) {
					revisions.add(insertUserResourceRevision(entity, revisionEntity, blobKeys));
					uploadedSomething = true;
				} else {
					break;
				}
			}
		}

		if (mResponse.conflicts.isEmpty()) {
			mResponse.uploadStatus = UploadStatuses.SUCCESS_ALL;
		} else {
			mResponse.uploadStatus = UploadStatuses.SUCCESS_PARTIAL;
		}


		// Send sync message
		if (uploadedSomething) {
			ChatMessage<Object> chatMessage = new ChatMessage<>();
			chatMessage.skipClient = mUser.getClientId();
			chatMessage.type = MessageTypes.SYNC_USER_RESOURCES;
			sendMessage(chatMessage);
		}
	}

	/**
	 * Add resources that should be downloaded
	 * @param methodEntity parameters sent to the server
	 */
	private void syncToClient(SyncUserResourcesMethod methodEntity) {
		// Get all resources that were uploaded after latest sync
		Query query = new Query("user_resources", mUser.getKey());

		// Only older than latest sync
		Filter filter = new Query.FilterPredicate("uploaded", FilterOperator.GREATER_THAN, methodEntity.lastSync);
		query.setFilter(filter);

		// Only retrieve necessary elements
		DatastoreUtils.createUuidProjection(query, "resource_id");
		query.addProjection(new PropertyProjection("revision", Long.class));
		query.addProjection(new PropertyProjection("type", Long.class));
		query.addProjection(new PropertyProjection("blob_key", BlobKey.class));
		query.addProjection(new PropertyProjection("created", Date.class));

		query.addSort("uploaded");
		query.addSort("resource_id");
		query.addSort("revision");

		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);
		Iterable<Entity> entities = preparedQuery.asIterable();

		for (Entity entity : entities) {
			UUID resourceId = DatastoreUtils.getUuidProperty(entity, "resource_id");

			// Only add if resource isn't in conflict
			if (!mResponse.conflicts.containsKey(resourceId)) {
				ResourceRevisionBlobEntity blobEntity = new ResourceRevisionBlobEntity();
				blobEntity.resourceId = resourceId;
				blobEntity.revision = DatastoreUtils.getIntProperty(entity, "revision");
				blobEntity.uploadType = UploadTypes.fromId(DatastoreUtils.getIntProperty(entity, "type"));
				blobEntity.blobKey = ((BlobKey) entity.getProperty("blob_key")).getKeyString();
				blobEntity.created = (Date) entity.getProperty("created");

				ArrayList<ResourceBlobEntity> revisions = mResponse.blobsToDownload.get(resourceId);
				if (revisions == null) {
					revisions = new ArrayList<>();
					mResponse.blobsToDownload.put(resourceId, revisions);
				}
				revisions.add(blobEntity);
			}
		}
	}

	/**
	 * Get latest revision creation date from the specified resource
	 * @param resourceId
	 * @return latest revision creation date of this resource
	 */
	private Date getLatestRevisionDate(UUID resourceId) {
		Query query = new Query("user_resources", mUser.getKey());

		query.setFilter(DatastoreUtils.createUuidFilter("resource_id", resourceId));
		query.addProjection(new PropertyProjection("created", Date.class));
		query.addSort("created", SortDirection.DESCENDING);

		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);

		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1);

		List<Entity> entities = preparedQuery.asList(fetchOptions);

		// Get date from entity
		if (!entities.isEmpty()) {
			return (Date) entities.get(0).getProperty("created");
		} else {
			mLogger.severe("Could not find latest revision");
			return null;
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
		Entity entity = new Entity("user_resources", mUser.getKey());
		DatastoreUtils.setProperty(entity, "resource_id", resourceRevisionEntity.resourceId);
		entity.setProperty("revision", revisionEntity.revision);
		entity.setProperty("type", resourceRevisionEntity.type.getId());
		entity.setProperty("created", revisionEntity.date);
		entity.setProperty("uploaded", mSyncDate);
		entity.setProperty("blob_key", blobKeys.get(revisionEntity.revision));

		return DatastoreUtils.put(entity);
	}

	/** Sync date */
	private Date mSyncDate = null;
	/** Response */
	private SyncUserResourcesMethodResponse mResponse = null;
}
