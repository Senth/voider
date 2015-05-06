package com.spiddekauga.voider.servlets.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUsers;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Does an upgrade for the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings({ "serial" })
public class Upgrade extends VoiderServlet {
	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		updateUsers();

		getResponse().setContentType("text/html");
		getResponse().getWriter().append("DONE !");

		return null;
	}

	/**
	 * Update user datastore table
	 */
	private void updateUsers() {
		Iterable<Entity> entities = DatastoreUtils.getEntities(DatastoreTables.USERS);
		List<Entity> update = new ArrayList<>();

		for (Entity entity : entities) {
			String username = (String) entity.getProperty(CUsers.USERNAME);
			entity.setProperty(CUsers.USERNAME_LOWCASE, username.toLowerCase(Locale.ENGLISH));
			String email = (String) entity.getProperty(CUsers.EMAIL);
			entity.setProperty(CUsers.EMAIL, email.toLowerCase(Locale.ENGLISH));

			// Remove date format
			entity.removeProperty(CUsers.DATE_FORMAT);

			update.add(entity);
		}

		DatastoreUtils.put(update);
	}
}
