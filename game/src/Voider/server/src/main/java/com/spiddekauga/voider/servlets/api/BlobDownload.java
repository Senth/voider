package com.spiddekauga.voider.servlets.api;

import java.io.IOException;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.misc.BlobDownloadMethod;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Downloads a blob from the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BlobDownload extends VoiderApiServlet<BlobDownloadMethod> {
	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(BlobDownloadMethod method) throws ServletException, IOException {
		BlobKey blobKey = new BlobKey(method.blobKey);
		mBlobstoreService.serve(blobKey, getResponse());

		return null;
	}

	@Override
	protected boolean isHandlingRequestDuringMaintenance() {
		return true;
	}


	/** Blob store service */
	private BlobstoreService mBlobstoreService = BlobstoreServiceFactory.getBlobstoreService();
}
