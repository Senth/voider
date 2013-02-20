package com.spiddekauga.utils;

import java.util.Collection;

/**
 * Utilities for collections
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Collections {
	/**
	 * Linearly searches through a collection after the specified object
	 * @param collection the collection to search in
	 * @param searchObject the object to search for, this may not be null!
	 * @return index of the object if found, -1 if not found
	 */
	public static int linearSearch(Collection<?> collection, Object searchObject) {
		int index = 0;
		for (Object currentObject : collection) {
			if (searchObject.equals(currentObject)) {
				return index;
			}
			index++;
		}
		return -1;
	}
}
