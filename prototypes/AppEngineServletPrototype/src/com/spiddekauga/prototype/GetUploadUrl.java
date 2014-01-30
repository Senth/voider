package com.spiddekauga.prototype;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

/**
 * Returns a valid upload url
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class GetUploadUrl extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		String uploadUrl = blobstoreService.createUploadUrl("/enemyuploadfinished");
		response.addHeader("uploadUrl", uploadUrl);

		mLogger.info("Got upload url: " + uploadUrl);
	}

	/** Logger */
	private static final Logger mLogger = Logger.getLogger(GetUploadUrl.class.getName());
}