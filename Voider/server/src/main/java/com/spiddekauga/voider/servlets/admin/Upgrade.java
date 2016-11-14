package com.spiddekauga.voider.servlets.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.BlobUtils;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.resource.UploadTypes;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CHighscore;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserResources;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserResourcesDeleted;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUsers;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Does an upgrade for the server

 */
@SuppressWarnings({ "serial", "unused" })
public class Upgrade extends VoiderServlet {

	@Override
	protected void handleRequest() throws ServletException, IOException {
		deleteBackupInfo();

		getResponse().setContentType("text/html");
		getResponse().getWriter().append("DONE !");
	}

	private void deleteBackupInfo() {
		DatastoreUtils.delete(DatastoreUtils.getKeys("_AE_Backup_Information_Kind_Type_Info"));
	}

	private void convertUuidIntToString() {
		List<Entity> addEntities = new ArrayList<>();
		// Highscore (level_id)
		convertProperties(addEntities, DatastoreTables.HIGHSCORE, CHighscore.LEVEL_ID);
		// Published (copy_parent_id, level_id, resource_id)
		convertProperties(addEntities, DatastoreTables.PUBLISHED, CPublished.COPY_PARENT_ID, CPublished.LEVEL_ID, CPublished.RESOURCE_ID);
		// User Resources (resource_id)
		convertProperties(addEntities, DatastoreTables.USER_RESOURCES, CUserResources.RESOURCE_ID);
		// User Resources Deleted (resource_id)
		convertProperties(addEntities, DatastoreTables.USER_RESOURCES_DELETED, CUserResourcesDeleted.RESOURCE_ID);
		// Users (private_key)
		convertProperties(addEntities, DatastoreTables.USERS, CUsers.PRIVATE_KEY);

		DatastoreUtils.put(addEntities);
	}

	private void convertProperties(List<Entity> addEntities, String tableName, String... propertyNames) {
		Iterable<Entity> foundEntities = DatastoreUtils.getEntities(tableName);
		for (Entity entity : foundEntities) {
			boolean success = false;
			for (String propertyName : propertyNames) {
				if (convertProperty(entity, propertyName)) {
					success = true;
				}
			}

			if (success) {
				addEntities.add(entity);
			}
		}
	}

	private boolean convertProperty(Entity entity, String propertyName) {
		if (entity.hasProperty(propertyName + "-least")) {
			Long least = (Long) entity.getProperty(propertyName + "-least");
			Long most = (Long) entity.getProperty(propertyName + "-most");
			UUID uuid = new UUID(most, least);

			// Unindexed
			if (entity.isUnindexedProperty(propertyName + "-least")) {
				entity.setUnindexedProperty(propertyName, uuid.toString());
			}
			// Indexed
			else {
				entity.setProperty(propertyName, uuid.toString());
			}
			entity.removeProperty(propertyName + "-least");
			entity.removeProperty(propertyName + "-most");
			return true;
		} else {
			return false;
		}
	}


	private void makeBlobsUnindexed() {
		ArrayList<Entity> updateEntities = new ArrayList<>();

		Iterable<Entity> entities = DatastoreUtils.getEntities(DatastoreTables.PUBLISHED);
		for (Entity entity : entities) {
			BlobKey blobKey = (BlobKey) entity.getProperty(CPublished.BLOB_KEY);
			entity.removeProperty(CPublished.BLOB_KEY);
			entity.setUnindexedProperty(CPublished.BLOB_KEY, blobKey);
			updateEntities.add(entity);
		}

		DatastoreUtils.put(updateEntities);
	}

	private void indexLevelId() {
		ArrayList<Entity> updateEntities = new ArrayList<>();

		FilterWrapper filterByLevel = new FilterWrapper(CPublished.TYPE, UploadTypes.LEVEL_DEF.toId());
		Iterable<Entity> entities = DatastoreUtils.getEntities(DatastoreTables.PUBLISHED, filterByLevel);
		for (Entity entity : entities) {
			UUID levelId = DatastoreUtils.getPropertyUuid(entity, CPublished.LEVEL_ID);
			entity.removeProperty(CPublished.LEVEL_ID);
			DatastoreUtils.setProperty(entity, CPublished.LEVEL_ID, levelId);
			updateEntities.add(entity);
		}

		DatastoreUtils.put(updateEntities);
	}

	private void removeDeletedBlobs() {
		ArrayList<Key> resourceToRemove = new ArrayList<>();
		ArrayList<BlobKey> blobsToRemove = new ArrayList<>();
		ArrayList<Entity> entitiesToAdd = new ArrayList<>();

		Date date = new Date();

		FilterWrapper filterByLevel = new FilterWrapper(CPublished.TYPE, UploadTypes.LEVEL_DEF.toId());
		Iterable<Entity> publishedEntities = DatastoreUtils.getEntities(DatastoreTables.PUBLISHED, filterByLevel);
		for (Entity publishedEntity : publishedEntities) {
			UUID levelId = DatastoreUtils.getPropertyUuid(publishedEntity, CPublished.LEVEL_ID);
			FilterWrapper filterById = new FilterWrapper(CUserResources.RESOURCE_ID, levelId);
			Iterable<Entity> userEntities = DatastoreUtils.getEntities(DatastoreTables.USER_RESOURCES, filterById);

			Key parent = null;
			for (Entity userEntity : userEntities) {
				// Delete blob key
				parent = userEntity.getParent();
				BlobKey blobKey = (BlobKey) userEntity.getProperty(CUserResources.BLOB_KEY);
				blobsToRemove.add(blobKey);

				// Delete resource revision
				resourceToRemove.add(userEntity.getKey());
			}

			// Add to deleted
			if (parent != null) {
				Entity deletedEntity = new Entity(DatastoreTables.USER_RESOURCES_DELETED, parent);
				DatastoreUtils.setProperty(deletedEntity, CUserResourcesDeleted.RESOURCE_ID, levelId);
				DatastoreUtils.setProperty(deletedEntity, CUserResourcesDeleted.DATE, date);
				entitiesToAdd.add(deletedEntity);
			}
		}

		DatastoreUtils.delete(resourceToRemove);
		DatastoreUtils.put(entitiesToAdd);
		BlobUtils.delete(blobsToRemove);
	}
}
