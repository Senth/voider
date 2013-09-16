package com.spiddekauga.voider.utils;

import java.util.HashSet;
import java.util.List;

import com.badlogic.gdx.utils.ReflectionPool;

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
	 * 
	 * @param type object type
	 * @param initialCapacity how many initial objects will be created
	 */
	public Pool(Class<T> type, int initialCapacity) {
		super(type, initialCapacity);
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
		HashSet<T> freedObjects = new HashSet<T>();
		for (T object : list) {
			if (!freedObjects.contains(object)) {
				free(object);
				freedObjects.add(object);
			}
		}
	}
}
