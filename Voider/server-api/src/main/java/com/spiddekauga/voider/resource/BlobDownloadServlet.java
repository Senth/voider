package com.spiddekauga.voider.resource;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.spiddekauga.voider.server.util.VoiderServlet;

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * Downloads a blob from the server
 */
@SuppressWarnings("serial")
public class BlobDownloadServlet extends VoiderServlet {
private static final String P_BLOB_KEY = "blob_key";
private BlobstoreService mBlobstoreService = BlobstoreServiceFactory.getBlobstoreService();

@Override
protected void onInit() throws ServletException, IOException {
	super.onInit();
	setHandlesRequestDuringMaintenance(true);
}

@Override
protected void onRequest() throws ServletException, IOException {
	String blobKeyString = getRequest().getParameter(P_BLOB_KEY);

	if (blobKeyString != null && !blobKeyString.isEmpty()) {
		BlobKey blobKey = new BlobKey(blobKeyString);
		mBlobstoreService.serve(blobKey, getResponse());
	}
}
}
