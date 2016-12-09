package com.spiddekauga.voider.servlets.api.backup;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.spiddekauga.appengine.BlobUtils;
import com.spiddekauga.voider.network.backup.DeleteAllBlobsMethod;
import com.spiddekauga.voider.network.backup.DeleteAllBlobsResponse;
import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;

/**
 * Deletes all blobs from the server
 */
@SuppressWarnings("serial")
public class DeleteAllBlobs extends VoiderApiServlet<DeleteAllBlobsMethod> {
private DeleteAllBlobsResponse mResponse = null;
private BlobInfoFactory mBlobInfoFactory = new BlobInfoFactory();

@Override
protected boolean isHandlingRequestDuringMaintenance() {
	return true;
}

@Override
protected void onInit() {
	mResponse = new DeleteAllBlobsResponse();
	mResponse.status = GeneralResponseStatuses.SUCCESS;
}

@Override
protected IEntity onRequest(DeleteAllBlobsMethod method) throws ServletException, IOException {

	Iterator<BlobInfo> blobInfoIt = mBlobInfoFactory.queryBlobInfos();
	ArrayList<BlobKey> blobKeys = new ArrayList<>();
	while (blobInfoIt.hasNext()) {
		BlobInfo blobInfo = blobInfoIt.next();
		blobKeys.add(blobInfo.getBlobKey());
	}
	mLogger.info("Deleting " + blobKeys.size() + " blobs");
	BlobUtils.delete(blobKeys);

	return mResponse;
}
}
