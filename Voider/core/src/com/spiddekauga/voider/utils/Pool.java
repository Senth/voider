package com.spiddekauga.voider.utils;

import com.badlogic.gdx.utils.IdentityMap;
import com.badlogic.gdx.utils.ReflectionPool;
import com.spiddekauga.voider.Config;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A pool of objects that can be reused to avoid allocation
 * @param <T> type of pool
 */
public class Pool<T> extends ReflectionPool<T> {

/** Obtained objects */
IdentityMap<T, T> mObtainedObjects = new IdentityMap<>();
/** Debug tests if we're freeing the same object twice */
IdentityMap<T, T> mFreeObjects = new IdentityMap<>();

/**
 * @param type object type
 * @param initialCapacity how many initial objects will be created
 * @param max maximum stored object
 */
public Pool(Class<T> type, int initialCapacity, int max) {
	super(type, initialCapacity, max);
}

/**
 * @param type object type
 * @param initialCapacity how many initial objects will be created
 */
public Pool(Class<T> type, int initialCapacity) {
	super(type, initialCapacity);
}

/**
 * Creates a pool type
 * @param type object type
 */
public Pool(Class<T> type) {
	super(type);
}

/**
 * Obtain several free objects
 * @param list the list to fill with free objects
 * @return list
 */
public List<T> obtain(List<T> list) {
	for (int i = 0; i < list.size(); ++i) {
		list.set(i, obtain());
	}

	return list;
}

@SuppressWarnings("rawtypes")
@Override
public synchronized T obtain() {
	T object = super.obtain();

	// Debug tests for checking duplicate frees
	if (Config.Debug.DEBUG_TESTS) {
		mFreeObjects.remove(object);
		mObtainedObjects.put(object, object);
	}

	// Clear
	if (object instanceof Collection<?>) {
		((Collection) object).clear();
	} else if (object instanceof IdentityMap<?, ?>) {
		((IdentityMap) object).clear();
	} else if (object instanceof Map<?, ?>) {
		((Map) object).clear();
	}

	return object;
}

@Override
public synchronized void free(T object) {
	if (Config.Debug.DEBUG_TESTS) {
		assert !mFreeObjects.containsKey(object);
		assert mObtainedObjects.containsKey(object);
		mFreeObjects.put(object, object);
		mObtainedObjects.remove(object);
	}

	super.free(object);
}

/**
 * Obtain several free objects
 * @param array fill this vector with free objects
 * @return array
 */
public T[] obtain(T[] array) {
	for (int i = 0; i < array.length; ++i) {
		array[i] = obtain();
	}

	return array;
}

/**
 * Frees all the objects
 * @param objects all objects to free
 */
@SuppressWarnings("unchecked")
public void freeAll(T... objects) {
	for (T object : objects) {
		free(object);
	}
}

/**
 * Frees all the objects in the object arrays
 * @param arrays array with objects to free
 */
public final void freeAll(@SuppressWarnings("unchecked") T[]... arrays) {
	for (T[] objects : arrays) {
		for (T object : objects) {
			free(object);
		}
	}
}

/**
 * Frees all vectors in the list(s)
 * @param lists all lists with vectors to free
 */
@SafeVarargs
public final void freeAll(List<T>... lists) {
	for (List<T> list : lists) {
		for (T object : list) {
			free(object);
		}
	}
}

/**
 * Frees all vectors in the list. This list can contain duplicates as this method ensure that each
 * vector is only freed once.
 * @param list list with vectors to free, can contain duplicates.
 */
public final void freeDuplicates(List<T> list) {
	IdentityMap<T, T> freedObjects = new IdentityMap<>();

	for (T object : list) {
		if (!freedObjects.containsKey(object)) {
			free(object);
			freedObjects.put(object, object);
		}
	}
}
}
