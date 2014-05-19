package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.google.appengine.api.datastore.Query.SortDirection;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.PropertyWrapper;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ResourceConflictEntity;
import com.spiddekauga.voider.network.entities.ResourceRevisionEntity;
import com.spiddekauga.voider.network.entities.RevisionEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.SyncUserResourcesMethod;
import com.spiddekauga.voider.network.entities.method.SyncUserResourcesMethodResponse;
import com.spiddekauga.voider.network.entities.method.SyncUserResourcesMethodResponse.Statuses;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Synchronizes user resource revisions
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class SyncUserResources extends VoiderServlet {

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		SyncUserResourcesMethodResponse response = new SyncUserResourcesMethodResponse();
		response.status = Statuses.FAILED_INTERNAL;

		if (!mUser.isLoggedIn()) {
			response.status = Statuses.FAILED_USER_NOT_LOGGED_IN;
			return response;
		}

		if (methodEntity instanceof SyncUserResourcesMethod) {
			Map<UUID, Map<Integer, BlobKey>> blobResources = getUploadedRevisionBlobs();
			Map<UUID, ArrayList<Key>> successRevisions = new HashMap<>();

			// Iterate through each resource id
			for (ResourceRevisionEntity entity : ((SyncUserResourcesMethod) methodEntity).resources) {
				Map<Integer, BlobKey> blobKeys = blobResources.get(entity.resourceId);
				PropertyWrapper resourceProp = new PropertyWrapper("resource_id", entity.resourceId);
				ArrayList<Key> revisions = new ArrayList<>();

				int i = 0;
				int failedRevision = -1;
				// Add uploaded blob revisions to the datastore
				while (i < entity.revisions.size() && failedRevision != -1) {
					RevisionEntity revisionEntity = entity.revisions.get(i);

					PropertyWrapper revisionProp = new PropertyWrapper("revision", revisionEntity.revision);
					if (!DatastoreUtils.exists("user_resources", mUser.getKey(), resourceProp, revisionProp)) {
						revisions.add(createUserResourceRevision(entity, revisionEntity, blobKeys));
					}
					// Revision already exist add resource to conflict
					else {
						failedRevision = i;
					}

					++i;
				}


				if (failedRevision != -1) {
					successRevisions.put(entity.resourceId, revisions);
				}
				// Add resource conflict
				else {
					// From what revision is the conflict?
					ResourceConflictEntity conflict = new ResourceConflictEntity();
					conflict.resourceId = entity.resourceId;
					conflict.fromRevision = failedRevision;

					// When was that?
					PropertyWrapper revisionProp = new PropertyWrapper("revision", failedRevision);
					Entity datastoreEntity = DatastoreUtils.getSingleEntity("user_resources", mUser.getKey(), resourceProp, revisionProp);
					conflict.fromDate = (Date) datastoreEntity.getProperty("created");
					conflict.latestDate = getLatestRevisionDate(entity.resourceId);

					response.conflicts.add(conflict);

					// Remove added revision of that resource
					DatastoreUtils.delete(revisions);

					response.status = Statuses.SUCCESS_PARTIAL;
				}
			}

			if (response.status != Statuses.SUCCESS_PARTIAL) {
				response.status = Statuses.SUCCESS_ALL;
			}
		}

		return response;
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
	private Key createUserResourceRevision(ResourceRevisionEntity resourceRevisionEntity, RevisionEntity revisionEntity, Map<Integer, BlobKey> blobKeys) {
		Entity entity = new Entity("user_resources", mUser.getKey());
		DatastoreUtils.setProperty(entity, "resource_id", resourceRevisionEntity.resourceId);
		entity.setProperty("revision", revisionEntity.revision);
		entity.setProperty("type", resourceRevisionEntity.type.getId());
		entity.setProperty("created", revisionEntity.date);
		entity.setProperty("uploaded", new Date());
		entity.setProperty("blob_key", blobKeys.get(revisionEntity.revision));

		return DatastoreUtils.put(entity);
	}
}
