package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletException;

import net._01001111.text.LoremIpsum;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CResourceComment;
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
		List<Key> keys = DatastoreUtils.getKeys("published");

		for (Key levelKey : keys) {
			// createEmptyLevelStatistics(levelKey);
			createComments(levelKey);
		}

		return null;
	}

	private void createComments(Key key) {
		// Create 100 comments between today and a year ago
		Date now = new Date();
		final int yearMillis = 365 * 24 * 60 * 60 * 1000;
		Random random = new Random();

		final int COMMENTS = 100;
		LoremIpsum loremIpsum = new LoremIpsum();

		for (int i = 0; i < COMMENTS; ++i) {
			String comment = loremIpsum.words(25);
			long dateTime = random.nextInt(yearMillis);
			Date date = new Date(now.getTime() - dateTime);

			Entity entity = new Entity("resource_comment", key);
			entity.setProperty(CResourceComment.USERNAME, "player_" + i);
			entity.setUnindexedProperty(CResourceComment.COMMENT, comment);
			entity.setProperty(CResourceComment.DATE, date);
			DatastoreUtils.put(entity);
		}
	}

	/**
	 * Create empty level statistics
	 * @param key datastore key of the level entity to add empty statistics for
	 * @return true if successful, false otherwise
	 */
	@SuppressWarnings("unused")
	private boolean createEmptyLevelStatistics(Key key) {
		Entity entity = new Entity(DatastoreTables.LEVEL_STAT.toString(), key);

		entity.setProperty("play_count", 0);
		entity.setProperty("bookmarks", 0);
		entity.setProperty("rating_sum", 0);
		entity.setProperty("ratings", 0);
		entity.setProperty("rating_avg", 0.0);
		entity.setProperty("clear_count", 0);

		Key statKey = DatastoreUtils.put(entity);

		return statKey != null;
	}
}
