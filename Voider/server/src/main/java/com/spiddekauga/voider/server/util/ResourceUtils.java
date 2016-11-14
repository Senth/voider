package com.spiddekauga.voider.server.util;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.resource.ResourceBlobEntity;
import com.spiddekauga.voider.network.resource.UploadTypes;

/**
 * Common methods for resources
 * 

 */
public class ResourceUtils {
	/**
	 * @param levelDef the level def to get information from
	 * @return information about the actual level
	 */
	public static ResourceBlobEntity getBlobLevelInformation(Entity levelDef) {
		ResourceBlobEntity information = new ResourceBlobEntity();

		information.resourceId = DatastoreUtils.getPropertyUuid(levelDef, "level_id");
		information.blobKey = ((BlobKey) levelDef.getProperty("level_blob_key")).getKeyString();
		information.uploadType = UploadTypes.LEVEL;

		return information;
	}

	/**
	 * @param resource get information from the specified resource
	 * @return information about the resource
	 */
	public static ResourceBlobEntity getBlobInformation(Entity resource) {
		ResourceBlobEntity information = new ResourceBlobEntity();

		information.resourceId = DatastoreUtils.getPropertyUuid(resource, "resource_id");
		information.blobKey = ((BlobKey) resource.getProperty("blob_key")).getKeyString();
		long defTypeId = (long) resource.getProperty("type");
		information.uploadType = UploadTypes.fromId((int) defTypeId);

		return information;
	}
}
