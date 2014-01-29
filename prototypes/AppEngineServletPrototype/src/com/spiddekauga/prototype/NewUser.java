package com.spiddekauga.prototype;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

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
		response.setContentType("text/plain");

		PrintWriter out = response.getWriter();

		out.println("You user is: " + request.getParameter("username") + "\n"
				+ "Your password is: " + request.getParameter("password"));


	}

	/** Datastore service */
	private static DatastoreService mDatastore = DatastoreServiceFactory.getDatastoreService();
}
