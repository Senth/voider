package com.spiddekauga.voider.resources;

/**
 * Prepares writing of a resource. This should be called manually before starting
 * the write. Not to be mixed with KryoPreWrite.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResourcePrepareWrite {
	/**
	 * Called before a write is occuring. Meaning it should be called
	 * before kryo.writeXXX(...);
	 */
	void prepareWrite();
}
