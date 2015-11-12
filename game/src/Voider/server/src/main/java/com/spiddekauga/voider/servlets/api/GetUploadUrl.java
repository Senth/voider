package com.spiddekauga.voider.servlets.api;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.misc.GetUploadUrlMethod;
import com.spiddekauga.voider.network.misc.GetUploadUrlResponse;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Returns a valid upload url
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class GetUploadUrl extends VoiderApiServlet<GetUploadUrlMethod> {
	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(GetUploadUrlMethod method) throws ServletException, IOException {
		GetUploadUrlResponse methodResponse = new GetUploadUrlResponse();

		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		methodResponse.uploadUrl = blobstoreService.createUploadUrl("/" + method.redirectMethod);
		mLogger.finest("Upload url: " + methodResponse.uploadUrl);

		return methodResponse;
	}

	@Override
	protected boolean isHandlingRequestDuringMaintenance() {
		return true;
	}

	/** Logger */
	private Logger mLogger = Logger.getLogger(GetUploadUrl.class.getName());
}