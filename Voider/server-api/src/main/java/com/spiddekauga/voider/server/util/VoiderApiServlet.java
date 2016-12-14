package com.spiddekauga.voider.server.util;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.NetworkEntitySerializer;

import java.io.IOException;

import javax.servlet.ServletException;


/**
 * Wrapper for the Voider servlet
 * @param <Method> Method from the client
 */
@SuppressWarnings("serial")
public abstract class VoiderApiServlet<Method extends IMethodEntity> extends VoiderServlet {

@Override
@SuppressWarnings("unchecked")
protected void onRequest() throws ServletException, IOException {
	try {
		byte[] byteEntity = NetworkGateway.getEntity(getRequest());
		Method methodEntity = null;
		if (byteEntity != null) {
			methodEntity = (Method) NetworkEntitySerializer.deserializeEntity(byteEntity);
		}

		// Handle request
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


/**
 * Called by the server to handle a request
 * @param method the entity that was sent to the method
 * @return response entity
 * @throws IOException      if an input or output error is detected
 * @throws ServletException if the request could not be handled
 */
protected abstract IEntity onRequest(Method method) throws ServletException, IOException;

}
