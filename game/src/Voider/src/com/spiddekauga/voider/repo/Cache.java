package com.spiddekauga.voider.repo;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.utils.Disposable;

/**
 * Cache of a spcific type
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 * @param <Key> key to sort the cache entities by
 * @param <CacheType> type of cache
 */
public class Cache<Key, CacheType extends CacheEntity> {
	/**
	 * Add a new, or update an existing cache entity
	 * @param key identifies the cache entity
	 * @param cacheEntity the cache to add
	 */
	public void add(Key key, CacheType cacheEntity) {
		CacheType oldCache = mEntities.put(key, cacheEntity);
		clearCacheEntity(oldCache);

	}

	/**
	 * Get cache for the specified type.
	 * @param key identifies the cache entity
	 * @return cache if it exists, null if it doesn't exist or it has expired
	 */
	public CacheType get(Key key) {
		CacheType cacheEntity = mEntities.get(key);

		if (cacheEntity != null && cacheEntity.isOutdated()) {
			clearCacheEntity(cacheEntity);
			cacheEntity = null;
		}

		return cacheEntity;
	}

	/**
	 * Clear cache entity
	 * @param cacheEntity
	 */
	private void clearCacheEntity(CacheType cacheEntity) {
		if (cacheEntity instanceof Disposable) {
			((Disposable) cacheEntity).dispose();
		}
	}

	/**
	 * Clear cache
	 */
	public void clear() {
		for (Entry<Key, CacheType> entry : mEntities.entrySet()) {
			CacheType cacheEntity = entry.getValue();
			clearCacheEntity(cacheEntity);
		}
		mEntities.clear();
	}

	/** Cashe entities */
	private HashMap<Key, CacheType> mEntities = new HashMap<>();
}
