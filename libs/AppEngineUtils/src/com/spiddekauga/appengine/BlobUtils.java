package com.spiddekauga.appengine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

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
	 * @return Map with all blob keys mapped to a UUID
	 */
	public static Map<UUID, BlobKey> getBlobKeysFromUpload(HttpServletRequest request) {
		HashMap<UUID, BlobKey> blobKeys = new HashMap<>();

		Map<String, List<BlobKey>> map = mBlobstore.getUploads(request);
		for (Entry<String, List<BlobKey>> entry : map.entrySet()) {
			blobKeys.put(UUID.fromString(entry.getKey()), entry.getValue().get(0));
		}

		return blobKeys;
	}

	/** Blobstore service */
	private static final BlobstoreService mBlobstore = BlobstoreServiceFactory.getBlobstoreService();
	//	/** Logger */
	//	private static final Logger mLogger = Logger.getLogger(BlobUtils.class.getName());
}
