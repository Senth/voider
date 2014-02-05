package com.spiddekauga.prototype;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.utils.BCrypt;

/**
 * Tries to login a user
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class Login extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");

		HttpSession session = request.getSession();


		PrintWriter out = response.getWriter();
		String username = request.getParameter("username");

		boolean loginSuccess = false;

		Entity entity = DatastoreUtils.getSingleItem("users", "username", username);
		if (entity != null) {
			String password = request.getParameter("password");

			String hashedPassword = (String) entity.getProperty("password");

			// Same password
			if (BCrypt.checkpw(password, hashedPassword)) {
				loginSuccess = true;
			} else {
				mLogger.info("Wrong password");
			}
		} else {
			mLogger.info("Did not find any user with that username");
		}

		String json;
		if (loginSuccess) {
			json = "{ \"success\": true; }";
		} else {
			json = "{ \"success\": false; }";
		}
		out.print(json);
		out.flush();
	}

	/** Logger */
	private static final Logger mLogger = Logger.getLogger(Login.class.getName());
}
