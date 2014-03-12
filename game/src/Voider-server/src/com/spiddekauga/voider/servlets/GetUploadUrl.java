package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.GetUploadUrlMethod;
import com.spiddekauga.voider.network.entities.method.GetUploadUrlMethodResponse;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Returns a valid upload url
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class GetUploadUrl extends VoiderServlet {
	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			return;
		}

		GetUploadUrlMethodResponse methodResponse = new GetUploadUrlMethodResponse();

		byte[] byteEntity = NetworkGateway.getEntity(request);
		IEntity networkEntity = NetworkEntitySerializer.deserializeEntity(byteEntity);

		if (networkEntity instanceof GetUploadUrlMethod) {
			BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
			methodResponse.uploadUrl = blobstoreService.createUploadUrl("/" + ((GetUploadUrlMethod) networkEntity).redirectMethod);
			mLogger.info("Upload url: " + methodResponse.uploadUrl);
		}

		byte[] byteResponse = NetworkEntitySerializer.serializeEntity(methodResponse);
		NetworkGateway.sendResponse(response, byteResponse);
	}

	/** Logger */
	private Logger mLogger = Logger.getLogger(GetUploadUrl.class.getName());
}