package com.spiddekauga.voider.servlets.api;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.misc.GetUploadUrlMethod;
import com.spiddekauga.voider.network.misc.GetUploadUrlResponse;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Returns a valid upload url
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class GetUploadUrl extends VoiderServlet {
	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			return null;
		}

		GetUploadUrlResponse methodResponse = new GetUploadUrlResponse();

		if (methodEntity instanceof GetUploadUrlMethod) {
			BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
			methodResponse.uploadUrl = blobstoreService.createUploadUrl("/" + ((GetUploadUrlMethod) methodEntity).redirectMethod);
			mLogger.finest("Upload url: " + methodResponse.uploadUrl);
		}

		return methodResponse;
	}

	/** Logger */
	private Logger mLogger = Logger.getLogger(GetUploadUrl.class.getName());
}