package com.spiddekauga.voider.servlets.api;

import java.io.IOException;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.misc.BlobDownloadMethod;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Downloads a blob from the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BlobDownload extends VoiderApiServlet {
	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (methodEntity instanceof BlobDownloadMethod) {
			BlobKey blobKey = new BlobKey(((BlobDownloadMethod) methodEntity).blobKey);
			mBlobstoreService.serve(blobKey, getResponse());
		}

		return null;
	}

	/** Blob store service */
	private BlobstoreService mBlobstoreService = BlobstoreServiceFactory.getBlobstoreService();
}
