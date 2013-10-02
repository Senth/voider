package com.spiddekauga.utils;

/**
 * Called after Kryo has written something if the class is serialized by
 * SerializableTaggedFieldSerializer
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface KryoPostWrite {
	/**
	 * Called after Kryo has written the object
	 */
	void postWrite();
}
