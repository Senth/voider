package com.spiddekauga.appengine;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.blobstore.BlobKey;

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
	public static ArrayList<BlobKey> getBlobKeysFromUpload(HttpServletRequest request) {
		ArrayList<BlobKey> returnList = new ArrayList<BlobKey>();

		HashMap<?, ?> blobkeyMap = (HashMap<?,?>) request.getAttribute(BLOB_KEY_ATTRIBUTE_NAME);
		@SuppressWarnings("unchecked")
		ArrayList<String> blobStrings = (ArrayList<String>) blobkeyMap.get("fileKey");

		for (String blobString : blobStrings) {
			BlobKey blobKey = new BlobKey(blobString);
			returnList.add(blobKey);
		}

		return returnList;
	}

	/** Attribute name for blob keys in a request */
	private static final String BLOB_KEY_ATTRIBUTE_NAME = "com.google.appengine.api.blobstore.upload.blobkeys";
}
