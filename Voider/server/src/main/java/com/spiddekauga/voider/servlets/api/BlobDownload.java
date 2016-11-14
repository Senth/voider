package com.spiddekauga.voider.servlets.api;

import java.io.IOException;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Downloads a blob from the server

 */
@SuppressWarnings("serial")
public class BlobDownload extends VoiderServlet {
	@Override
	protected void handleRequest() throws ServletException, IOException {
		String blobKeyString = getRequest().getParameter(P_BLOB_KEY);

		if (blobKeyString != null && !blobKeyString.isEmpty()) {
			BlobKey blobKey = new BlobKey(blobKeyString);
			mBlobstoreService.serve(blobKey, getResponse());
		}
	}

	private static final String P_BLOB_KEY = "blob_key";
	private BlobstoreService mBlobstoreService = BlobstoreServiceFactory.getBlobstoreService();
}
