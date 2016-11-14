package com.spiddekauga.voider.network.resource;

import com.spiddekauga.voider.network.stat.Tags;

import java.util.ArrayList;

/**
 * Fetches information about all levels
 */
public class LevelFetchMethod extends FetchMethod {
/** Sorting */
public SortOrders sort = SortOrders.NEWEST;
/** All tags that should be included, if empty all tags are used */
public ArrayList<Tags> tags = new ArrayList<>();
/** Search string or text filter, if null not used */
public String searchString = "";
/** Level length categories to search for */
public ArrayList<LevelLengthSearchRanges> levelLengths = new ArrayList<>();
/** Level speeds to search for */
public ArrayList<LevelSpeedSearchRanges> levelSpeeds = new ArrayList<>();
/** If we should search and use the various filters, or sort (with tag filters) */
public boolean search = false;

/**
 * Creates a copy of this method
 * @return a copy of this method
 */
public LevelFetchMethod copy() {
	LevelFetchMethod copy = new LevelFetchMethod();

	copy.search = search;
	copy.sort = sort;
	copy.tags.addAll(tags);
	copy.searchString = searchString;
	copy.levelLengths.addAll(levelLengths);
	copy.levelSpeeds.addAll(levelSpeeds);

	return copy;
}

@Override
public MethodNames getMethodName() {
	return MethodNames.LEVEL_FETCH;
}

@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (search ? 1 : 0);
	result = prime * result + getListHashCode(levelLengths, LevelLengthSearchRanges.values().length);
	result = prime * result + getListHashCode(levelSpeeds, LevelSpeedSearchRanges.values().length);
	result = prime * result + ((searchString == null) ? 0 : searchString.hashCode());
	result = prime * result + ((sort == null) ? 0 : sort.hashCode());
	result = prime * result + ((tags == null) ? 0 : tags.hashCode());
	return result;
}

@Override
public boolean equals(Object obj) {
	if (this == obj) {
		return true;
	}
	if (obj == null) {
		return false;
	}
	if (getClass() != obj.getClass()) {
		return false;
	}
	LevelFetchMethod other = (LevelFetchMethod) obj;
	if (search != other.search) {
		return false;
	}
	if (!isListEquals(levelLengths, other.levelLengths, LevelLengthSearchRanges.values().length)) {
		return false;
	}
	if (!isListEquals(levelSpeeds, other.levelSpeeds, LevelSpeedSearchRanges.values().length)) {
		return false;
	}
	if (searchString == null) {
		if (other.searchString != null) {
			return false;
		}
	} else if (!searchString.equals(other.searchString)) {
		return false;
	}
	if (sort != other.sort) {
		return false;
	}
	if (tags == null) {
		if (other.tags != null) {
			return false;
		}
	} else if (!tags.equals(other.tags)) {
		return false;
	}
	return true;
}

/**
 * Types of levels to get, or rather sort by
 */
public enum SortOrders {
	/** Newest */
	NEWEST("New"),
	/** Rating */
	RATING("Rating"),
	/** Number of plays */
	PLAYS("Plays"),
	/** Number of likes */
	BOOKMARKS("Bookmark count"),;

	/** Human readable name */
	private String mName;

	/**
	 * Constructor with a human readable name
	 * @param name human readable name
	 */
	private SortOrders(String name) {
		mName = name;
	}

	@Override
	public String toString() {
		return mName;
	}
}
}
