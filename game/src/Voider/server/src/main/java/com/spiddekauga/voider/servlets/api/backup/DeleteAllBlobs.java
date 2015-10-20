package com.spiddekauga.voider.servlets.api.backup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.spiddekauga.appengine.BlobUtils;
import com.spiddekauga.voider.network.backup.DeleteAllBlobsMethod;
import com.spiddekauga.voider.network.backup.DeleteAllBlobsResponse;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Deletes all blobs from the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class DeleteAllBlobs extends VoiderApiServlet<DeleteAllBlobsMethod> {
	@Override
	protected void onInit() {
		mResponse = new DeleteAllBlobsResponse();
	}

	@Override
	protected IEntity onRequest(DeleteAllBlobsMethod method) throws ServletException, IOException {

		Iterator<BlobInfo> blobInfoIt = mBlobInfoFactory.queryBlobInfos();
		ArrayList<BlobKey> blobKeys = new ArrayList<>();
		while (blobInfoIt.hasNext()) {
			BlobInfo blobInfo = blobInfoIt.next();
			blobKeys.add(blobInfo.getBlobKey());
		}
		BlobUtils.delete(blobKeys);

		return mResponse;
	}

	private DeleteAllBlobsResponse mResponse = null;
	private BlobInfoFactory mBlobInfoFactory = new BlobInfoFactory();
}
