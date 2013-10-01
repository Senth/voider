package com.spiddekauga.voider.utils;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.ReflectionPool;
import com.spiddekauga.utils.Collections;

/**
 * A pool of objects that can be reused to avoid allocation
 * @param <T> type of pool
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Pool<T> extends ReflectionPool<T> {

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

	//	@Override
	//	public T obtain() {
	//		T object = super.obtain();
	//		if (object instanceof Vector2) {
	//			return (T) new Vector2();
	//		} else {
	//			return object;
	//		}
	//	}

	@Override
	public T obtain() {
		T object = super.obtain();
		if (object instanceof ArrayList) {
			return (T) new ArrayList();
		} else {
			return object;
		}
	}

	/**
	 * Creates a pool type
	 * @param type objct type
	 */
	public Pool(Class<T> type) {
		super(type);
	}

	/**
	 * Frees all the objects
	 * @param objects all objects to free
	 */
	public void freeAll(T... objects) {
		for (T object : objects) {
			free(object);
		}
	}

	/**
	 * Frees all vectors in the list
	 * @param list list with vectors to free
	 */
	public void freeAll(List<T> list) {
		for (T object : list) {
			free(object);
		}
	}

	/**
	 * Frees all vectors in the list. This list can contain duplicates
	 * as this method ensure that each vector is only freed once.
	 * @param list list with vectors to free, can contain duplicates.
	 */
	public void freeDuplicates(List<T> list) {
		@SuppressWarnings("unchecked")
		ArrayList<T> freedObjects = Pools.arrayList.obtain();
		freedObjects.clear();
		for (T object : list) {
			if (!Collections.listContains(freedObjects, object, true)) {
				free(object);
				freedObjects.add(object);
			}
		}
	}
}
