package com.spiddekauga.voider.repo;

/**
 * Base class for repositories
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class Repo implements ICallerResponseListener {
	/**
	 * Add add an element to the front of an array
	 * @param array the original array
	 * @param element the element to add
	 * @return new array with the element at the front
	 */
	protected static ICallerResponseListener[] addToFront(ICallerResponseListener[] array, ICallerResponseListener element) {
		ICallerResponseListener[] newArray = new ICallerResponseListener[array.length + 1];
		newArray[0] = element;
		System.arraycopy(array, 0, newArray, 1, array.length);
		return newArray;
	}
}
