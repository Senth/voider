package com.spiddekauga.utils;

/**
 * Copies some parts of an already tagged copied object
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface KryoTaggedCopyable {
	/**
	 * Set some field that the tagged fields didn't copy
	 * @param fromOriginal the original object to copy from
	 */
	void copy(Object fromOriginal);
}
