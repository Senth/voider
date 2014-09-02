package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.stat.Tags;

/**
 * Fetches information about all levels
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelGetAllMethod implements IMethodEntity {
	/** Sorting */
	public SortOrders sort = null;
	/** Cursor to continue from */
	public String nextCursor = null;
	/** All tags that should be included, if empty all tags are used */
	public ArrayList<Tags> tagFilter = null;
	/** Search string or text filter, if null not used */
	public String searchString = null;

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
		LIKES("Likes"),

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
	public String getMethodName() {
		return "level-get-all";
	}
}
