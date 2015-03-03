package org.apache.http.entity.mime;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.spiddekauga.net.IOutstreamProgressListener;
import com.spiddekauga.net.OutputStreamProgress;


/**
 * Wrapper for MultipartFortEntity that handles the upload progress
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class MultipartFormEntityWithProgress extends MultipartFormEntity {

	/**
	 * @param multipart
	 * @param contentType
	 * @param contentLength
	 * @param progressListeners all progress listeners
	 */
	MultipartFormEntityWithProgress(AbstractMultipartForm multipart, String contentType, long contentLength, List<IOutstreamProgressListener> progressListeners) {
		super(multipart, contentType, contentLength);
		mProgressListeners = progressListeners;
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		OutputStream outputStream = new OutputStreamProgress(outstream, getContentLength(), mProgressListeners);
		super.writeTo(outputStream);
	}

	/** Progress listeners */
	List<IOutstreamProgressListener> mProgressListeners;
}
