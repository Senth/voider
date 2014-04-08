package com.spiddekauga.voider.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.BlobDownloadMethod;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Downloads a blob from the server
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BlobDownload extends VoiderServlet {
	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		byte[] byteEntity = NetworkGateway.getEntity(request);
		IEntity networkEntity = NetworkEntitySerializer.deserializeEntity(byteEntity);

		if (networkEntity instanceof BlobDownloadMethod) {
			BlobKey blobKey = new BlobKey(((BlobDownloadMethod) networkEntity).blobKey);
			mBlobstoreService.serve(blobKey, response);
		}
	}

	/** Blob store service */
	private BlobstoreService mBlobstoreService = BlobstoreServiceFactory.getBlobstoreService();
}
