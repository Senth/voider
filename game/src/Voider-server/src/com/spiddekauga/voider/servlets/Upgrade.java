package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Does an upgrade for the server
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Upgrade extends VoiderServlet {
	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Update private key in user entities
		Query query = new Query("users");
		Iterator<Entity> entityIt = DatastoreUtils.prepare(query).asIterator();

		while (entityIt.hasNext()) {
			Entity entity = entityIt.next();

			// Update private key
			if (entity.hasProperty("privateKey-most")) {
				UUID privateKey = DatastoreUtils.getUuidProperty(entity, "privateKey");
				DatastoreUtils.setProperty(entity, "private_key", privateKey);
				entity.removeProperty("privateKey-most");
				entity.removeProperty("privateKey-least");
				DatastoreUtils.put(entity);
			}
		}
	}
}
