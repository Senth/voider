package com.spiddekauga.voider.server.util;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.blobstore.BlobKey;
import com.spiddekauga.appengine.BlobUtils;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.NetworkEntitySerializer;
import com.spiddekauga.voider.server.util.ServerConfig.MaintenanceModes;


/**
 * Wrapper for the Voider servlet
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 * @param <Method> Method from the client
 */
@SuppressWarnings("serial")
public abstract class VoiderApiServlet<Method extends IMethodEntity> extends VoiderServlet {
	/**
	 * Called by the server to handle a post or get call.
	 * @param method the entity that was sent to the method
	 * @return response entity
	 * @throws IOException if an input or output error is detected when the servlet
	 *         handles the GET/POST request
	 * @throws ServletException if the request for the GET/POST could not be handled
	 */
	protected abstract IEntity onRequest(Method method) throws ServletException, IOException;

	/**
	 * Initializes the servlet
	 */
	protected abstract void onInit();


	@Override
	@SuppressWarnings("unchecked")
	protected void handleRequest() throws ServletException, IOException {
		if (getMaintenanceMode() == MaintenanceModes.UP || isHandlingRequestDuringMaintenance()) {
			// Get method parameters
			try {
				byte[] byteEntity = NetworkGateway.getEntity(getRequest());
				Method methodEntity = null;
				if (byteEntity != null) {
					methodEntity = (Method) NetworkEntitySerializer.deserializeEntity(byteEntity);
				}

				// Handle request
				onInit();
				IEntity responseEntity = onRequest(methodEntity);

				// Send response
				if (responseEntity != null) {
					byte[] responseBytes = NetworkEntitySerializer.serializeEntity(responseEntity);
					NetworkGateway.sendResponse(getResponse(), responseBytes);
				}
			} catch (ClassCastException e) {
				// Wrong type of method. Doesn't work
			}
		}
	}

	/**
	 * Override this method if the subclass will handle request even during maintenance
	 * mode
	 * @return true if the subclass will handle requests during maintenance mode
	 */
	protected boolean isHandlingRequestDuringMaintenance() {
		return false;
	}

	/**
	 * @return get blob information from the current request, null if no uploads were
	 *         made.
	 */
	protected Map<UUID, BlobKey> getUploadedBlobs() {
		return BlobUtils.getBlobKeysFromUpload(getRequest());
	}

	/**
	 * @return get blob information from the current request where the uploaded resources
	 *         contains revisions, null if no uploads were made.
	 */
	protected Map<UUID, Map<Integer, BlobKey>> getUploadedRevisionBlobs() {
		return BlobUtils.getBlobKeysFromUploadRevision(getRequest());
	}


}
