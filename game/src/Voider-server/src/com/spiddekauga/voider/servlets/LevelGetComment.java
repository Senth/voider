package com.spiddekauga.voider.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.LevelGetCommentMethod;
import com.spiddekauga.voider.network.entities.method.LevelGetCommentMethodResponse;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Get all level comments
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class LevelGetComment extends VoiderServlet {

	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			return;
		}

		LevelGetCommentMethodResponse methodResponse = new LevelGetCommentMethodResponse();

		byte[] byteEntity = NetworkGateway.getEntity(request);
		IEntity networkEntity = NetworkEntitySerializer.deserializeEntity(byteEntity);

		if (networkEntity instanceof LevelGetCommentMethod) {

		}
	}

}
