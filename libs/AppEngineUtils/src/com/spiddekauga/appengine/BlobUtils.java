package com.spiddekauga.appengine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

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
			mLogger.info("Blob name: " + entry.getKey());
			blobKeys.addAll(entry.getValue());
		}


		return blobKeys;
	}
	//	public static ArrayList<BlobKey> getBlobKeysFromUpload(HttpServletRequest request) {
	//		ArrayList<BlobKey> returnList = new ArrayList<BlobKey>();
	//
	//		// Get file keys from the attribute
	//		HashMap<?, ?> blobNameMap = (HashMap<?,?>) request.getAttribute(BLOB_KEY_ATTRIBUTE_NAME);
	//		@SuppressWarnings("unchecked")
	//		ArrayList<String> blobNames = (ArrayList<String>) blobNameMap.get("fileKey");
	//
	//		// Get the actual blob key from the datastore
	//		for (String blobName : blobNames) {
	//			Entity entity = DatastoreUtils.getItemByKey(blobName);
	//			mLogger.info("Entity:\n"
	//					+ "Key: " + entity.getKey()
	//					+ "\nKind: " + entity.getKind()
	//					+ "\nParent: " + entity.getParent()
	//					+ "\nProperties: " + entity);
	//
	//			if (entity != null) {
	//			} else {
	//				mLogger.warning("Could not find the blob with name: " + blobName);
	//			}
	//		}
	//
	//
	//		//		for (String blobString : blobNames) {
	//		//			BlobKey blobKey = new BlobKey(blobString);
	//		//			returnList.add(blobKey);
	//		//		}
	//
	//		return returnList;
	//	}

	/** Blobstore service */
	private static final BlobstoreService mBlobstore = BlobstoreServiceFactory.getBlobstoreService();
	/** Attribute name for blob keys in a request */
	private static final String BLOB_KEY_ATTRIBUTE_NAME = "com.google.appengine.api.blobstore.upload.blobkeys";
	/** Logger */
	private static final Logger mLogger = Logger.getLogger(BlobUtils.class.getName());
}
