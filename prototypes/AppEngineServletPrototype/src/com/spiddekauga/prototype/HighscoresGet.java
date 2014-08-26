package com.spiddekauga.prototype;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.prototype.entities.HighscoreEntity;
import com.spiddekauga.voider.prototype.entities.HighscoreGetMethod;
import com.spiddekauga.voider.prototype.entities.HighscoreGetMethodResponse;
import com.spiddekauga.voider.prototype.entities.IEntity;
import com.spiddekauga.voider.prototype.entities.IMethodEntity;
import com.spiddekauga.web.VoiderServlet;

/**
 * Returns all highscores of a specific level
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class HighscoresGet extends VoiderServlet {
	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		mResponse = new HighscoreGetMethodResponse();

		if (methodEntity instanceof HighscoreGetMethod) {
			Key levelKey = getLevelKey(((HighscoreGetMethod) methodEntity).levelId);

			if (levelKey != null) {
				getHighscores(levelKey);
			}
		}

		return mResponse;
	}

	/**
	 * Get all highscores for the level
	 * @param levelKey level key identifier
	 */
	private void getHighscores(Key levelKey) {
		Query query = new Query("highscore", levelKey);

		query.addProjection(new PropertyProjection("score", Long.class));
		query.addProjection(new PropertyProjection("user_key", Key.class));

		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);

		FetchOptions fetchOptions = FetchOptions.Builder.withChunkSize(100);

		for (Entity entity : preparedQuery.asIterable(fetchOptions)) {
			HighscoreEntity highscoreEntity = new HighscoreEntity();
			highscoreEntity.score = ((Long) entity.getProperty("score")).intValue();
			highscoreEntity.playerName = getUsername((Key) entity.getProperty("user_key"));

			mResponse.highscores.add(highscoreEntity);
		}
	}

	/**
	 * Get level key from the level id
	 * @param levelId the level to get
	 * @return key of the level if exactly one level was found with the specified id
	 */
	private Key getLevelKey(UUID levelId) {
		List<Key> keys = DatastoreUtils.getKeys("published", new FilterWrapper("resource_id", levelId));

		Key levelKey = null;

		if (keys.size() == 1) {
			levelKey = keys.get(0);
		} else if (keys.size() > 1) {
			mLogger.severe("Found more than one level!");
		} else {
			mLogger.severe("Didn't find any level!");
		}


		return levelKey;
	}

	/**
	 * Get username from the specified key
	 * @param userKey key of the user
	 * @return username of the user, null if user wasn't found or hasn't any username set.
	 */
	private String getUsername(Key userKey) {
		Entity entity = DatastoreUtils.getEntityByKey(userKey);

		if (entity != null && entity.hasProperty("username")) {
			return (String) entity.getProperty("username");
		} else {
			return null;
		}
	}

	/** Response */
	HighscoreGetMethodResponse mResponse = null;
}
