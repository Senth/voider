package com.spiddekauga.prototype;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.spiddekauga.web.VoiderServlet;

/**
 * Returns a valid upload url
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class GetUploadUrl extends VoiderServlet {
	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			mLogger.info("User is not logged in!");
			PrintWriter out = response.getWriter();
			out.print("User is not logged in");
			out.flush();
			return;
		}


		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		String uploadUrl = blobstoreService.createUploadUrl("/enemyuploadfinished");
		response.addHeader("uploadUrl", uploadUrl);
		mLogger.info("Got upload url: " + uploadUrl);
	}


	/** Logger */
	private static final Logger mLogger = Logger.getLogger(GetUploadUrl.class.getName());
}