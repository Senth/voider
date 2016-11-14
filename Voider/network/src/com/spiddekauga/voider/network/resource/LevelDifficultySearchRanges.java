package com.spiddekauga.voider.network.resource;

import com.spiddekauga.utils.ISearchStore;

/**
 * Search ranges for level difficulty
 */
@SuppressWarnings("javadoc")
public enum LevelDifficultySearchRanges implements ISearchStore {
	// !!! IF YOU CHANGE THESE -> Be sure to fix update the search ranges
	IMPOSSIBLE(0, 0.01f),
	INSANE(0.01f, 0.05f),
	VERY_HARD(0.05f, 0.1f),
	HARD(0.1f, 0.2f),
	CHALLENGING(0.2f, 0.4f),
	CASUAL(0.4f, 0.6f),
	EASY(0.6f, 0.8f),
	WALK_IN_THE_PARK(0.8f, 1f),;


private String mDisplayName;
private float mLow;
private float mHigh;

/**
 * Creates the level difficulty ranges
 * @param low lowest possible value
 * @param high highest possible value
 */
private LevelDifficultySearchRanges(float low, float high) {
	mDisplayName = name().substring(0, 1) + name().substring(1).toLowerCase().replace("_", " ");
	mLow = low;
	mHigh = high;
}

/**
 * Convert a value into the correct level difficulty range category. Will always return the lowest
 * category first if the value can be in two categories
 * @param value the value to get a category for
 * @return the value this category uses. Null if the value doesn't belong to a range
 */
public static LevelDifficultySearchRanges getRange(float value) {
	for (LevelDifficultySearchRanges range : values()) {
		if (range.mLow <= value && value <= range.mHigh) {
			return range;
		}
	}

	return null;
}

@Override
public String toSearchId() {
	return mDisplayName;
}

@Override
public String toString() {
	return mDisplayName;
}
}
