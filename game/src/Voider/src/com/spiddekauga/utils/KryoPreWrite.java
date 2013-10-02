package com.spiddekauga.utils;

/**
 * When used together with #SerializableTaggedFieldSerializer which calls
 * {@link #preWrite()}.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface KryoPreWrite {
	/**
	 * Called before writing starts to Kryo starts
	 */
	void preWrite();
}
