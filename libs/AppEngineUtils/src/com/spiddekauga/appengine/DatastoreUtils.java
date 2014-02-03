package com.spiddekauga.appengine;


import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;

/**
 * Utilities for Datastore
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class DatastoreUtils {
	/**
	 * Searches for an existing entity
	 * @param searchIn what kind of entity to search in
	 * @param propertyName the property value to search in
	 * @param searchForItem the value to search for
	 * @return found entity, null if none was found
	 */
	public static Entity getSingleItem(String searchIn, String propertyName, Object searchForItem) {
		Query query = new Query(searchIn);
		Filter filter = new Query.FilterPredicate(propertyName, FilterOperator.EQUAL, searchForItem);
		query.setFilter(filter);
		List<Entity> results = mDatastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
		mLogger.info("Found " + results.size() + " entities in (" + searchIn + ") with property (" + propertyName + ") named (" + searchForItem + ")");
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}

	/**
	 * Searches for an entity with the specified key
	 * @param idName the key of the entity
	 * @return entity with specified key, null if not found
	 */
	public static Entity getItemByKey(String idName) {
		Key key = KeyFactory.createKey("__BlobInfo__", idName);
		return getItemByKey(key);
	}

	/**
	 * Searches for an entity with the specified key
	 * @param idName the key of the entity
	 * @return entity with specified key, null if not found
	 */
	public static Entity getItemByKey(Key idName) {
		try {
			return mDatastore.get(idName);
		} catch (EntityNotFoundException e) {
			mLogger.warning("Could not find entity with key: " + idName);
			return null;
		}
	}

	/** Datastore service */
	public static DatastoreService mDatastore = DatastoreServiceFactory.getDatastoreService();
	/** Logger */
	private static final Logger mLogger = Logger.getLogger(DatastoreUtils.class.getName());
}
