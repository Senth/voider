package com.spiddekauga.prototype;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.magicscroll.server.blobstore.ChainedBlobstoreInputStream;

/**
 * Enemy has been uploaded
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class EnemyUploadFinished extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		mLogger.info("Upload done");

		Enumeration<String> attributeNames = request.getAttributeNames();
		while (attributeNames != null && attributeNames.hasMoreElements()) {
			String name = attributeNames.nextElement();
			Object value = request.getAttribute(name);
			mLogger.info("Attribute: " + name + " (" + value + ")");
		}


		Object blobkey = request.getAttribute("com.google.appengine.api.blobstore.upload.blobkeys");
		mLogger.info(blobkey.getClass() + ": " + blobkey);

		ChainedBlobstoreInputStream blobInputStream;
	}

	/** Logger */
	private static final Logger mLogger = Logger.getLogger(EnemyUploadFinished.class.getName());
}
