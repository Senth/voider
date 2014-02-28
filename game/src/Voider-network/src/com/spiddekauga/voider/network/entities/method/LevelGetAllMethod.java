package com.spiddekauga.voider.network.entities.method;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.Tags;

/**
 * Fetches information about all levels
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class LevelGetAllMethod implements IMethodEntity {
	/** Sorting */
	public SortTypes sort = null;
	/** Cursor to continue from */
	public String nextCursor = null;
	/** All tags that should be included, if empty all tags are used */
	public ArrayList<Tags> tagFilter = new ArrayList<>();
	/** Search string or text filter, if null not used */
	public String searchString = null;

	/**
	 * Types of levels to get, or rather sort by
	 */
	public enum SortTypes {
		/** Featured */
		FEATURED,
		/** Number of likes */
		LIKES,
		/** Number of plays */
		PLAYS,
		/** Rating */
		RATING,
		/** Newest */
		NEWEST,
		/** Last played */
		LAST_PLAYED,
	}

	@Override
	public String getMethodName() {
		return "level-get-all";
	}
}
