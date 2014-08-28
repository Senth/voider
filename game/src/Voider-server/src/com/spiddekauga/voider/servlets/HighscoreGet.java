package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.entities.HighscoreEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.HighscoreGetMethod;
import com.spiddekauga.voider.network.entities.method.HighscoreGetMethodResponse;
import com.spiddekauga.voider.network.entities.method.HighscoreGetMethodResponse.Statuses;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.server.util.VoiderServlet;


/**
 * Returns all highscores of a specific level
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class HighscoreGet extends VoiderServlet {
	@Override
	protected void onInit() {
		mResponse = new HighscoreGetMethodResponse();
		mResponse.status = Statuses.FAILED_INTERNAL;
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (methodEntity instanceof HighscoreGetMethod) {
			if (((HighscoreGetMethod) methodEntity).levelId != null) {
				mParameters = (HighscoreGetMethod) methodEntity;

				boolean foundKey = fetchLevelKey();

				if (foundKey) {
					switch (mParameters.fetch) {
					case FIRST_PLACE:
						fetchFirstPlace();
						mResponse.status = Statuses.SUCCESS;
						break;

					case TOP_SCORES:
						fetchTopScores();
						break;

					case USER_SCORE:
						fetchFirstPlace();
						fetchUserScore();
						fetchUserPos();
						fetchScoreBeforeAndAfterUser();
						break;
					}
				} else {
					mResponse.status = Statuses.FAILED_LEVEL_NOT_FOUND;
				}
			}
		}

		return mResponse;
	}

	/**
	 * Fetch user score
	 */
	private void fetchUserScore() {
		Query query = new Query("highscore", mLevelKey);
		query.setFilter(new FilterPredicate("username", FilterOperator.EQUAL, USERNAME));

		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);
		Entity entity = preparedQuery.asSingleEntity();
		if (entity != null) {
			mResponse.userScore = datastoreToNetworkEntity(entity);
		}
	}

	/**
	 * Fetch scores before and after the user
	 */
	private void fetchScoreBeforeAndAfterUser() {
		if (mResponse.userScore == null) {
			return;
		}

		mResponse.beforeUser = new ArrayList<>();
		mResponse.afterUser = new ArrayList<>();

		// Before (higher score) - Initial
		Iterator<Entity> higherIt = getScoreBeforeOrAfterUser(true);
		while (higherIt.hasNext() && mResponse.beforeUser.size() < SCORES_BEFORE_AFTER_USER) {
			Entity entity = higherIt.next();
			HighscoreEntity highscoreEntity = datastoreToNetworkEntity(entity);
			mResponse.beforeUser.add(highscoreEntity);
		}

		// After (lower score)
		Iterator<Entity> lowerIt = getScoreBeforeOrAfterUser(false);
		while (lowerIt.hasNext() && mResponse.afterUser.size() < SCORES_BEFORE_AFTER_USER) {
			Entity entity = lowerIt.next();
			HighscoreEntity highscoreEntity = datastoreToNetworkEntity(entity);
			mResponse.afterUser.add(highscoreEntity);
		}


		// Do we miss score before or after? I.e. player is either almost last or almost
		// first
		// Low number before/higher
		if (mResponse.beforeUser.size() < SCORES_BEFORE_AFTER_USER && mResponse.afterUser.size() >= SCORES_BEFORE_AFTER_USER) {
			int maxCount = SCORES_BEFORE_AFTER_USER * 2;
			while (higherIt.hasNext() && mResponse.beforeUser.size() + mResponse.afterUser.size() < maxCount) {
				Entity entity = higherIt.next();
				HighscoreEntity highscoreEntity = datastoreToNetworkEntity(entity);
				mResponse.beforeUser.add(highscoreEntity);
			}
		}
		// Low number after/lower
		else if (mResponse.afterUser.size() < SCORES_BEFORE_AFTER_USER && mResponse.beforeUser.size() >= SCORES_BEFORE_AFTER_USER) {
			int maxCount = SCORES_BEFORE_AFTER_USER * 2;
			while (lowerIt.hasNext() && mResponse.beforeUser.size() + mResponse.afterUser.size() < maxCount) {
				Entity entity = lowerIt.next();
				HighscoreEntity highscoreEntity = datastoreToNetworkEntity(entity);
				mResponse.afterUser.add(highscoreEntity);
			}
		}
	}

	/**
	 * Get result for either higher or lower scores than the user
	 * @param higher set to true to get results for higher score
	 * @return iterator for score results
	 */
	private Iterator<Entity> getScoreBeforeOrAfterUser(boolean higher) {
		FilterOperator filterOperator = higher ? FilterOperator.GREATER_THAN : FilterOperator.LESS_THAN;
		SortDirection sortDirection = higher ? SortDirection.ASCENDING : SortDirection.DESCENDING;

		Query query = new Query("highscore", mLevelKey);
		addScoreProjection(query);
		query.addSort("score", sortDirection);
		query.setFilter(new FilterPredicate("score", filterOperator, mResponse.userScore.score));

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults().prefetchSize(SCORES_BEFORE_AFTER_USER).chunkSize(SCORES_BEFORE_AFTER_USER);
		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);
		return preparedQuery.asIterator(fetchOptions);
	}

	/**
	 * Fetch user position
	 */
	private void fetchUserPos() {
		if (mResponse.userScore == null) {
			return;
		}

		FilterWrapper scoreFilter = new FilterWrapper("score", FilterOperator.GREATER_THAN, mResponse.userScore.score);
		int cBeforeUser = DatastoreUtils.count("highscore", mLevelKey, scoreFilter);

		mResponse.userPlace = cBeforeUser + 1;
	}

	/**
	 * Fetch and set level key
	 * @return true if successful, false otherwise
	 */
	private boolean fetchLevelKey() {
		mLevelKey = DatastoreUtils.getSingleKey("published", new FilterWrapper("resource_id", mParameters.levelId));
		return mLevelKey != null;
	}

	/**
	 * Fetch top scores
	 */
	private void fetchTopScores() {
		Iterator<Entity> entityIt = fetchTopScores(TOP_SCORES);

		mResponse.topScores = new ArrayList<>();

		while (entityIt.hasNext()) {
			Entity entity = entityIt.next();
			mResponse.topScores.add(datastoreToNetworkEntity(entity));
		}
	}

	/**
	 * Fetch and set the first place for the level if one exists
	 */
	private void fetchFirstPlace() {
		Iterator<Entity> entityIt = fetchTopScores(1);

		if (entityIt.hasNext()) {
			Entity entity = entityIt.next();
			mResponse.firstPlace = datastoreToNetworkEntity(entity);
		}
	}

	/**
	 * Fetch the X top scores
	 * @param limit the limit of top scores to fetch
	 * @return iterator for the top scores
	 */
	private Iterator<Entity> fetchTopScores(int limit) {
		Query query = new Query("highscore", mLevelKey);
		addScoreProjection(query);
		query.addSort("score", SortDirection.DESCENDING);

		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);
		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults().limit(limit);
		return preparedQuery.asIterator(fetchOptions);
	}


	/**
	 * Add score projection to query
	 * @param query the query to add the score projection to
	 */
	private static void addScoreProjection(Query query) {
		query.addProjection(new PropertyProjection("username", String.class));
		query.addProjection(new PropertyProjection("score", Long.class));
	}

	/**
	 * Convert datastore entity to a network entity
	 * @param datastoreEntity entity from the datastore
	 * @return highscore entity for the network
	 */
	private static HighscoreEntity datastoreToNetworkEntity(Entity datastoreEntity) {
		HighscoreEntity highscoreEntity = new HighscoreEntity();
		highscoreEntity.playerName = (String) datastoreEntity.getProperty("username");
		highscoreEntity.score = ((Long) datastoreEntity.getProperty("score")).intValue();
		return highscoreEntity;
	}

	/** Number of top scores to get */
	private static final int TOP_SCORES = 10;
	/** Number of scores to fetch before and after the user */
	private static final int SCORES_BEFORE_AFTER_USER = 5;
	/** REMOVE username */
	private static final String USERNAME = "username_10";

	/** Level key */
	private Key mLevelKey = null;
	/** Parameters */
	private HighscoreGetMethod mParameters = null;
	/** Response */
	private HighscoreGetMethodResponse mResponse = null;
}
