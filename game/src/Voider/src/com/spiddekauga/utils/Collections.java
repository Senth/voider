package com.spiddekauga.utils;

import java.util.List;

/**
 * Class for common collection operations
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Collections {
	/**
	 * Computes the previous index of an array, i.e. it wraps the index from the front to back if needed.
	 * @param array the array to wrap
	 * @param index calculates the previous index of this
	 * @return previous index
	 */
	public static int previousIndex(final List<?> array, int index) {
		return index == 0 ? array.size() - 1 : index - 1;
	}

	/**
	 * Computes the next index of an array, i.e. it wraps the index from back to front if needed.
	 * @param array the array to wrap
	 * @param index calculates the next index of this
	 * @return next index
	 */
	public static int nextIndex(final List<?> array, int index) {
		return index == array.size() - 1 ? 0 : index + 1;
	}

	/**
	 * Checks if a list contains the specified item. Uses linear search
	 * @param list the list to search in
	 * @param object the object to search for
	 * @param identity set to true to test "object == list.get(i)", false to test "object.equals(list.get(i))"
	 * @return true if list contains object.
	 */
	public static boolean listContains(List<? extends Object> list, Object object, boolean identity) {
		if (identity) {
			for (Object listObject : list) {
				if (listObject == object) {
					return true;
				}
			}
		} else {
			for (Object listObject : list) {
				if (listObject.equals(object)) {
					return true;
				}
			}
		}
		return false;
	}
}
