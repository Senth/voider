package com.spiddekauga.voider.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spiddekauga.voider.network.entities.method.LogoutMethodResponse;
import com.spiddekauga.voider.network.entities.method.LogoutMethodResponse.Statuses;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Called when a user tries to logout
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Logout extends VoiderServlet {
	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LogoutMethodResponse logoutMethodResponse = new LogoutMethodResponse();
		logoutMethodResponse.status = Statuses.FAILED_SERVER_ERROR;

		if (mUser.isLoggedIn()) {
			mUser.logout();
			logoutMethodResponse.status = Statuses.SUCCESS;
		} else {
			logoutMethodResponse.status = Statuses.FAILED_NOT_LOGGED_IN;
		}

		byte[] byteResponse = NetworkEntitySerializer.serializeEntity(logoutMethodResponse);
		NetworkGateway.sendResponse(response, byteResponse);
	}
}
