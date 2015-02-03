package com.spiddekauga.voider.servlets;

import java.io.IOException;

import javax.servlet.ServletException;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.user.LogoutMethodResponse;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Called when a user tries to logout
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Logout extends VoiderServlet {
	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		LogoutMethodResponse logoutMethodResponse = new LogoutMethodResponse();
		logoutMethodResponse.status = GeneralResponseStatuses.FAILED_SERVER_ERROR;

		if (mUser.isLoggedIn()) {
			mUser.logout();
			logoutMethodResponse.status = GeneralResponseStatuses.SUCCESS;
		} else {
			logoutMethodResponse.status = GeneralResponseStatuses.FAILED_USER_NOT_LOGGED_IN;
		}
		return logoutMethodResponse;
	}
}
