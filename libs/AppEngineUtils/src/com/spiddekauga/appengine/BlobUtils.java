package com.spiddekauga.appengine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

/**
 * Various utilities for handling blobs
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BlobUtils {
	/**
	 * Get all uploaded blob keys from a request
	 * @param request the request send to the servlet
	 * @return ArrayList with all blob keys that were uploaded
	 */
	public static List<BlobKey> getBlobKeysFromUpload(HttpServletRequest request) {
		ArrayList<BlobKey> blobKeys = new ArrayList<BlobKey>();

		Map<String, List<BlobKey>> map = mBlobstore.getUploads(request);
		for (Entry<String, List<BlobKey>> entry : map.entrySet()) {
			blobKeys.addAll(entry.getValue());
		}

		return blobKeys;
	}

	/** Blobstore service */
	private static final BlobstoreService mBlobstore = BlobstoreServiceFactory.getBlobstoreService();
	//	/** Logger */
	//	private static final Logger mLogger = Logger.getLogger(BlobUtils.class.getName());
}
