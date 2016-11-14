package com.spiddekauga.voider.network.resource;

/**
 * Search ranges for enemies' bullet speed

 */
@SuppressWarnings("javadoc")
public enum BulletSpeedSearchRanges implements IRangeEnum {
	// !!! IF YOU CHANGE THESE -> Be sure to fix update the search ranges
	SLOW("Slow", "1-10", 1, 10),
	MEDIUM("Medium", "10-20", 10, 20),
	FAST("Fast", "20-30", 20, 30),
	FASTER("Ultra Fast", "30-40", 30, 40),
	FASTEST("Lightning Fast", "40+", 40, 1000),

	;

	/**
	 * Creates the bullet speed ranges
	 * @param displayName
	 * @param internalName
	 * @param low lowest possible value
	 * @param high highest possible value
	 */
	private BulletSpeedSearchRanges(String displayName, String internalName, float low, float high) {
		mDisplayName = displayName;
		mLow = low;
		mHigh = high;
		mInternalName = internalName;
	}

	/**
	 * Convert a value into the correct bullet speed range category. Will always return
	 * the lowest category first if the value can be in two categories
	 * @param value the value to get a category for
	 * @return the value this category uses. Null if the value doesn't belong to a range
	 */
	public static BulletSpeedSearchRanges getRange(float value) {
		for (BulletSpeedSearchRanges range : values()) {
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
