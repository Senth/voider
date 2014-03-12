package com.spiddekauga.utils.kryo;

/**
 * Called after Kryo has written something if the class is serialized by
 * SerializableTaggedFieldSerializer
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface KryoPostWrite {
	/**
	 * Called after Kryo has written the object
	 */
	void postWrite();
}
