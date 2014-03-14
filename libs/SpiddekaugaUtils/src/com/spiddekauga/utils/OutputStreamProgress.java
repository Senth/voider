package com.spiddekauga.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for an output stream with progress
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class OutputStreamProgress extends OutputStream {
	/**
	 * @param outputStream original output stream to wrap
	 * @param progressListeners all progress listeners
	 */
	public OutputStreamProgress(OutputStream outputStream, List<IOutstreamProgressListener> progressListeners) {
		this(outputStream, -1L, progressListeners);
	}

	/**
	 * @param outputStream original output stream to wrap
	 * @param progressListeners all progress listeners
	 */
	public OutputStreamProgress(OutputStream outputStream, IOutstreamProgressListener... progressListeners) {
		this(outputStream, -1L, progressListeners);
	}

	/**
	 * @param outputStream original output stream to wrap
	 * @param cTotalBytes total byte count, -1 if not known
	 * @param progressListeners all progress listeners
	 */
	public OutputStreamProgress(OutputStream outputStream, Long cTotalBytes, List<IOutstreamProgressListener> progressListeners) {
		mOutstream = outputStream;
		mProgressListeners = progressListeners;
		mcTotalBytes = cTotalBytes;
	}

	/**
	 * @param outputStream original output stream to wrap
	 * @param cTotalBytes total byte count, -1 if not known
	 * @param progressListeners all progress listeners
	 */
	public OutputStreamProgress(OutputStream outputStream, Long cTotalBytes, IOutstreamProgressListener... progressListeners) {
		mOutstream = outputStream;
		mcTotalBytes = cTotalBytes;

		if (progressListeners.length > 0) {
			mProgressListeners = new ArrayList<>();
			for (IOutstreamProgressListener listener : progressListeners) {
				if (listener != null) {
					mProgressListeners.add(listener);
				}
			}
		}
	}

	@Override
	public void write(int b) throws IOException {
		mOutstream.write(b);
		mcWrittenBytes++;
		sendWriteEvent();
	}

	@Override
	public void write(byte[] b) throws IOException {
		mOutstream.write(b);
		mcWrittenBytes += b.length;
		sendWriteEvent();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		mOutstream.write(b, off, len);
		mcWrittenBytes += len;
		sendWriteEvent();
	}

	/**
	 * Send write event to listeners
	 */
	private void sendWriteEvent() {
		if (mProgressListeners != null) {
			for (IOutstreamProgressListener listener : mProgressListeners) {
				listener.handleWrite(mcWrittenBytes, mcTotalBytes);
			}
		}
	}

	@Override
	public void flush() throws IOException {
		mOutstream.flush();
	}

	@Override
	public void close() throws IOException {
		mOutstream.close();
	}


	/** Original output stream */
	private final OutputStream mOutstream;
	/** Total bytes written */
	private long mcWrittenBytes = 0;
	/** Total bytes */
	private final long mcTotalBytes;
	/** Progress listeners */
	private List<IOutstreamProgressListener> mProgressListeners = null;
}
