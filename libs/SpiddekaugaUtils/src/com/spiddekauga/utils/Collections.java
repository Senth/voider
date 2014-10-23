package com.spiddekauga.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class for common collection operations
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Collections {
	/**
	 * Computes the previous index of an array, i.e. it wraps the index from the front to
	 * back if needed.
	 * @param array the array to wrap
	 * @param index calculates the previous index of this
	 * @return previous index
	 */
	public static int previousIndex(final List<?> array, int index) {
		return prevIndex(array.size(), index);
	}

	/**
	 * Computes the previous index of an array, i.e. it wraps the index from the front to
	 * back if needed.
	 * @param size size of the array/list
	 * @param index calculates the previous index of this
	 * @return previous index
	 */
	public static int prevIndex(int size, int index) {
		return index == 0 ? size - 1 : index - 1;
	}

	/**
	 * Computes the next index of an array, i.e. it wraps the index from back to front if
	 * needed.
	 * @param array the array to wrap
	 * @param index calculates the next index of this
	 * @return next index
	 */
	public static int nextIndex(final List<?> array, int index) {
		return nextIndex(array.size(), index);
	}

	/**
	 * Computes the next index of an array, i.e. it wraps the index from back to front if
	 * needed.
	 * @param size the size of the array/list
	 * @param index calculates the next index of this
	 * @return next index
	 */
	public static int nextIndex(int size, int index) {
		return index == size - 1 ? 0 : index + 1;
	}

	/**
	 * Returns all the indices between the specified low and high indices. If you want it
	 * wrapped lowIndex should be higher than highIndex
	 * @param size the size of the list/array
	 * @param lowIndex from this (or from the next)
	 * @param lowInclude set to true to include lowIndex in the returned list
	 * @param highIndex to this (or to the previous)
	 * @param highInclude set to true to include highIndex in the returned list
	 * @return list of all indices between
	 */
	public static ArrayList<Integer> getIndicesBetween(int size, int lowIndex, boolean lowInclude, int highIndex, boolean highInclude) {
		ArrayList<Integer> indices = new ArrayList<>();

		if (lowIndex < highIndex) {
			int startIndex = lowInclude ? lowIndex : lowIndex + 1;
			int endIndex = highInclude ? highIndex : highIndex - 1;
			for (int i = startIndex; i <= endIndex; ++i) {
				indices.add(i);
			}
		}
		// Wrapped
		else {
			int startIndex = highInclude ? highIndex : nextIndex(size, highIndex);
			int endIndex = lowInclude ? nextIndex(size, lowIndex) : lowIndex;
			for (int i = startIndex; i != endIndex; i = nextIndex(size, i)) {
				indices.add(i);
			}
		}

		return indices;
	}

	/**
	 * Checks if a list contains the specified item. Uses linear search
	 * @param list the list to search in
	 * @param object the object to search for
	 * @param identity set to true to test "object == list.get(i)", false to test
	 *        "object.equals(list.get(i))"
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

	/**
	 * Adds all elements to an collection
	 * @param <ElementType> the element type
	 * @param addFrom the collection to add from
	 * @param addTo the collection to add to
	 */
	public static <ElementType> void addAll(ElementType[] addFrom, Collection<ElementType> addTo) {
		for (ElementType element : addFrom) {
			addTo.add(element);
		}
	}

	/**
	 * Adds all elements to an collection
	 * @param <ElementType> the element type
	 * @param addFrom the collection to add from
	 * @param addTo the collection to add to
	 */
	public static <ElementType> void addAll(Iterable<ElementType> addFrom, Collection<ElementType> addTo) {
		for (ElementType element : addFrom) {
			addTo.add(element);
		}
	}
}
