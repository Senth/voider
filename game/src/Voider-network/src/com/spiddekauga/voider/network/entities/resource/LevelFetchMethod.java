package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.stat.Tags;

/**
 * Fetches information about all levels
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelFetchMethod extends FetchMethod {
	/** Sorting */
	public SortOrders sort = null;
	/** All tags that should be included, if empty all tags are used */
	public ArrayList<Tags> tags = new ArrayList<>();
	/** Search string or text filter, if null not used */
	public String searchString = null;
	/** Level length categories to search for */
	public ArrayList<LevelLengthSearchRanges> levelLengths = new ArrayList<>();
	/** Level speeds to search for */
	public ArrayList<LevelSpeedSearchRanges> levelSpeeds = new ArrayList<>();

	/**
	 * Creates a copy of this method
	 * @return a copy of this method
	 */
	public LevelFetchMethod copy() {
		LevelFetchMethod copy = new LevelFetchMethod();

		copy.sort = sort;
		copy.tags.addAll(tags);
		copy.searchString = searchString;
		copy.levelLengths.addAll(levelLengths);
		copy.levelSpeeds.addAll(levelSpeeds);

		return copy;
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
		BOOKMARKS("Bookmark count"),

		;

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

		/** Human readable name */
		private String mName;
	}

	@Override
	public MethodNames getMethodName() {
		return MethodNames.LEVEL_FETCH;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
}
