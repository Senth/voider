package com.spiddekauga.voider.server.util;

import java.util.Date;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.entities.resource.DefEntity;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;


/**
 * Common class for getting resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public abstract class ResourceFetch extends VoiderServlet {
	/**
	 * Set a def entity from a datastore entity
	 * @param <DefType> extended type of a DefEntity
	 * @param datastoreEntity in parameter
	 * @param networkEntity out parameter
	 * @return the network entity (same as the second parameter)
	 */
	protected static <DefType extends DefEntity> DefType datastoreToDefEntity(Entity datastoreEntity, DefType networkEntity) {
		networkEntity.copyParentId = DatastoreUtils.getUuidProperty(datastoreEntity, CPublished.COPY_PARENT_ID);
		networkEntity.date = (Date) datastoreEntity.getProperty(CPublished.DATE);
		networkEntity.description = (String) datastoreEntity.getProperty(CPublished.DESCRIPTION);
		networkEntity.name = (String) datastoreEntity.getProperty(CPublished.NAME);
		networkEntity.resourceId = DatastoreUtils.getUuidProperty(datastoreEntity, CPublished.RESOURCE_ID);
		networkEntity.png = DatastoreUtils.getByteArrayProperty(datastoreEntity, CPublished.PNG);

		// Set creators
		Key creatorKey = datastoreEntity.getParent();
		Key originalCreatorKey = (Key) datastoreEntity.getProperty(CPublished.ORIGINAL_CREATOR_KEY);
		networkEntity.creatorKey = KeyFactory.keyToString(creatorKey);
		networkEntity.originalCreatorKey = KeyFactory.keyToString(originalCreatorKey);
		networkEntity.creator = UserRepo.getUsername(creatorKey);
		networkEntity.originalCreator = UserRepo.getUsername(originalCreatorKey);

		// Skip dependencies, no need for the player to know about them

		return networkEntity;

	}
}
