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
	public static int computePreviousIndex(final List<?> array, int index) {
		return index == 0 ? array.size() - 1 : index - 1;
	}

	/**
	 * Computes the next index of an array, i.e. it wraps the index from back to front if needed.
	 * @param array the array to wrap
	 * @param index calculates the next index of this
	 * @return next index
	 */
	public static int computeNextIndex(final List<?> array, int index) {
		return index == array.size() - 1 ? 0 : index + 1;
	}
}
