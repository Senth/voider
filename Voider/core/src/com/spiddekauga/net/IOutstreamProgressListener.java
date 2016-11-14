package com.spiddekauga.net;

/**
 * Listens to progress updates in an out stream
 */
public interface IOutstreamProgressListener extends IProgressListener {
/**
 * Called while writing data
 * @param cWrittenBytes number of bytes that has been written
 * @param cTotalBytes total number of bytes, -1 if not applicable
 */
void handleWrite(long cWrittenBytes, long cTotalBytes);
}
