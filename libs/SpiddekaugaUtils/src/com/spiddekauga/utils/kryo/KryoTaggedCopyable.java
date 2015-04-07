package com.spiddekauga.utils.kryo;

/**
 * Copies some parts of an already tagged copied object
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface KryoTaggedCopyable {
	/**
	 * Set some field that the tagged fields didn't copy
	 * @param fromOriginal the original object to copy from
	 */
	void copy(Object fromOriginal);
}
