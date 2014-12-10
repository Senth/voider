package com.spiddekauga.voider.server.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.voider.network.entities.resource.DefEntity;
import com.spiddekauga.voider.network.entities.resource.FetchStatuses;
import com.spiddekauga.voider.network.util.ISearchStore;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.ServerConfig.FetchSizes;


/**
 * Common class for getting resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 * @param <ReturnType>
 */
@SuppressWarnings("serial")
public abstract class ResourceFetch<ReturnType> extends VoiderServlet {
	/**
	 * Set a def entity from a datastore entity. If ReturnType isn't an extension of
	 * DefEntity overload this method
	 * @param <DefType> definition type
	 * @param datastoreEntity in parameter
	 * @param networkEntity out parameter
	 * @return the network entity (same as the second parameter)
	 */
	protected <DefType extends DefEntity> DefType datastoreToDefEntity(Entity datastoreEntity, DefType networkEntity) {
		networkEntity.copyParentId = DatastoreUtils.getUuidProperty(datastoreEntity, CPublished.COPY_PARENT_ID);
		networkEntity.date = (Date) datastoreEntity.getProperty(CPublished.DATE);
		networkEntity.description = (String) datastoreEntity.getProperty(CPublished.DESCRIPTION);
		networkEntity.name = (String) datastoreEntity.getProperty(CPublished.NAME);
		networkEntity.resourceId = DatastoreUtils.getUuidProperty(datastoreEntity, CPublished.RESOURCE_ID);
		networkEntity.png = DatastoreUtils.getByteArrayProperty(datastoreEntity, CPublished.PNG);

		// Set creators
		Key creatorKey = datastoreEntity.getParent();
		Key originalCreatorKey = (Key) datastoreEntity.getProperty(CPublished.ORIGINAL_CREATOR_KEY);
		networkEntity.revisedByKey = KeyFactory.keyToString(creatorKey);
		networkEntity.originalCreatorKey = KeyFactory.keyToString(originalCreatorKey);
		networkEntity.revisedBy = UserRepo.getUsername(creatorKey);
		networkEntity.originalCreator = UserRepo.getUsername(originalCreatorKey);

		// Skip dependencies, no need for the player to know about them

		return networkEntity;
	}

	/**
	 * Set a def entity from a datastore entity. If ReturnType isn't an extension of
	 * DefEntity overload this method
	 * @param datastoreEntity in parameter
	 * @param networkEntity out parameter
	 * @return the network entity (same as the second parameter)
	 */
	protected ReturnType datastoreToDefEntity(Entity datastoreEntity, ReturnType networkEntity) {
		if (networkEntity instanceof DefEntity) {
			datastoreToDefEntity(datastoreEntity, (DefEntity) networkEntity);
		}

		return networkEntity;
	}

	/**
	 * Add an array of values into the search builder
	 * @param fieldName name of the field
	 * @param array the array to add
	 * @param maxLength maximum length of the array, if the array is of this size nothing
	 *        will be added
	 * @param builder the builder to add to
	 */
	protected static void appendSearchEnumArray(String fieldName, ArrayList<? extends ISearchStore> array, int maxLength, SearchUtils.Builder builder) {
		if (!array.isEmpty() && array.size() != maxLength) {
			String[] searchFor = new String[array.size()];
			for (int i = 0; i < searchFor.length; i++) {
				searchFor[i] = array.get(i).getSearchId();
			}
			builder.text(fieldName, searchFor);
		}
	}

	/**
	 * Builds a search string to search for
	 * @return search string to use when searching
	 */
	protected abstract String buildSearchString();

	/**
	 * Search and set the enemies as a response
	 * @param searchTable name of the table to search in
	 * @param nextCursor
	 * @param defs the definition to add to
	 * @return fetchStatus of the search
	 */
	protected FetchStatuses searchAndSetFoundDefs(String searchTable, String nextCursor, ArrayList<ReturnType> defs) {
		String searchString = buildSearchString();

		Results<ScoredDocument> documents = SearchUtils.search(searchTable, searchString, FetchSizes.ACTORS, nextCursor);

		// Convert all found documents to entities
		for (ScoredDocument document : documents) {
			ReturnType networkEntity = newNetworkDef();

			// Datastore
			Key datastoreKey = KeyFactory.stringToKey(document.getId());
			Entity datastoreEntity = DatastoreUtils.getEntity(datastoreKey);
			datastoreToDefEntity(datastoreEntity, networkEntity);

			setAdditionalDefInformation(datastoreEntity, networkEntity);
			defs.add(networkEntity);
		}

		// Did we fetch all
		return getSuccessStatus(defs);
	}

	/**
	 * Adds additional information to the def
	 * @param datastoreEntity datastoreEntity
	 * @param networkEntity newly created actor that needs information to be set
	 */
	protected abstract void setAdditionalDefInformation(Entity datastoreEntity, ReturnType networkEntity);

	/**
	 * @return new empty actor definition
	 */
	protected abstract ReturnType newNetworkDef();

	/**
	 * Checks if we fetched all and returns the correct success status
	 * @param list the results from a query
	 * @return SUCCESS_FETCHED_ALL or SUCCESS_MORE_EXISTS depending on the size of the
	 *         list
	 */
	protected abstract FetchStatuses getSuccessStatus(List<?> list);
}
