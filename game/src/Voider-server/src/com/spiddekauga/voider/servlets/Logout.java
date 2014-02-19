package com.spiddekauga.voider.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spiddekauga.voider.network.entities.method.LogoutMethodResponse;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Called when a user tries to logout
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class Logout extends VoiderServlet {
	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LogoutMethodResponse logoutMethodResponse = new LogoutMethodResponse();

		if (mUser.isLoggedIn()) {
			mUser.logout();
		}

		byte[] byteResponse = NetworkEntitySerializer.serializeEntity(logoutMethodResponse);
		NetworkGateway.sendResponse(response, byteResponse);
	}
}
