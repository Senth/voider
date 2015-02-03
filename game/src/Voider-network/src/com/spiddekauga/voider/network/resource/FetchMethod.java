package com.spiddekauga.voider.network.resource;

import java.util.ArrayList;
import java.util.List;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Common class for fetching stuff from the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public abstract class FetchMethod implements IMethodEntity {
	/** Cursor to continue the search/get from */
	public String nextCursor = null;

	/**
	 * Get the hash code for a specific array list. If the list is the maximum size an
	 * empty list hash will instead be used
	 * @param list the list to check
	 * @param maxSize maximum size of the list
	 * @return hash code for the list
	 */
	protected static int getListHashCode(List<?> list, int maxSize) {
		if (list != null) {
			if (list.size() != maxSize) {
				return list.hashCode();
			} else {
				return EMPTY_LIST_HASH;
			}
		} else {
			return 0;
		}
	}

	/**
	 * Test equals on a list for a specific array. If any of the lists are of maximum
	 * sizes they will be treated as an empty list
	 * @param thisList
	 * @param otherList
	 * @param maxSize
	 * @return true if the lists are equal
	 */
	protected static boolean isListEquals(List<?> thisList, List<?> otherList, int maxSize) {
		if (thisList == null) {
			if (otherList != null) {
				return false;
			}
		} else {
			if (otherList == null) {
				return false;
			} else if (!thisList.equals(otherList)) {
				// Check for max size
				if (!((thisList.isEmpty() || thisList.size() == maxSize) && (otherList.isEmpty() || otherList.size() == maxSize))) {
					return false;
				}
			}
		}

		return true;
	}

	/** Empty list for testing equal and getting hash */
	private static final int EMPTY_LIST_HASH = new ArrayList<>().hashCode();
}
