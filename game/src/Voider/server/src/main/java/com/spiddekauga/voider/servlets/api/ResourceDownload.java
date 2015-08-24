package com.spiddekauga.voider.servlets.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.misc.ChatMessage;
import com.spiddekauga.voider.network.misc.ChatMessage.MessageTypes;
import com.spiddekauga.voider.network.resource.ResourceBlobEntity;
import com.spiddekauga.voider.network.resource.ResourceDownloadMethod;
import com.spiddekauga.voider.network.resource.ResourceDownloadResponse;
import com.spiddekauga.voider.network.resource.ResourceDownloadResponse.Statuses;
import com.spiddekauga.voider.network.resource.ResourceRevisionBlobEntity;
import com.spiddekauga.voider.network.resource.UploadTypes;
import com.spiddekauga.voider.server.util.ResourceUtils;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserResources;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Downloads resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ResourceDownload extends VoiderApiServlet<ResourceDownloadMethod> {
	@Override
	protected void onInit() {
		mResponse.status = Statuses.FAILED_SERVER_INTERAL;
		mResponse.resources.clear();
		mParameters = null;
		mAddedResources.clear();
	}

	@Override
	protected IEntity onRequest(ResourceDownloadMethod method) throws ServletException, IOException {
		if (mUser.isLoggedIn()) {
			boolean success = false;
			mParameters = method;

			// New download
			if (!mParameters.redownload) {
				success = setInformationAndDependenciesToResponse(mParameters.resourceId);

				// Set download date and send sync message
				if (success && !mAddedResources.isEmpty()) {
					setUserDownloadDate();
					mResponse.status = Statuses.SUCCESS;
					sendMessage(new ChatMessage<>(MessageTypes.SYNC_COMMUNITY_DOWNLOAD, mUser.getClientId()));
				}
			}
			// Redownload
			else {
				redownloadResource();
			}
		} else {
			mResponse.status = Statuses.FAILED_USER_NOT_LOGGED_IN;
		}

		return mResponse;
	}

	/**
	 * Redownload resource
	 */
	private void redownloadResource() {
		// Get blob
		ResourceBlobEntity blobEntity = getDownloadBlob();

		// Set blob
		if (blobEntity != null) {
			mResponse.resources.add(blobEntity);
			mResponse.status = Statuses.SUCCESS;
		}
	}

	/**
	 * @return found resource blob entity to redownload
	 */
	private ResourceBlobEntity getDownloadBlob() {
		ResourceBlobEntity foundBlob = null;

		// Published
		if (mParameters.revision == -1) {
			BlobKey blobKey = null;
			UploadTypes uploadType = null;

			// Resource id
			FilterWrapper resourceIdFilter = new FilterWrapper(CPublished.RESOURCE_ID, mParameters.resourceId);
			Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.PUBLISHED, resourceIdFilter);

			if (entity != null) {
				blobKey = (BlobKey) entity.getProperty(CPublished.BLOB_KEY);
				uploadType = DatastoreUtils.getPropertyIdStore(entity, CPublished.TYPE, UploadTypes.class);

				// Level id
			} else {
				FilterWrapper levelIdFilter = new FilterWrapper(CPublished.LEVEL_ID, mParameters.resourceId);
				entity = DatastoreUtils.getSingleEntity(DatastoreTables.PUBLISHED, levelIdFilter);

				if (entity != null) {
					blobKey = (BlobKey) entity.getProperty(CPublished.LEVEL_BLOB_KEY);
					uploadType = UploadTypes.LEVEL;
				}
			}

			// Set blob info
			if (blobKey != null) {
				foundBlob = new ResourceBlobEntity();
				foundBlob.blobKey = blobKey.getKeyString();
				foundBlob.resourceId = mParameters.resourceId;
				foundBlob.uploadType = uploadType;
			}
		}
		// User resource
		else {
			FilterWrapper resourceIdFilter = new FilterWrapper(CUserResources.RESOURCE_ID, mParameters.resourceId);
			FilterWrapper resourceRevisionFilter = new FilterWrapper(CUserResources.REVISION, mParameters.revision);
			Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.USER_RESOURCES, resourceIdFilter, resourceRevisionFilter);

			if (entity != null) {
				BlobKey blobKey = (BlobKey) entity.getProperty(CUserResources.BLOB_KEY);

				foundBlob = new ResourceRevisionBlobEntity();
				foundBlob.blobKey = blobKey.getKeyString();
				foundBlob.resourceId = mParameters.resourceId;
				foundBlob.uploadType = DatastoreUtils.getPropertyIdStore(entity, CUserResources.TYPE, UploadTypes.class);
				((ResourceRevisionBlobEntity) foundBlob).revision = mParameters.revision;
			}
		}

		return foundBlob;
	}

	/**
	 * Set download date for the specified user so these resources can be synchronized
	 * between devices.
	 */
	private void setUserDownloadDate() {
		for (Key key : mAddedResources) {
			// Create entity if user hasn't downloaded the resource before
			if (!DatastoreUtils.exists("sync_published", mUser.getKey(), new FilterWrapper("published_key", key))) {
				Entity entity = new Entity("sync_published", mUser.getKey());
				entity.setUnindexedProperty("published_key", key);
				entity.setProperty("download_date", new Date());

				DatastoreUtils.put(entity);
			}
		}
	}

	/**
	 * Get information about the specified resource and its dependencies
	 * @param resourceId id of the resource to get information from and set to the
	 *        response
	 * @return true if successfully added information and dependencies
	 */
	private boolean setInformationAndDependenciesToResponse(UUID resourceId) {
		// Get resource key
		Key resourceKey = DatastoreUtils.getSingleKey(DatastoreTables.PUBLISHED.toString(), new FilterWrapper("resource_id", resourceId));
		if (resourceKey != null) {
			return setInformationAndDependenciesToResponse(resourceKey);
		}
		return false;
	}

	/**
	 * Get information about the specified resource and its dependencies
	 * @param resourceKey key of the resource to get information from and set to the
	 *        response
	 * @return true if successfully added information and dependencies
	 */
	private boolean setInformationAndDependenciesToResponse(Key resourceKey) {
		if (!mAddedResources.contains(resourceKey)) {
			mAddedResources.add(resourceKey);
			Entity resource = DatastoreUtils.getEntity(resourceKey);
			if (resource == null) {
				return false;
			}

			// Information
			ResourceBlobEntity information = ResourceUtils.getBlobInformation(resource);
			if (information == null) {
				return false;
			}
			mResponse.resources.add(information);

			// Level def? Then add level resource
			if (information.uploadType == UploadTypes.LEVEL_DEF) {
				mResponse.resources.add(ResourceUtils.getBlobLevelInformation(resource));
			}

			// Dependencies
			List<Key> dependencies = getDependencies(resourceKey);
			if (dependencies == null) {
				return false;
			}
			// Recursive for all dependencies
			for (Key dependency : dependencies) {
				boolean success = setInformationAndDependenciesToResponse(dependency);

				if (!success) {
					return false;
				}
			}
		}

		return true;
	}


	/**
	 * @param resourceKey get all dependencies of the specified key
	 * @return all dependencies of the specified resource
	 */
	private List<Key> getDependencies(Key resourceKey) {
		Iterable<Entity> dependencies = DatastoreUtils.getEntities(DatastoreTables.DEPENDENCY.toString(), resourceKey);
		ArrayList<Key> keys = new ArrayList<>();

		for (Entity dependency : dependencies) {
			keys.add((Key) dependency.getProperty("dependency"));
		}

		return keys;
	}

	/** Method */
	private ResourceDownloadMethod mParameters = null;
	/** Method response */
	private ResourceDownloadResponse mResponse = new ResourceDownloadResponse();
	/** All added resources */
	private HashSet<Key> mAddedResources = new HashSet<>();
}
