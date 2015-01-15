package com.spiddekauga.appengine;


import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreFailureException;
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
		Exception exception;
		do {
			exception = null;
			try {
				mDatastore.delete(keys);
			} catch (ConcurrentModificationException e) {
				exception = e;
			}
		} while (exception != null);
	}

	/**
	 * Deletes the specified keys
	 * @param keys deletes all the specified keys
	 */
	public static void delete(Iterable<Key> keys) {
		Exception exception;
		do {
			exception = null;
			try {
				mDatastore.delete(keys);
			} catch (ConcurrentModificationException e) {
				exception = e;
			}
		} while (exception != null);
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
	 * Puts several entities to the datastore. This checks for concurrent modifications
	 * @param entities iteratable object with all entities to put in the datastore
	 * @return list of all keys that was put
	 */
	public static List<Key> put(Iterable<Entity> entities) {
		Exception exception;
		List<Key> keys = null;
		do {
			exception = null;
			try {
				keys = mDatastore.put(entities);
			} catch (ConcurrentModificationException e) {
				exception = e;
			}
		} while (exception != null);
		return keys;
	}

	/**
	 * Get all keys with the specified properties
	 * @param searchIn what kind of entity (table) to search in
	 * @param filters all properties to search for
	 * @return an array list of all found entities with the specified parent
	 */
	public static List<Key> getKeys(String searchIn, FilterWrapper... filters) {
		return getKeys(searchIn, null, filters);
	}

	/**
	 * Get all entities (with only keys) with the specified parent and properties
	 * @param searchIn what kind of entity (table) to search in
	 * @param parent search for all entities with this parent
	 * @param filters all properties to search for
	 * @return an array list of all found entities with the specified parent
	 */
	public static List<Key> getKeys(String searchIn, Key parent, FilterWrapper... filters) {
		Query query = new Query(searchIn);

		query.setKeysOnly();

		// Parent
		if (parent != null) {
			query.setAncestor(parent);
		}

		// Search by properties
		setFilterProperties(query, filters);


		PreparedQuery preparedQuery = mDatastore.prepare(query);

		ArrayList<Key> keys = new ArrayList<>();
		for (Entity entity : preparedQuery.asIterable()) {
			keys.add(entity.getKey());
		}

		return keys;
	}

	/**
	 * Get all entities with the specified properties
	 * @param searchIn what kind of entity (table) to search in
	 * @param filters all properties to search for
	 * @return an iterable of all found entities with the specified parent
	 */
	public static Iterable<Entity> getEntities(String searchIn, FilterWrapper... filters) {
		return getEntities(searchIn, null, filters);
	}

	/**
	 * Get all entities with the specified parent and properties
	 * @param searchIn what kind of entity (table) to search in
	 * @param parent search for all entities with this parent
	 * @param filters all properties to search for
	 * @return an iterable of all found entities with the specified parent
	 */
	public static Iterable<Entity> getEntities(String searchIn, Key parent, FilterWrapper... filters) {
		Query query = new Query(searchIn);

		// Parent
		if (parent != null) {
			query.setAncestor(parent);
		}

		setFilterProperties(query, filters);

		PreparedQuery preparedQuery = mDatastore.prepare(query);

		return preparedQuery.asIterable();
	}

	/**
	 * Set filters for a query
	 * @param query the query to set the filters for
	 * @param properties filter properties
	 */
	private static void setFilterProperties(Query query, FilterWrapper[] properties) {
		if (properties != null && properties.length >= 1) {
			query.setFilter(createCompositeFilter(CompositeFilterOperator.AND, properties));
		}
	}

	/**
	 * Counts the rows of the specified table
	 * @param searchIn the kind of entity (table) to search in
	 * @param filters all properties to search for
	 * @return number of rows
	 */
	public static int count(String searchIn, FilterWrapper... filters) {
		return count(searchIn, null, filters);
	}

	/**
	 * Counts the rows of the specified table
	 * @param searchIn the kind of entity (table) to search in
	 * @param parent search for all entities with this parent
	 * @param filters all properties to search for
	 * @return number of rows
	 */
	public static int count(String searchIn, Key parent, FilterWrapper... filters) {
		Query query = new Query(searchIn);
		query.setKeysOnly();

		if (parent != null) {
			query.setAncestor(parent);
		}

		setFilterProperties(query, filters);


		PreparedQuery preparedQuery = mDatastore.prepare(query);
		return preparedQuery.countEntities(FetchOptions.Builder.withDefaults());
	}

	/**
	 * Searches for an existing entity
	 * @param searchIn what kind of entity to search in
	 * @param parent the parent of the entity to find, set to null to skip
	 * @param filters property name and values to search for
	 * @return found entity, null if none or more than 1 was found
	 */
	public static Entity getSingleEntity(String searchIn, Key parent, FilterWrapper... filters) {
		return getSingleEntity(searchIn, parent, false, filters);
	}

	/**
	 * Searches for an existing entity
	 * @param searchIn what kind of entity to search in
	 * @param parent the parent of the entity to find, set to null to skip
	 * @param onlyKeys will only retrieve keys for the found entity
	 * @param filters property name and values to search for
	 * @return found entity, null if none or more than 1 was found
	 */
	private static Entity getSingleEntity(String searchIn, Key parent, boolean onlyKeys, FilterWrapper... filters) {
		Query query = new Query(searchIn);

		if (onlyKeys) {
			query.setKeysOnly();
		}

		setFilterProperties(query, filters);

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
	 * @param filters property name and values to search for
	 * @return found entity, null if none or more than 1 was found
	 */
	public static Entity getSingleEntity(String searchIn, FilterWrapper... filters) {
		return getSingleEntity(searchIn, null, filters);
	}

	/**
	 * Searches for an existing entity
	 * @param searchIn what kind of entity to search in
	 * @param parent the parent for the entity, set to null to skip using
	 * @param filters the values to search for
	 * @return found key for entity
	 */
	public static Key getSingleKey(String searchIn, Key parent, FilterWrapper... filters) {
		Entity foundEntity = getSingleEntity(searchIn, parent, true, filters);
		if (foundEntity != null) {
			return foundEntity.getKey();
		}
		return null;
	}

	/**
	 * Searches for an existing entity
	 * @param searchIn what kind of entity to search in
	 * @param filters the values to search for
	 * @return found key for entity
	 */
	public static Key getSingleKey(String searchIn, FilterWrapper... filters) {
		return getSingleKey(searchIn, null, filters);
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
	 * Creates a composite filter out of the specified filters
	 * @param operator composite filter operator (AND/OR)
	 * @param filters all the filters to add
	 * @return a composite filter with the specified filters
	 */
	public static Filter createCompositeFilter(CompositeFilterOperator operator, FilterWrapper... filters) {
		if (filters != null) {
			ArrayList<Filter> datastoreFilters = new ArrayList<>();
			for (FilterWrapper property : filters) {
				Filter filter = null;
				if (property.value instanceof UUID) {
					filter = createUuidFilter(property.name, (UUID) property.value);
				} else if (property.value != null) {
					filter = new Query.FilterPredicate(property.name, property.operator, property.value);
				}

				if (filter != null) {
					datastoreFilters.add(filter);
				}
			}

			if (datastoreFilters.size() == 1) {
				return datastoreFilters.get(0);
			} else if (datastoreFilters.size() > 1) {
				return new Query.CompositeFilter(operator, datastoreFilters);
			}
		}

		return null;
	}

	/**
	 * Searches if an entity exists
	 * @param searchIn what kind of entity to search in
	 * @param includes the values to search for
	 * @return true if the datastore contains the specified entity
	 */
	public static boolean exists(String searchIn, FilterWrapper... includes) {
		return exists(searchIn, null, includes);
	}

	/**
	 * Searches if an entity exists
	 * @param searchIn what kind of entity to search in
	 * @param parent the parent for the entity, set to null to skip using
	 * @param includes the values to search for
	 * @return true if the datastore contains the specified entity
	 */
	public static boolean exists(String searchIn, Key parent, FilterWrapper... includes) {
		return getSingleKey(searchIn, parent, includes) != null;
	}

	/**
	 * Searches for an entity with the specified key in blob info
	 * @param idName the key of the blob entity to find
	 * @return blob entity with specified key, null if not found
	 */
	public static Entity getBlobEntityByKey(String idName) {
		Key key = KeyFactory.createKey("__BlobInfo__", idName);
		return getEntity(key);
	}

	/**
	 * Searches for an entity with the specified key
	 * @param idName the key of the entity
	 * @return entity with specified key, null if not found
	 */
	public static Entity getEntity(Key idName) {
		try {
			return mDatastore.get(idName);
		} catch (EntityNotFoundException e) {
			mLogger.warning("Could not find entity with key: " + idName);
			return null;
		}
	}

	/**
	 * Gets all entities for this key as long as they exist in the datastore
	 * @param keys all entity keys
	 * @return all entities that were found with the specified keys
	 */
	public static Map<Key, Entity> getEntities(Iterable<Key> keys) {
		try {
			return mDatastore.get(keys);
		} catch (IllegalArgumentException | DatastoreFailureException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Set property to an entity, but only if it's not null
	 * @param entity the entity to set the property in
	 * @param propertyName name of the property
	 * @param value the object to set as the property value. If null this method does
	 *        nothing.
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
	 * @param value the object to set as the property value. If null this method does
	 *        nothing.
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
	public static void setUnindexedProperty(Entity entity, String propertyName, UUID uuid) {
		if (uuid != null) {
			entity.setUnindexedProperty(propertyName + UUID_LEAST_POSTFIX, uuid.getLeastSignificantBits());
			entity.setUnindexedProperty(propertyName + UUID_MOST_POSTFIX, uuid.getMostSignificantBits());
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
	public static class FilterWrapper {
		/**
		 * Default constructor
		 */
		public FilterWrapper() {
			this(null, null);
		}

		/**
		 * Sets the name and value of the property. Uses default operator
		 * FilterOperator.EQUAL
		 * @param name
		 * @param value
		 */
		public FilterWrapper(String name, Object value) {
			this(name, FilterOperator.EQUAL, value);
		}

		/**
		 * Sets the name and value of the property
		 * @param name
		 * @param operator the operator to use
		 * @param value
		 */
		public FilterWrapper(String name, FilterOperator operator, Object value) {
			this.name = name;
			this.value = value;
			this.operator = operator;
		}

		/** Property name */
		public String name;
		/** Property value */
		public Object value;
		/** Property operator */
		public FilterOperator operator;
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
