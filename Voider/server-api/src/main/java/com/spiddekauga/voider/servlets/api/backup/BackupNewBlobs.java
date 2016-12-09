package com.spiddekauga.voider.servlets.api.backup;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.backup.BackupNewBlobsMethod;
import com.spiddekauga.voider.network.backup.BackupNewBlobsResponse;
import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.resource.ResourceBlobEntity;
import com.spiddekauga.voider.network.resource.ResourceRevisionBlobEntity;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserResources;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

/**
 * Get all new blobs from the server so we can download them for backup purposes.
 */
@SuppressWarnings("serial")
public class BackupNewBlobs extends VoiderApiServlet<BackupNewBlobsMethod> {

private BackupNewBlobsResponse mResponse = null;

@Override
protected boolean isHandlingRequestDuringMaintenance() {
	return true;
}

@Override
protected void onInit() {
	mResponse = new BackupNewBlobsResponse();
	mResponse.status = GeneralResponseStatuses.FAILED_SERVER_ERROR;
}

@Override
protected IEntity onRequest(BackupNewBlobsMethod method) throws ServletException, IOException {
	getNewPublished(method.lastBackup);
	getNewUserRevisions(method.lastBackup);
	mResponse.status = GeneralResponseStatuses.SUCCESS;

	return mResponse;
}

/**
 * Get all new published resources
 * @param since date since last backup
 */
private void getNewPublished(Date since) {
	FilterWrapper filter = new FilterWrapper(CPublished.DATE, FilterOperator.GREATER_THAN, since);
	Iterable<Entity> entities = DatastoreUtils.getEntities(DatastoreTables.PUBLISHED, filter);

	for (Entity entity : entities) {
		ResourceBlobEntity blobEntity = new ResourceBlobEntity();
		blobEntity.resourceId = DatastoreUtils.getPropertyUuid(entity, CPublished.RESOURCE_ID);
		blobEntity.created = (Date) entity.getProperty(CPublished.DATE);
		BlobKey blobKey = (BlobKey) entity.getProperty(CPublished.BLOB_KEY);
		if (blobKey != null) {
			blobEntity.blobKey = blobKey.getKeyString();
			mResponse.publishedBlobs.add(blobEntity);
		}

		// Level blob
		if (entity.hasProperty(CPublished.LEVEL_BLOB_KEY)) {
			blobEntity = new ResourceBlobEntity();
			blobEntity.resourceId = DatastoreUtils.getPropertyUuid(entity, CPublished.LEVEL_ID);
			blobEntity.created = (Date) entity.getProperty(CPublished.DATE);
			blobKey = (BlobKey) entity.getProperty(CPublished.LEVEL_BLOB_KEY);
			if (blobKey != null) {
				blobEntity.blobKey = blobKey.getKeyString();
				mResponse.publishedBlobs.add(blobEntity);
			}
		}
	}
}

/**
 * Get all new user resource revisions
 * @param since date since last backup
 */
private void getNewUserRevisions(Date since) {
	FilterWrapper filter = new FilterWrapper(CUserResources.UPLOADED, FilterOperator.GREATER_THAN, since);
	Iterable<Entity> userResources = DatastoreUtils.getEntities(DatastoreTables.USER_RESOURCES, filter);

	for (Entity entity : userResources) {
		ResourceRevisionBlobEntity blobEntity = new ResourceRevisionBlobEntity();
		blobEntity.resourceId = DatastoreUtils.getPropertyUuid(entity, CUserResources.RESOURCE_ID);
		blobEntity.revision = DatastoreUtils.getPropertyInt(entity, CUserResources.REVISION, 0);
		blobEntity.created = (Date) entity.getProperty(CUserResources.UPLOADED);
		BlobKey blobKey = (BlobKey) entity.getProperty(CUserResources.BLOB_KEY);
		if (blobKey != null) {
			blobEntity.blobKey = blobKey.getKeyString();
			mResponse.userBlobs.add(blobEntity);
		}
	}
}
}
