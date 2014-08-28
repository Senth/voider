package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ResourceBlobEntity;
import com.spiddekauga.voider.network.entities.UploadTypes;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.SyncDownloadMethod;
import com.spiddekauga.voider.network.entities.method.SyncDownloadMethodResponse;
import com.spiddekauga.voider.network.entities.method.SyncDownloadMethodResponse.Statuses;
import com.spiddekauga.voider.server.util.ResourceUtils;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Checks if there are new published resources to sync to the client depending on the time
 * of last sync.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class SyncDownload extends VoiderServlet {
	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		SyncDownloadMethodResponse response = new SyncDownloadMethodResponse();
		response.status = Statuses.FAILED_INTERNAL;

		if (mUser.isLoggedIn()) {
			if (methodEntity instanceof SyncDownloadMethod) {
				response.syncTime = new Date();
				response.resources = getResourcesToSync(((SyncDownloadMethod) methodEntity).lastSync);
				response.status = Statuses.SUCCESS;
			}
		} else {
			response.status = Statuses.FAILED_USER_NOT_LOGGED_IN;
		}

		return response;
	}

	/**
	 * Get all published resources to download from the current user
	 * @param lastSync date the user synced the last time
	 * @return a list of all resources to sync, can be empty
	 */
	private ArrayList<ResourceBlobEntity> getResourcesToSync(Date lastSync) {
		ArrayList<Key> publishedKeys = getPublishedKeysToSync(lastSync);
		return getResourseBlobsToSync(publishedKeys);
	}

	/**
	 * Get all blob resource information from the published keys
	 * @param publishedKeys all published resources to sync (download)
	 * @return list of all resources to sync
	 */
	private ArrayList<ResourceBlobEntity> getResourseBlobsToSync(ArrayList<Key> publishedKeys) {
		ArrayList<ResourceBlobEntity> resources = new ArrayList<>();

		for (Key key : publishedKeys) {
			Entity entity = DatastoreUtils.getEntity(key);

			if (entity != null) {
				ResourceBlobEntity blobEntity = ResourceUtils.getBlobInformation(entity);
				resources.add(blobEntity);

				// Add level blob
				if (blobEntity.uploadType == UploadTypes.LEVEL_DEF) {
					resources.add(ResourceUtils.getBlobLevelInformation(entity));
				}
			}
		}

		return resources;
	}

	/**
	 * Get all keys to the published resources to download
	 * @param lastSync date the user synced the last time
	 * @return list of all published keys to sync
	 */
	private ArrayList<Key> getPublishedKeysToSync(Date lastSync) {
		// Qualify by date
		FilterWrapper filterWrapper = new FilterWrapper("download_date", FilterOperator.GREATER_THAN, lastSync);

		Iterable<Entity> entities = DatastoreUtils.getEntities("sync_published", mUser.getKey(), filterWrapper);
		ArrayList<Key> publishedKeys = new ArrayList<>();

		for (Entity entity : entities) {
			publishedKeys.add((Key) entity.getProperty("published_key"));
		}

		return publishedKeys;
	}
}
