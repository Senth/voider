package com.spiddekauga.voider.servlets.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.BlobUtils;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.resource.UploadTypes;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserResources;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserResourcesDeleted;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Does an upgrade for the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings({ "serial" })
public class Upgrade extends VoiderApiServlet<IMethodEntity> {
	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(IMethodEntity method) throws ServletException, IOException {
		indexLevelId();
		removeDeletedBlobs();

		getResponse().setContentType("text/html");
		getResponse().getWriter().append("DONE !");

		return null;
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
