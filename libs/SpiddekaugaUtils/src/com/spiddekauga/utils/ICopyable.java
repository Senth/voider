package com.spiddekauga.utils;

/**
 * Returns a copy of the object
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 * @param <Type> the type to copy and return
 */
public interface ICopyable<Type> {
	/**
	 * @return a copy of the object
	 */
	Type copy();

	/**
	 * Set an existing object to be a copy of this one
	 * @param copy object to be set as a copy
	 */
	void copy(Type copy);
}
