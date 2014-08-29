package com.spiddekauga.voider.repo;


/**
 * Base class for caches
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
abstract class CacheEntity {
	/**
	 * Sets the outdated time for the cache
	 * @param outdated how long time (in seconds) until the cache should be treated as
	 *        outdate
	 */
	protected CacheEntity(long outdated) {
		mOutdatedTime = outdated;
	}

	/** Last updated cache (system time) (seconds) */
	private long mLastUpdated = getCurrentTime();
	/** Time (in seconds) for the cache to be considered outdated */
	private long mOutdatedTime;

	/** Nano seconds to seconds */
	private final static long NANO_TO_SECONDS = 1000000;

	/**
	 * @return true if the cache is outdated
	 */
	boolean isOutdated() {
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

}
