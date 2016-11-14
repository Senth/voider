package com.spiddekauga.voider.resources;

/**
 * Prepares writing of a resource. This should be called manually before starting
 * the write. Not to be mixed with KryoPreWrite.
 *

 */
public interface IResourcePrepareWrite {
	/**
	 * Called before a write is occuring. Meaning it should be called
	 * before kryo.writeXXX(...);
	 */
	void prepareWrite();
}
