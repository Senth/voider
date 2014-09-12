package com.spiddekauga.voider.repo;

import com.spiddekauga.utils.ICopyable;


/**
 * Base class for caches
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 * @param <EntityType> What type of subclass is stored
 */
public abstract class CacheEntity<EntityType> implements ICopyable<EntityType> {
	/**
	 * Sets the outdated time for the cache
	 * @param outdated how long time (in seconds) until the cache should be treated as
	 *        outdated
	 */
	protected CacheEntity(long outdated) {
		mOutdatedTime = outdated;
	}

	/**
	 * @return true if the cache is outdated
	 */
	public boolean isOutdated() {
		return mLastUpdated + mOutdatedTime < getCurrentTime();
	}

	/**
	 * @return current time in seconds
	 */
	private static long getCurrentTime() {
		return System.nanoTime() / NANO_TO_SECONDS;
	}

	/**
	 * Update cache time to current
	 */
	protected void updateCacheTime() {
		mLastUpdated = getCurrentTime();
	}

	@Override
	public void copy(EntityType copy) {
		if (copy instanceof CacheEntity<?>) {
			CacheEntity<?> cacheCopy = (CacheEntity<?>) copy;
			cacheCopy.mLastUpdated = mLastUpdated;
			cacheCopy.mOutdatedTime = mOutdatedTime;
		}
	}

	/** Last updated cache (system time) (seconds) */
	private long mLastUpdated = getCurrentTime();
	/** Time (in seconds) for the cache to be considered outdated */
	private long mOutdatedTime;

	/** Nano seconds to seconds */
	private final static long NANO_TO_SECONDS = 1000000;
}
