package com.spiddekauga.prototype;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.GetUploadUrlMethod;
import com.spiddekauga.voider.network.entities.method.GetUploadUrlMethodResponse;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.web.VoiderServlet;

/**
 * Returns a valid upload url
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class GetUploadUrl extends VoiderServlet {
	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		GetUploadUrlMethodResponse methodResponse = new GetUploadUrlMethodResponse();

		if (methodEntity instanceof GetUploadUrlMethod) {
			BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
			methodResponse.uploadUrl = blobstoreService.createUploadUrl("/" + ((GetUploadUrlMethod) methodEntity).redirectMethod);
			mLogger.info("Upload url: " + methodResponse.uploadUrl);
		}

		return methodResponse;
	}

	/** Logger */
	private Logger mLogger = Logger.getLogger(GetUploadUrl.class.getName());
}