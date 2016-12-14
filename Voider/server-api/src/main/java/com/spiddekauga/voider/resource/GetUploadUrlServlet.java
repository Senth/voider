package com.spiddekauga.voider.resource;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.misc.GetUploadUrlMethod;
import com.spiddekauga.voider.network.misc.GetUploadUrlResponse;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;

/**
 * Returns a valid upload url
 */
@SuppressWarnings("serial")
public class GetUploadUrlServlet extends VoiderApiServlet<GetUploadUrlMethod> {
/** Logger */
private Logger mLogger = Logger.getLogger(GetUploadUrlServlet.class.getName());

@Override
protected void onInit() throws ServletException, IOException {
	super.onInit();
	setHandlesRequestDuringMaintenance(true);
}

@Override
protected IEntity onRequest(GetUploadUrlMethod method) throws ServletException, IOException {
	GetUploadUrlResponse methodResponse = new GetUploadUrlResponse();

	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	methodResponse.uploadUrl = blobstoreService.createUploadUrl("/" + method.redirectMethod);
	mLogger.finest("Upload url: " + methodResponse.uploadUrl);

	return methodResponse;
}
}