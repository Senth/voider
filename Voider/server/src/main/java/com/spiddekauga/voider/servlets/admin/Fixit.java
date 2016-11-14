package com.spiddekauga.voider.servlets.admin;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.VoiderController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

/**
 * View, edit and generate new MOTDs for Voider
 */
@SuppressWarnings("serial")
public class Fixit extends VoiderController {

/** Blobstore service */
private static final BlobstoreService mBlobstore = BlobstoreServiceFactory.getBlobstoreService();

@Override
protected void onRequest() {
	Map<UUID, BlobKey> blobs = getBlobKeysFromUpload(getRequest());

	if (blobs != null && !blobs.isEmpty()) {
		mLogger.info("Uploaded blob!");
		for (Entry<UUID, BlobKey> entry : blobs.entrySet()) {
			updateResourceBlobKey(entry.getKey(), entry.getValue());
		}
	}

	forwardToHtml();
}

/**
 * Get all uploaded blob keys from a request
 * @param request the request send to the servlet
 * @return Map with all blob keys mapped to a UUID, null if no uploads were made
 */
public Map<UUID, BlobKey> getBlobKeysFromUpload(HttpServletRequest request) {
	HashMap<UUID, BlobKey> blobKeys = new HashMap<>();

	try {
		Map<String, List<BlobKey>> map = mBlobstore.getUploads(request);
		for (Entry<String, List<BlobKey>> entry : map.entrySet()) {
			try {
				blobKeys.put(UUID.fromString(getParameter("uuid")), entry.getValue().get(0));
			} catch (IllegalArgumentException e) {
				mLogger.severe("UUID string invalid: " + entry.getKey());
			}
		}
	} catch (IllegalStateException e) {
		mLogger.fine("No blob uploads found");
		return null;
	}

	return blobKeys;
}

/**
 * Update blob for an existing resource
 * @param resourceId
 * @param blobKey new blob key
 */
private void updateResourceBlobKey(UUID resourceId, BlobKey blobKey) {
	// Get resource
	FilterWrapper filter = new FilterWrapper(CPublished.RESOURCE_ID, resourceId);
	Entity resourceEntity = DatastoreUtils.getSingleEntity(DatastoreTables.PUBLISHED, filter);
	if (resourceEntity != null) {
		resourceEntity.setProperty(CPublished.BLOB_KEY, blobKey);
		DatastoreUtils.put(resourceEntity);
		mLogger.info("Replaced blob for " + resourceEntity.getProperty(CPublished.NAME));
		return;
	}

	// Check level blob
	Iterable<Entity> entities = DatastoreUtils.getEntities(DatastoreTables.PUBLISHED);
	for (Entity entity : entities) {
		UUID levelId = DatastoreUtils.getPropertyUuid(entity, CPublished.LEVEL_ID);
		if (levelId != null && levelId.equals(resourceId)) {
			entity.setProperty(CPublished.LEVEL_BLOB_KEY, blobKey);
			DatastoreUtils.put(entity);
			mLogger.info("Replaced blob for " + entity.getProperty(CPublished.NAME));
			return;
		}
	}

}
}
