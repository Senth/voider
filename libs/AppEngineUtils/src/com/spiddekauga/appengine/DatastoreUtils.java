package com.spiddekauga.appengine;


import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.ShortBlob;

/**
 * Utilities for Datastore
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class DatastoreUtils {
	/**
	 * Deletes the specified keys
	 * @param keys deletes all the specified keys
	 */
	public static void delete(Key... keys) {
		mDatastore.delete(keys);
	}

	/**
	 * Deletes the specified keys
	 * @param keys deletes all the specified keys
	 */
	public static void delete(Iterable<Key> keys) {
		mDatastore.delete(keys);
	}

	/**
	 * Puts an entity to the datastore. This checks for concurrent modifications
	 * @param entity the entity to put to the datastore
	 * @return key of the entity if put was successful, null otherwise
	 */
	public static Key put(Entity entity) {
		Exception exception;
		Key key = null;
		do {
			exception = null;
			try {
				key = mDatastore.put(entity);
			} catch (ConcurrentModificationException e) {
				exception = e;
			}
		} while (exception != null);

		return key;
	}

	/**
	 * Get all entities with the specified parent
	 * @param searchIn what kind of entity (table) to search in
	 * @param parent search for all entities with this parent
	 * @return a list of all found entities with the specified parent
	 */
	public static List<Entity> getEntities(String searchIn, Key parent) {
		Query query = new Query(searchIn, parent);
		PreparedQuery preparedQuery = mDatastore.prepare(query);

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		return preparedQuery.asList(fetchOptions);
	}

	/**
	 * Get all keys with the specified parent
	 * @param searchIn what kind of entity (table) to search in
	 * @param parent search for all entities with this parent
	 * @return a list of all found keys with the specified parent
	 */
	public static List<Key> getKeys(String searchIn, Key parent) {
		Query query = new Query(searchIn, parent);
		query.setKeysOnly();
		PreparedQuery preparedQuery = mDatastore.prepare(query);

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();

		ArrayList<Key> keys = new ArrayList<>();
		for (Entity entity : preparedQuery.asList(fetchOptions)) {
			keys.add(entity.getKey());
		}

		return keys;
	}

	/**
	 * Searches for an existing entity
	 * @param searchIn what kind of entity to search in
	 * @param parent the parent of the entity to find, set to null to skip
	 * @param includes property name and values to search for
	 * @return found entity, null if none or more than 1 was found
	 */
	public static Entity getSingleEntity(String searchIn, Key parent, PropertyWrapper... includes) {
		return getSingleEntity(searchIn, parent, false, includes);
	}

	/**
	 * Searches for an existing entity
	 * @param searchIn what kind of entity to search in
	 * @param parent the parent of the entity to find, set to null to skip
	 * @param onlyKeys will only retrieve keys for the found entity
	 * @param includes property name and values to search for
	 * @return found entity, null if none or more than 1 was found
	 */
	private static Entity getSingleEntity(String searchIn, Key parent, boolean onlyKeys, PropertyWrapper... includes) {
		Query query = new Query(searchIn);

		if (onlyKeys) {
			query.setKeysOnly();
		}

		if (includes != null) {
			ArrayList<Filter> filters = new ArrayList<>();
			for (PropertyWrapper property : includes) {
				Filter filter = null;
				if (property.value instanceof UUID) {
					filter = createUuidFilter(property.name, (UUID) property.value);
				} else if (property.value != null) {
					filter = new Query.FilterPredicate(property.name, FilterOperator.EQUAL, property.value);
				}

				if (filter != null) {
					filters.add(filter);
				}
			}

			if (filters.size() == 1) {
				query.setFilter(filters.get(0));
			} else if (filters.size() > 1) {
				query.setFilter(new Query.CompositeFilter(CompositeFilterOperator.AND, filters));
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
	 * @param includes property name and values to search for
	 * @return found entity, null if none or more than 1 was found
	 */
	public static Entity getSingleEntity(String searchIn, PropertyWrapper... includes) {
		return getSingleEntity(searchIn, null, includes);
	}

	/**
	 * Searches for an existing entity
	 * @param searchIn what kind of entity to search in
	 * @param parent the parent for the entity, set to null to skip using
	 * @param includes the values to search for
	 * @return found key for entity
	 */
	public static Key getSingleKey(String searchIn, Key parent, PropertyWrapper... includes) {
		Entity foundEntity = getSingleEntity(searchIn, parent, true, includes);
		if (foundEntity != null) {
			return foundEntity.getKey();
		}
		return null;
	}

	/**
	 * Searches for an existing entity
	 * @param searchIn what kind of entity to search in
	 * @param includes the values to search for
	 * @return found key for entity
	 */
	public static Key getSingleKey(String searchIn, PropertyWrapper... includes) {
		return getSingleKey(searchIn, null, includes);
	}

	/**
	 * Creates an equal UUID search filter
	 * @param propertyName property name in the entity (column)
	 * @param value the value to search for
	 * @return equal filter for the UUID value
	 */
	public static Filter createUuidFilter(String propertyName, UUID value) {
		Filter least = new FilterPredicate(propertyName + UUID_LEAST_POSTFIX, FilterOperator.EQUAL, value.getLeastSignificantBits());
		Filter most = new FilterPredicate(propertyName + UUID_MOST_POSTFIX, FilterOperator.EQUAL, value.getMostSignificantBits());

		return createCompositeFilter(CompositeFilterOperator.AND, least, most);
	}

	/**
	 * Creates a UUID property projection
	 * @param query the query to add the projection to
	 * @param propertyName property name in the entity (column)
	 */
	public static void createUuidProjection(Query query, String propertyName) {
		query.addProjection(new PropertyProjection(propertyName + UUID_LEAST_POSTFIX, Long.class));
		query.addProjection(new PropertyProjection(propertyName + UUID_MOST_POSTFIX, Long.class));
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
	 * @param includes the values to search for
	 * @return true if the datastore contains the specified entity
	 */
	public static boolean exists(String searchIn, PropertyWrapper... includes) {
		return exists(searchIn, null, includes);
	}

	/**
	 * Searches if an entity exists
	 * @param searchIn what kind of entity to search in
	 * @param parent the parent for the entity, set to null to skip using
	 * @param includes the values to search for
	 * @return true if the datastore contains the specified entity
	 */
	public static boolean exists(String searchIn, Key parent, PropertyWrapper... includes) {
		return getSingleKey(searchIn, parent, includes) != null;
	}

	/**
	 * Searches for an entity with the specified key in blob info
	 * @param idName the key of the blob entity to find
	 * @return blob entity with specified key, null if not found
	 */
	public static Entity getBlobEntityByKey(String idName) {
		Key key = KeyFactory.createKey("__BlobInfo__", idName);
		return getEntityByKey(key);
	}

	/**
	 * Searches for an entity with the specified key
	 * @param idName the key of the entity
	 * @return entity with specified key, null if not found
	 */
	public static Entity getEntityByKey(Key idName) {
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
	 * @param value the object to set as the property value. If null this method does nothing.
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
	 * @param value the object to set as the property value. If null this method does nothing.
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
			entity.setProperty(propertyName + UUID_LEAST_POSTFIX, uuid.getLeastSignificantBits());
			entity.setProperty(propertyName + UUID_MOST_POSTFIX, uuid.getMostSignificantBits());
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
		if (entity.hasProperty(propertyName + UUID_LEAST_POSTFIX) && entity.hasProperty(propertyName + UUID_MOST_POSTFIX)) {
			long leastBits = (long) entity.getProperty(propertyName + UUID_LEAST_POSTFIX);
			long mostBits = (long) entity.getProperty(propertyName + UUID_MOST_POSTFIX);
			return new UUID(mostBits, leastBits);
		}

		return null;
	}

	/**
	 * Get an integer property
	 * @param entity the entity to get the integer from
	 * @param propertyName name of the property
	 * @return stored integer, 0 if it doesn't exist
	 */
	public static int getIntProperty(Entity entity, String propertyName) {
		Long longValue = (Long) entity.getProperty(propertyName);

		if (longValue != null) {
			return longValue.intValue();
		}

		return 0;
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
		} else if (blob instanceof Blob) {
			return ((Blob) blob).getBytes();
		}

		return null;
	}

	/**
	 * Prepare a query
	 * @param query the query to prepare
	 * @return prepared query
	 */
	public static PreparedQuery prepare(Query query) {
		return mDatastore.prepare(query);
	}

	/**
	 * Property wrapper. Contains the property name and value
	 */
	public static class PropertyWrapper {
		/**
		 * Default constructor
		 */
		public PropertyWrapper() {
			// Does nothing
		}

		/**
		 * Sets the name and value of the property
		 * @param name
		 * @param value
		 */
		public PropertyWrapper(
				String name, Object value) {
			this.name = name;
			this.value = value;
		}

		/** Property name */
		public String name = null;
		/** Property value */
		public Object value = null;
	}

	/** UUID least postfix */
	private static final String UUID_LEAST_POSTFIX = "-least";
	/** UUID most postfix */
	private static final String UUID_MOST_POSTFIX = "-most";
	/** Datastore service */
	private static DatastoreService mDatastore = DatastoreServiceFactory.getDatastoreService();
	/** Logger */
	private static final Logger mLogger = Logger.getLogger(DatastoreUtils.class.getName());

	/** Short blob maximum size */
	private static final int SHORT_BLOG_MAX_SIZE = 500;
}
