package com.spiddekauga.prototype;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.utils.BCrypt;
import com.spiddekauga.web.VoiderServlet;

/**
 * Tries to login a user
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class OldLogin extends VoiderServlet {

	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");

		boolean loginSuccess = false;

		// Only log in if we haven't logged in
		if (!mUser.isLoggedIn()) {

			String username = request.getParameter("username");

			Entity entity = DatastoreUtils.getSingleItem("users", "username", username);
			if (entity != null) {
				String password = request.getParameter("password");

				String hashedPassword = (String) entity.getProperty("password");

				// Same password
				if (BCrypt.checkpw(password, hashedPassword)) {
					mUser.login(username);
					loginSuccess = true;
				} else {
					mLogger.info("Wrong password");
				}
			} else {
				mLogger.info("Did not find any user with that username");
			}
		} else {
			mLogger.info("User already logged in");
		}

		String json;
		if (loginSuccess) {
			json = "{ \"success\": true; }";
		} else {
			json = "{ \"success\": false; }";
		}
		PrintWriter out = response.getWriter();
		out.print(json);
		out.flush();
	}

	/** Logger */
	private static final Logger mLogger = Logger.getLogger(OldLogin.class.getName());
}
