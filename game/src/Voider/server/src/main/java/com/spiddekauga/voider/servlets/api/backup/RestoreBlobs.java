package com.spiddekauga.voider.servlets.api.backup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.appengine.DatastoreUtils.PropertyNotFoundException;
import com.spiddekauga.voider.network.backup.RestoreBlobsMethod;
import com.spiddekauga.voider.network.backup.RestoreBlobsResponse;
import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserResources;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Called when blobs have been uploaded and need to be restored or bound to the correct
 * datastore object.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class RestoreBlobs extends VoiderApiServlet<RestoreBlobsMethod> {
	@Override
	protected IEntity onRequest(RestoreBlobsMethod method) throws ServletException, IOException {
		try {
			ResourceBlobKeyContainer container = getBlobKeys();
			bindBlobKeys(container);
			mResponse.status = GeneralResponseStatuses.SUCCESS;
		} catch (IllegalArgumentException e) {
			mResponse.errorMessage = "Failed to convert fieldname to UUID: " + e.getMessage();
			mResponse.status = GeneralResponseStatuses.FAILED_SERVER_ERROR;
		} catch (ResourceNotFoundException e) {
			mResponse.errorMessage = e.getErrorMessage();
			mResponse.status = GeneralResponseStatuses.FAILED_SERVER_ERROR;
		}
		return mResponse;
	}

	/**
	 * Get all uploaded blobs with resource id and possible revision
	 * @return blobKeys with resource id and possible revision
	 */
	private ResourceBlobKeyContainer getBlobKeys() {
		ResourceBlobKeyContainer container = new ResourceBlobKeyContainer();

		BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
		Map<String, List<BlobKey>> blobKeys = blobstore.getUploads(getRequest());

		for (Entry<String, List<BlobKey>> entry : blobKeys.entrySet()) {
			container.add(entry.getKey(), entry.getValue().get(0));
		}

		return container;
	}

	/**
	 * Bind resources
	 * @param container container for all blob keys
	 * @throws ResourceNotFoundException
	 */
	private void bindBlobKeys(ResourceBlobKeyContainer container) throws ResourceNotFoundException {
		// User resources
		for (Entry<UUID, Map<Integer, BlobKey>> entry : container.mUserBlobKeys.entrySet()) {
			bindUserResource(entry.getKey(), entry.getValue());
		}

		// Published resources
		for (Entry<UUID, BlobKey> entry : container.mPublishedBlobKeys.entrySet()) {
			bindPublishedResource(entry.getKey(), entry.getValue());
		}

		// Save entities
		DatastoreUtils.put(mEntitiesToUpdate);
	}

	/**
	 * Bind a published resource
	 * @param resourceId
	 * @param blobKey
	 * @throws ResourceNotFoundException
	 */
	private void bindPublishedResource(UUID resourceId, BlobKey blobKey) throws ResourceNotFoundException {
		// Search for resource id
		FilterWrapper filterByResourceId = new FilterWrapper(CPublished.RESOURCE_ID, resourceId);
		Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.PUBLISHED, filterByResourceId);

		if (entity != null) {
			mLogger.info("Resource found");
			entity.setUnindexedProperty(CPublished.BLOB_KEY, blobKey);
		}

		// Search level id
		else {
			FilterWrapper filterByLevelId = new FilterWrapper(CPublished.LEVEL_ID, resourceId);
			entity = DatastoreUtils.getSingleEntity(DatastoreTables.PUBLISHED, filterByLevelId);

			if (entity != null) {
				entity.setUnindexedProperty(CPublished.LEVEL_BLOB_KEY, blobKey);
			}
		}

		// Add to datastore
		if (entity != null) {
			mLogger.info(entity.toString());
			// Add directly to the datastore so both IDs are updated correctly
			if (entity.hasProperty(CPublished.LEVEL_BLOB_KEY)) {
				mLogger.info("Put directly");
				DatastoreUtils.put(entity);
			}
			// Update later
			else {
				mEntitiesToUpdate.add(entity);
			}
		}
		// Entity not found, abort
		else {
			throw new ResourceNotFoundException("Couldn't find resource (" + resourceId + ") in published! (least: "
					+ resourceId.getLeastSignificantBits() + ", most: " + resourceId.getMostSignificantBits());
		}
	};

	/**
	 * Bind a user resource
	 * @param resourceId
	 * @param blobKeys
	 * @throws ResourceNotFoundException
	 */
	private void bindUserResource(UUID resourceId, Map<Integer, BlobKey> blobKeys) throws ResourceNotFoundException {
		// Search for user resource id
		FilterWrapper filterByResourceId = new FilterWrapper(CUserResources.RESOURCE_ID, resourceId);
		Iterable<Entity> entities = DatastoreUtils.getEntities(DatastoreTables.USER_RESOURCES, filterByResourceId);

		for (Entity entity : entities) {
			try {
				int entityRevision = DatastoreUtils.getPropertyInt(entity, CUserResources.REVISION);

				BlobKey revisionBlobKey = blobKeys.get(entityRevision);
				if (revisionBlobKey != null) {
					entity.setProperty(CUserResources.BLOB_KEY, revisionBlobKey);
					mEntitiesToUpdate.add(entity);
					blobKeys.remove(entityRevision);
				}
			} catch (PropertyNotFoundException e) {
				throw new ResourceNotFoundException("No revision property for user entity...");
			}
		}

		// Not all revisions were updated, abort
		if (!blobKeys.isEmpty()) {
			throw new ResourceNotFoundException("Couldn't update " + blobKeys.size() + " revisions for " + resourceId);
		}
	}

	@Override
	protected void onInit() {
		mResponse = new RestoreBlobsResponse();
	}

	/**
	 * Thrown if the resource wasn't found
	 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
	 */
	private class ResourceNotFoundException extends Exception {
		/**
		 * @param errorMessage
		 */
		private ResourceNotFoundException(String errorMessage) {
			mErrorMessage = errorMessage;
		}

		/**
		 * @return the error message
		 */
		private String getErrorMessage() {
			return mErrorMessage;
		}

		/** Where the resource wasn't found */
		private String mErrorMessage = null;
	}

	/**
	 * Container and helper class for getting blob keys from a resource and revision
	 */
	private class ResourceBlobKeyContainer {
		/**
		 * Add a new blob key to the list
		 * @param resourceRevisionString field name from the blob key. Will be converted
		 *        to UUID and revision
		 * @param blobKey the blob key of the blob
		 * @throws IllegalArgumentException if resourceRevisionString isn't in the valid
		 *         format UUID or UUID_Integer.
		 */
		private void add(String resourceRevisionString, BlobKey blobKey) {
			int splitLocation = resourceRevisionString.indexOf("_");

			// User resource
			if (splitLocation != -1) {
				String[] split = resourceRevisionString.split("_");
				UUID resourceId = UUID.fromString(split[0]);
				Integer revision = Integer.parseInt(split[1]);

				Map<Integer, BlobKey> revisions = mUserBlobKeys.get(resourceId);
				if (revisions == null) {
					revisions = new HashMap<>();
					mUserBlobKeys.put(resourceId, revisions);
				}

				// Put in blob key
				revisions.put(revision, blobKey);
			}
			// Published resource
			else {
				UUID resourceId = UUID.fromString(resourceRevisionString);
				mPublishedBlobKeys.put(resourceId, blobKey);
			}
		}

		private Map<UUID, BlobKey> mPublishedBlobKeys = new HashMap<>();
		private Map<UUID, Map<Integer, BlobKey>> mUserBlobKeys = new HashMap<>();
	}

	private List<Entity> mEntitiesToUpdate = new ArrayList<>();
	private RestoreBlobsResponse mResponse;
}
