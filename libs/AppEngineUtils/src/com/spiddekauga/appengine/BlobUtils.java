package com.spiddekauga.appengine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

/**
 * Various utilities for handling blobs
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class BlobUtils {
	/**
	 * Get all uploaded blob keys from a request
	 * @param request the request send to the servlet
	 * @return Map with all blob keys mapped to a UUID, null if no uploads were made
	 */
	public static Map<UUID, BlobKey> getBlobKeysFromUpload(HttpServletRequest request) {
		HashMap<UUID, BlobKey> blobKeys = new HashMap<>();

		try {
			Map<String, List<BlobKey>> map = mBlobstore.getUploads(request);
			for (Entry<String, List<BlobKey>> entry : map.entrySet()) {
				blobKeys.put(UUID.fromString(entry.getKey()), entry.getValue().get(0));
			}
		} catch (IllegalStateException e) {
			mLogger.fine("No blob uploads found");
			return null;
		}

		return blobKeys;
	}

	/**
	 * Get all uploaded blob keys from a request. The resource uploaded are resource
	 * revisions
	 * @param request the request send to the servlet
	 * @return Map with all blob keys mapped to a revision that is mapped to a UUID.
	 */
	public static Map<UUID, Map<Integer, BlobKey>> getBlobKeysFromUploadRevision(HttpServletRequest request) {
		Map<UUID, Map<Integer, BlobKey>> resources = new HashMap<>();

		try {
			Map<String, List<BlobKey>> map = mBlobstore.getUploads(request);
			for (Entry<String, List<BlobKey>> entry : map.entrySet()) {

				String fieldName = entry.getKey();
				String[] splitFieldName = fieldName.split("_");

				if (splitFieldName.length == 2) {
					try {
						UUID uuid = UUID.fromString(splitFieldName[0]);
						Integer revision = Integer.valueOf(splitFieldName[1]);

						// Get resource id
						Map<Integer, BlobKey> blobKeys = resources.get(uuid);

						if (blobKeys == null) {
							blobKeys = new HashMap<>();
							resources.put(uuid, blobKeys);
						}

						blobKeys.put(revision, entry.getValue().get(0));

					} catch (Exception e) {
						mLogger.severe("Failed to convert field name");
						e.printStackTrace();
					}
				}
			}
		} catch (IllegalStateException e) {
			mLogger.fine("No blob uploads found");
			return null;
		}

		return resources;
	}

	/**
	 * Delete the specified blobs
	 * @param blobKeys keys to the blobs to delete
	 */
	public static void delete(BlobKey... blobKeys) {
		mBlobstore.delete(blobKeys);
	}

	/**
	 * Delete all specified blobs
	 * @param blobKeys keys to the blobs to delete
	 */
	public static void delete(List<BlobKey> blobKeys) {
		// Convert to array
		BlobKey[] array = new BlobKey[blobKeys.size()];
		blobKeys.toArray(array);
		mBlobstore.delete(array);
	}

	/** Blobstore service */
	private static final BlobstoreService mBlobstore = BlobstoreServiceFactory.getBlobstoreService();
	/** Logger */
	private static final Logger mLogger = Logger.getLogger(BlobUtils.class.getName());
}
