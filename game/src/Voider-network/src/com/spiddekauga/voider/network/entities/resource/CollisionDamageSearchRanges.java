package com.spiddekauga.voider.network.entities.resource;

/**
 * Search ranges for actor collision damage
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public enum CollisionDamageSearchRanges implements IRangeEnum {
	// !!! IF YOU CHANGE THESE -> Be sure to update the search ranges
	LOW("Low", "1-2", 1, 2),
	MEDIUM("Medium", "2-5", 2, 5),
	HIGH("High", "5-10", 5, 10),
	HIGHER("Higher", "10-20", 10, 20),
	HIGHEST("Ultra High", "20-50", 20, 50),
	CRITICAL("Critical", "50+", 50, 1000),

	;

	/**
	 * Creates the bullet damage ranges
	 * @param displayName
	 * @param internalName
	 * @param low lowest possible value
	 * @param high highest possible value
	 */
	private CollisionDamageSearchRanges(String displayName, String internalName, float low, float high) {
		mDisplayName = displayName;
		mLow = low;
		mHigh = high;
		mInternalName = internalName;
	}

	/**
	 * Convert a value into the correct bullet damage range category. Will always return
	 * the lowest category first if the value can be in two categories
	 * @param value the value to get a category for
	 * @return the value this category uses. Null if the value doesn't belong to a range
	 */
	public static CollisionDamageSearchRanges getRange(float value) {
		for (CollisionDamageSearchRanges range : values()) {
			if (range.mLow <= value && value <= range.mHigh) {
				return range;
			}
		}

		return null;
	}

	@Override
	public String toString() {
		return mDisplayName;
	}

	@Override
	public String toSearchId() {
		return mInternalName;
	}

	@Override
	public float getLow() {
		return mLow;
	}

	@Override
	public float getHigh() {
		return mHigh;
	}

	private String mDisplayName;
	private String mInternalName;
	private float mLow;
	private float mHigh;
}
