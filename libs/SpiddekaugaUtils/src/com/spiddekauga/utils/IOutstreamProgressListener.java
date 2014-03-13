package com.spiddekauga.utils;

/**
 * Listens to progress updates in an out stream
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IOutstreamProgressListener {
	/**
	 * Called while writing data
	 * @param mcWrittenBytes number of bytes that has been written
	 * @param mcTotalBytes total number of bytes, -1 if not applicable
	 */
	void handleWrite(long mcWrittenBytes, long mcTotalBytes);
}
