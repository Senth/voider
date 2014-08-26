package com.spiddekauga.prototype;

import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;

/**
 * Creates lots of users
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class NewUsers extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Random random = new Random(System.nanoTime());

		for (int i = 0; i < 500; ++i) {
			long randomEnd = random.nextLong();

			Entity entity = new Entity("users");
			entity.setProperty("username", "Username_" + randomEnd);
			entity.setProperty("email", "email_" + randomEnd);
			entity.setProperty("password", "password_" + randomEnd);
			entity.setProperty("created", new Date());
			entity.setProperty("logged_in", new Date(randomEnd));
			DatastoreUtils.setProperty(entity, "private_key", UUID.randomUUID());
			entity.setProperty("date_format", "YYYY-MM-DD");

			DatastoreUtils.put(entity);
		}

		resp.setContentType("text/plain");
		resp.getWriter().println("Created 500 users!");
	}
}
