package com.spiddekauga.prototype;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.utils.BCrypt;

/**
 * Creates a new user
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class NewUser extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Set response type
		response.setContentType("application/json");

		PrintWriter out = response.getWriter();

		String salt = BCrypt.gensalt();

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String hashedPassword = BCrypt.hashpw(password, salt);

		boolean createdUser = false;

		Entity entity = DatastoreUtils.getSingleItem("users", "username", username);
		if (entity == null) {
			entity = new Entity("users");
			entity.setProperty("username", username);
			entity.setProperty("salt", salt);
			entity.setProperty("password", hashedPassword);

			createdUser = true;
			DatastoreUtils.mDatastore.put(entity);

			mLogger.info("Created user");
		} else {
			mLogger.info("User already exists!");
		}

		String json;
		if (createdUser) {
			json = "{ \"success\": true; }";
		} else {
			json = "{ \"success\": false; }";
		}

		out.print(json);
	}

	/** Logger */
	private static final Logger mLogger = Logger.getLogger(NewUser.class.getName());
}
