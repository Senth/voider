package com.spiddekauga.prototype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.magicscroll.server.blobstore.ChainedBlobstoreInputStream;

import com.google.appengine.api.blobstore.BlobKey;
import com.spiddekauga.appengine.BlobUtils;

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

		ArrayList<BlobKey> uploadedBlobkeys = BlobUtils.getBlobKeysFromUpload(request);

		ChainedBlobstoreInputStream blobInputStream = new ChainedBlobstoreInputStream(uploadedBlobkeys.get(0));

	}

	/** Logger */
	private static final Logger mLogger = Logger.getLogger(EnemyUploadFinished.class.getName());
}
