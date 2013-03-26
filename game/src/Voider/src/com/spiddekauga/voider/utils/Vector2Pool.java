package com.spiddekauga.voider.utils;

import java.util.HashSet;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/**
 * Pool for Vector2. The pool is the same pool you get from Pools.get(Vector2.class). But
 * can be obtained in constant time.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Vector2Pool {
	/**
	 * Creates a new vector when necessary, otherwise it will reuse an old vector
	 * that has been freed
	 * @return vector that can be used
	 */
	public static Vector2 obtain() {
		return mPool.obtain();
	}

	/**
	 * Frees an allocated vector to be reused. Don't use the vector after this.
	 * @param vector the vector to free
	 */
	public static void free(Vector2 vector) {
		mPool.free(vector);
	}

	/**
	 * Frees all vectors in the list
	 * @param list list with vectors to free
	 */
	public static void free(List<Vector2> list) {
		for (Vector2 vector : list) {
			free(vector);
		}
	}

	/**
	 * Frees all vectors in the list. This list can contain duplicates
	 * as this method ensure that each vector is only freed once.
	 * @param list list with vectors to free, can contain duplicates.
	 */
	public static void freeDuplicates(List<Vector2> list) {
		HashSet<Vector2> freedVectors = new HashSet<Vector2>();
		for (Vector2 vector : list) {
			if (!freedVectors.contains(vector)) {
				free(vector);
				freedVectors.add(vector);
			}
		}
	}

	/** Pool for vector 2 */
	private final static Pool<Vector2> mPool = Pools.get(Vector2.class);
}
