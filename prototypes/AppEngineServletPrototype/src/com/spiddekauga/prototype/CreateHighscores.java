package com.spiddekauga.prototype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;

/**
 * Creates lots of highscores and two levels for the highscores
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class CreateHighscores extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!DatastoreUtils.exists("published", new FilterWrapper("resource_id", mLevelIds.get(0)))) {
			createLevels();
		} else {
			getLevels();
		}
		deleteHighscores();
		createHighscores(mLevelKeys.get(0), 1000);
		createHighscores(mLevelKeys.get(1), 10000);

		resp.setContentType("text/plain");
		resp.getWriter().println("Done!");
	}

	/**
	 * Delete all highscores
	 */
	private void deleteHighscores() {
		List<Key> keys = DatastoreUtils.getKeys("highscore");
		DatastoreUtils.delete(keys);
	}

	/**
	 * Get levels
	 */
	private void getLevels() {
		for (UUID levelId : mLevelIds) {
			Key levelKey = DatastoreUtils.getSingleKey("published", new FilterWrapper("resource_id", levelId));
			mLevelKeys.add(levelKey);
		}
	}

	/**
	 * Create levels
	 */
	private void createLevels() {
		for (UUID levelId : mLevelIds) {
			Entity entity = new Entity("published");
			entity.setProperty("type", "level");
			entity.setUnindexedProperty("name", "my name");
			entity.setProperty("original_creator_key", "userkey");
			entity.setUnindexedProperty("description", "This should be quite a long description, but this is probably enough.");
			entity.setProperty("date", new Date());
			DatastoreUtils.setProperty(entity, "copy_parent_id", UUID.randomUUID());
			DatastoreUtils.setProperty(entity, "resource_id", levelId);
			entity.setProperty("level_length", 1000);
			DatastoreUtils.setProperty(entity, "level_id", UUID.randomUUID());

			mLevelKeys.add(DatastoreUtils.put(entity));
		}
	}

	/**
	 * Create highscores
	 * @param levelKey key for the level
	 * @param count number of highscores
	 */
	private void createHighscores(Key levelKey, int count) {
		Random random = new Random(1);
		Date date = new Date();

		for (int i = 0; i < count; ++i) {
			int randomScore = random.nextInt(100000000) + 1;

			Entity entity = new Entity("highscore", levelKey);
			entity.setProperty("username", "username_" + i);
			entity.setProperty("score", randomScore);
			entity.setProperty("created", new Date(randomScore));
			entity.setProperty("uploaded", date);

			DatastoreUtils.put(entity);
		}
	}

	/** Level ids */
	private static ArrayList<UUID> mLevelIds = new ArrayList<>();
	/** Created levels */
	private ArrayList<Key> mLevelKeys = new ArrayList<>();

	/**
	 * Initialize levels
	 */
	static {
		mLevelIds.add(UUID.fromString("d0b97b20-2cef-11e4-8c21-0800200c9a66"));
		mLevelIds.add(UUID.fromString("d0b97b21-2cef-11e4-8c21-0800200c9a66"));
	}
}
