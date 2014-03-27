package com.spiddekauga.appengine;


import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.ShortBlob;

/**
 * Utilities for Datastore
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class DatastoreUtils {
	/**
	 * Searches for an existing entity
	 * @param searchIn what kind of entity to search in
	 * @param propertyName the property value to search in, can be null
	 * @param value the value to search for, can be null
	 * @param parent the parent of the entity to find, set to null to skip
	 * @return found entity, null if none or more than 1 was found
	 */
	public static Entity getSingleEntity(String searchIn, String propertyName, Object value, Key parent) {
		Query query = new Query(searchIn);
		if (propertyName != null) {
			Filter filter = null;
			if (value instanceof UUID) {
				filter = createUuidFilter(propertyName, (UUID) value);
			} else if (value != null) {
				filter = new Query.FilterPredicate(propertyName, FilterOperator.EQUAL, value);
			}

			if (filter != null) {
				query.setFilter(filter);
			}
		}


		if (parent != null) {
			query.setAncestor(parent);
		}

		try {
			return mDatastore.prepare(query).asSingleEntity();
		} catch (TooManyResultsException e) {
			// Does nothing
		}
		return null;
	}

	/**
	 * Searches for an existing entity
	 * @param searchIn what kind of entity to search in
	 * @param propertyName the property value to search in
	 * @param value the value to search for
	 * @return found entity, null if none or more than 1 was found
	 */
	public static Entity getSingleEntity(String searchIn, String propertyName, Object value) {
		return getSingleEntity(searchIn, propertyName, value, null);
	}

	/**
	 * Searches for an existing entity
	 * @param searchIn what kind of entity to search in
	 * @param propertyName the property value to search in, can be null
	 * @param value the value to search for, can be null
	 * @param parent the parent for the entity, set to null to skip using
	 * @return found key for entity
	 */
	public static Key getSingleKey(String searchIn, String propertyName, Object value, Key parent) {
		Query query = new Query(searchIn).setKeysOnly();
		if (propertyName != null) {
			Filter filter = null;
			if (value instanceof UUID) {
				filter = createUuidFilter(propertyName, (UUID) value);
			} else if (value != null) {
				filter = new FilterPredicate(propertyName, FilterOperator.EQUAL, value);
			}

			if (filter != null) {
				query.setFilter(filter);
			}
		}

		if (parent != null) {
			query.setAncestor(parent);
		}

		try {
			Entity foundEntity = mDatastore.prepare(query).asSingleEntity();
			if (foundEntity != null) {
				return foundEntity.getKey();
			}
		} catch (TooManyResultsException e) {
			// Does nothing
		}
		return null;
	}

	/**
	 * Searches for an existing entity
	 * @param searchIn what kind of entity to search in
	 * @param propertyName the property value to search in
	 * @param value the value to search for
	 * @return found key for entity
	 */
	public static Key getSingleKey(String searchIn, String propertyName, Object value) {
		return getSingleKey(searchIn, propertyName, value, null);
	}

	/**
	 * Creates an equal UUID search filter
	 * @param propertyName property name in the entity (column)
	 * @param value the value to search for
	 * @return equal filter for the UUID value
	 */
	public static Filter createUuidFilter(String propertyName, UUID value) {
		Filter least = new FilterPredicate(propertyName + "-least", FilterOperator.EQUAL, value.getLeastSignificantBits());
		Filter most = new FilterPredicate(propertyName + "-most", FilterOperator.EQUAL, value.getMostSignificantBits());

		return createCompositeFilter(CompositeFilterOperator.AND, least, most);
	}

	/**
	 * Creates a composite filter out of the specified filters
	 * @param operator composite filter operator (AND/OR)
	 * @param filters all the filters to add
	 * @return a composite filter with the specified filters
	 */
	public static Filter createCompositeFilter(CompositeFilterOperator operator, Filter... filters) {
		ArrayList<Filter> arrayListFilters = new ArrayList<>();

		for (Filter filter : filters) {
			arrayListFilters.add(filter);
		}

		return new CompositeFilter(operator, arrayListFilters);
	}

	/**
	 * Searches if an entity exists
	 * @param searchIn what kind of entity to search in
	 * @param propertyName name of the property (column)
	 * @param value the value to search for
	 * @return true if the datastore contains the specified entity
	 */
	public static boolean containsEntity(String searchIn, String propertyName, Object value) {
		return getSingleKey(searchIn, propertyName, value) != null;
	}

	/**
	 * Searches for an entity with the specified key in blob info
	 * @param idName the key of the blob entity to find
	 * @return blob entity with specified key, null if not found
	 */
	public static Entity getBlobEntityByKey(String idName) {
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

	/**
	 * Set property to an entity, but only if it's not null
	 * @param entity the entity to set the property in
	 * @param propertyName name of the property
	 * @param value the object to set as the property value. If null this method
	 * does nothing.
	 */
	public static void setProperty(Entity entity, String propertyName, Object value) {
		if (value != null) {
			entity.setProperty(propertyName, value);
		}
	}

	/**
	 * Set an unindex property to an entity, but only if it's not null
	 * @param entity the entity to set the property in
	 * @param propertyName name of the property
	 * @param value the object to set as the property value. If null this method
	 * does nothing.
	 */
	public static void setUnindexedProperty(Entity entity, String propertyName, Object value) {
		if (value != null) {
			entity.setUnindexedProperty(propertyName, value);
		}
	}

	/**
	 * Set a UUID property to an entity.
	 * @param entity the entity to add the UUID to
	 * @param propertyName name of the property
	 * @param uuid the UUID to add to the entity
	 */
	public static void setProperty(Entity entity, String propertyName, UUID uuid) {
		if (uuid != null) {
			entity.setProperty(propertyName + "-least", uuid.getLeastSignificantBits());
			entity.setProperty(propertyName + "-most", uuid.getMostSignificantBits());
		}
	}

	/**
	 * Set a byte array property to an entity
	 * @param entity the entity to add the byte array to
	 * @param propertyName name of the property
	 * @param bytes the byte array to add to the entity
	 */
	public static void setUnindexedProperty(Entity entity, String propertyName, byte[] bytes) {
		if (bytes != null) {
			if (bytes.length <= SHORT_BLOG_MAX_SIZE) {
				ShortBlob blob = new ShortBlob(bytes);
				entity.setProperty(propertyName, blob);
			} else {
				Blob blob = new Blob(bytes);
				entity.setProperty(propertyName, blob);
			}
		}
	}

	/**
	 * Get a UUID property from an entity
	 * @param entity the entity to get the UUID from
	 * @param propertyName name of the property
	 * @return Stored UUID, null if it doesn't exist
	 */
	public static UUID getUuidProperty(Entity entity, String propertyName) {
		if (entity.hasProperty(propertyName + "-least") && entity.hasProperty(propertyName + "-most")) {
			int leastBits = (int) ((long) entity.getProperty(propertyName + "-least"));
			int mostBits = (int) ((long) entity.getProperty(propertyName + "-most"));
			return new UUID(mostBits, leastBits);
		}

		return null;
	}

	/**
	 * Get a byte array from an entity
	 * @param entity the entity to get the byte array from
	 * @param propertyName name of the property
	 * @return stored byte array, null if wasn't set
	 */
	public static byte[] getByteArrayProperty(Entity entity, String propertyName) {
		Object blob = entity.getProperty(propertyName);

		if (blob instanceof ShortBlob) {
			return ((ShortBlob) blob).getBytes();
		}
		else if (blob instanceof Blob) {
			return ((Blob) blob).getBytes();
		}

		return null;
	}

	/** Datastore service */
	public static DatastoreService mDatastore = DatastoreServiceFactory.getDatastoreService();
	/** Logger */
	private static final Logger mLogger = Logger.getLogger(DatastoreUtils.class.getName());

	/** Short blob maximum size */
	private static final int SHORT_BLOG_MAX_SIZE = 500;
}
