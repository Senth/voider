package com.spiddekauga.voider.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethodResponse;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Get all levels with specified filters
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class LevelGetAll extends VoiderServlet {
	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			return;
		}

		LevelGetAllMethodResponse methodResponse = new LevelGetAllMethodResponse();

		byte[] byteEntity = NetworkGateway.getEntity(request);
		IEntity networkEntity = NetworkEntitySerializer.deserializeEntity(byteEntity);


		if (networkEntity instanceof LevelGetAllMethod) {

		}


		byte[] byteResponse = NetworkEntitySerializer.serializeEntity(methodResponse);
		NetworkGateway.sendResponse(response, byteResponse);
	}

	/**
	 * Search for levels and add these
	 * @param networkEntity information how to search
	 * @param methodResponse all searched levels
	 */
}
