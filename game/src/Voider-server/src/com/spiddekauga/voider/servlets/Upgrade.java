package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Does an upgrade for the server
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Upgrade extends VoiderServlet {
	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		// Update private key in user entities
		Query query = new Query("users");
		Iterator<Entity> entityIt = DatastoreUtils.prepare(query).asIterator();

		while (entityIt.hasNext()) {
			Entity entity = entityIt.next();

			// Update private key
			UUID uuid = new UUID(-6809848858477280398L, 3947965928476658606L);
			DatastoreUtils.setProperty(entity, "private_key", uuid);
			DatastoreUtils.put(entity);
		}

		return null;
	}
}
