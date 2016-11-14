package com.spiddekauga.voider.repo;

/**
 * Base class for repositories
 */
public abstract class Repo implements IResponseListener {
/**
 * Add add an element to the front of an array
 * @param array the original array
 * @param element the element to add
 * @return new array with the element at the front
 */
protected static IResponseListener[] addToFront(IResponseListener[] array, IResponseListener element) {
	IResponseListener[] newArray = new IResponseListener[array.length + 1];
	newArray[0] = element;
	System.arraycopy(array, 0, newArray, 1, array.length);
	return newArray;
}
}
