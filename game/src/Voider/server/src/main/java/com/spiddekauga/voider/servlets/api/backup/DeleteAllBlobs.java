package com.spiddekauga.voider.servlets.api.backup;

import java.io.IOException;

import javax.servlet.ServletException;

import com.spiddekauga.voider.network.backup.DeleteAllBlobsMethod;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Deletes all blobs from the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class DeleteAllBlobs extends VoiderApiServlet<DeleteAllBlobsMethod> {

	@Override
	protected IEntity onRequest(DeleteAllBlobsMethod method) throws ServletException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onInit() {
		// TODO Auto-generated method stub

	}

}
