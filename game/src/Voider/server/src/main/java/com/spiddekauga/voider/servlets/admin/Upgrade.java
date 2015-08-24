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
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CLevelStat;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserLevelStat;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUsers;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Does an upgrade for the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings({ "serial" })
public class Upgrade extends VoiderApiServlet<IMethodEntity> {
	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(IMethodEntity method) throws ServletException, IOException {
		updateUsers();
		addDeathStatColumn();

		getResponse().setContentType("text/html");
		getResponse().getWriter().append("DONE !");

		return null;
	}

	/**
	 * Add death column to level stats
	 */
	private void addDeathStatColumn() {
		// Level stats
		Iterable<Entity> entities = DatastoreUtils.getEntities(DatastoreTables.LEVEL_STAT);
		List<Entity> update = new ArrayList<>();

		for (Entity entity : entities) {
			if (!entity.hasProperty(CLevelStat.DEATH_COUNT)) {
				entity.setUnindexedProperty(CLevelStat.DEATH_COUNT, 0);
				update.add(entity);
			}
		}

		DatastoreUtils.put(update);

		// User stats
		entities = DatastoreUtils.getEntities(DatastoreTables.USER_LEVEL_STAT);
		update = new ArrayList<>();

		for (Entity entity : entities) {
			if (!entity.hasProperty(CUserLevelStat.DEATH_COUNT)) {
				entity.setUnindexedProperty(CUserLevelStat.DEATH_COUNT, 0);
				update.add(entity);
			}
		}

		DatastoreUtils.put(update);
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
			if (entity.hasProperty("date_format")) {
				entity.removeProperty("date_format");
			}

			update.add(entity);
		}

		DatastoreUtils.put(update);
	}
}
