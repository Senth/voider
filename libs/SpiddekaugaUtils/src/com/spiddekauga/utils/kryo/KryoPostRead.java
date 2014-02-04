package com.spiddekauga.utils.kryo;

/**
 * When used together with #SerializableTaggedFieldSerializer which calls
 * {@link #postRead()}.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface KryoPostRead {
	/**
	 * Called when object has finished reading from kryo
	 */
	void postRead();
}
