package com.spiddekauga.voider.network.resource;

/**
 * Search ranges for level lengths
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public enum LevelLengthSearchRanges implements IRangeEnum {
	// !!! IF YOU CHANGE THESE -> Be sure to fix update the search ranges
	SHORT("0-1 min", "0-1", 0, 60),
	NORMAL("1-3 min", "1-3", 60, 180),
	LONG("3-5 min", "3-5", 180, 300),
	LONGER("5-10 min", "5-10", 300, 600),
	LONGEST("10+ min", "10+", 600, 86400),

	;

	/**
	 * Creates the level length ranges
	 * @param displayName
	 * @param internalName
	 * @param low lowest possible value
	 * @param high highest possible value
	 */
	private LevelLengthSearchRanges(String displayName, String internalName, float low, float high) {
		mDisplayName = displayName;
		mLow = low;
		mHigh = high;
		mInternalName = internalName;
	}

	/**
	 * Convert a value into the correct level length range category. Will always return
	 * the lowest category first if the value can be in two categories
	 * @param value the value to get a category for
	 * @return the value this category uses. Null if the value doesn't belong to a range
	 */
	public static LevelLengthSearchRanges getRange(float value) {
		for (LevelLengthSearchRanges range : values()) {
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
