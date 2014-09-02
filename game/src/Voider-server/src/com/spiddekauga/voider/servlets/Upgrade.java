package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Does an upgrade for the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Upgrade extends VoiderServlet {
	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		// Add stub highscores to a level
		UUID levelId = UUID.fromString("2263cb75-59b7-4f32-9398-92cb36287f58");
		Key levelKey = getLevelKey(levelId);
		Random random = new Random();
		Date date = new Date();

		// 100 highscores
		if (levelKey != null) {
			for (int i = 0; i < 100; ++i) {
				Entity entity = new Entity("highscore", levelKey);
				entity.setProperty("username", "username_" + i);
				entity.setProperty("score", random.nextInt(1500));
				entity.setProperty("created", date);
				entity.setProperty("uploaded", date);
				DatastoreUtils.setProperty(entity, "level_id", levelId);
				DatastoreUtils.put(entity);
			}
		}

		return null;
	}

	/**
	 * Get level key for the specified level_id
	 * @param levelId id for the level
	 * @return level key from the level
	 */
	private static Key getLevelKey(UUID levelId) {
		return DatastoreUtils.getSingleKey("published", new FilterWrapper("resource_id", levelId));
	}
}
