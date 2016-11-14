package com.spiddekauga.voider.network.resource;

/**
 * Search ranges for enemy movement speed

 */
@SuppressWarnings("javadoc")
public enum EnemySpeedSearchRanges implements IRangeEnum {
	// !!! IF YOU CHANGE THESE -> Be sure to fix update the search ranges
	SLOWER("Turtle", "1-5", 1, 5),
	SLOW("Slow", "5-10", 5, 10),
	NORMAL("Medium", "10-20", 10, 20),
	FAST("Fast", "20-30", 20, 30),
	FASTER("Ultra Fast", "30+", 30, 1000),

	;

	/**
	 * Creates the enemy speed ranges
	 * @param displayName
	 * @param internalName
	 * @param low lowest possible value
	 * @param high highest possible value
	 */
	private EnemySpeedSearchRanges(String displayName, String internalName, float low, float high) {
		mDisplayName = displayName;
		mLow = low;
		mHigh = high;
		mInternalName = internalName;
	}

	/**
	 * Convert a value into the correct enemy movement speed range category. Will always
	 * return the lowest category first if the value can be in two categories
	 * @param value the value to get a category for
	 * @return the value this category uses. Null if the value doesn't belong to a range
	 */
	public static EnemySpeedSearchRanges getRange(float value) {
		for (EnemySpeedSearchRanges range : values()) {
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
