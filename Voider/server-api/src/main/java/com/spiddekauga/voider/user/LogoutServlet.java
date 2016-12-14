package com.spiddekauga.voider.user;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.user.LogoutMethod;
import com.spiddekauga.voider.network.user.LogoutResponse;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * Called when a user tries to logout
 */
@SuppressWarnings("serial")
public class LogoutServlet extends VoiderApiServlet<LogoutMethod> {
@Override
protected void onInit() throws ServletException, IOException {
	super.onInit();
	setHandlesRequestDuringMaintenance(true);
}

@Override
protected IEntity onRequest(LogoutMethod method) throws ServletException, IOException {
	LogoutResponse logoutMethodResponse = new LogoutResponse();
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
