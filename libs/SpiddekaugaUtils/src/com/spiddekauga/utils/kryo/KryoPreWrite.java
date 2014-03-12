package com.spiddekauga.utils.kryo;

/**
 * When used together with #SerializableTaggedFieldSerializer which calls
 * {@link #preWrite()}.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface KryoPreWrite {
	/**
	 * Called before writing starts to Kryo starts
	 */
	void preWrite();
}
