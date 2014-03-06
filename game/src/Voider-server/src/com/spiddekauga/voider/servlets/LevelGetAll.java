package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.voider.network.entities.DefTypes;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelDefEntity;
import com.spiddekauga.voider.network.entities.LevelInfoEntity;
import com.spiddekauga.voider.network.entities.LevelStatsEntity;
import com.spiddekauga.voider.network.entities.Tags;
import com.spiddekauga.voider.network.entities.UserLevelStatsEntity;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethodResponse;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.FetchSizes;
import com.spiddekauga.voider.server.util.UserRepo;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Get all levels with specified filters
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class LevelGetAll extends VoiderServlet {
	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			return;
		}

		byte[] byteEntity = NetworkGateway.getEntity(request);
		IEntity networkEntity = NetworkEntitySerializer.deserializeEntity(byteEntity);


		if (networkEntity instanceof LevelGetAllMethod) {
			mParameters = (LevelGetAllMethod) networkEntity;

			getAndSetLevelResponse();
		}


		byte[] byteResponse = NetworkEntitySerializer.serializeEntity(mResponse);
		NetworkGateway.sendResponse(response, byteResponse);
	}

	/**
	 * Get land set the levels to send back in response
	 */
	private void getAndSetLevelResponse() {
		// Tag filter
		if (!mParameters.tagFilter.isEmpty()) {
			filterByTags();
		}
		// Text search
		else if (mParameters.searchString != null && !mParameters.searchString.equals("")) {
			// Only search for 3 or more characters
			if (mParameters.searchString.length() >= 3) {
				searchLevels();
			}
		}
		// Just sort, i.e. get the levels
		else {
			mResponse.levels = getLevels(FetchSizes.LEVELS);
		}
	}

	/**
	 * Get levels a specified sorting algorithm. Cursor will be set
	 * @param limit maximum amount of levels to get
	 * @return all found levels
	 */
	private ArrayList<LevelInfoEntity> getLevels(int limit) {
		// Which table to search in?
		DatastoreTables table = null;
		switch (mParameters.sort) {
		// Level stats
		case LIKES:
		case PLAYS:
		case RATING:
			table = DatastoreTables.LEVEL_STAT;
			break;

			// Published table
		case NEWEST:
			table = DatastoreTables.PUBLISHED;
			break;
		}


		Query query = new Query(table.toString());


		// Set sorting and other filters if necessary
		switch (mParameters.sort) {
		case LIKES:
			query.addSort("likes", SortDirection.DESCENDING);
			break;

		case NEWEST:
			query.addSort("date", SortDirection.DESCENDING);
			// Only search for levels
			Filter levelFilter = new FilterPredicate("type", FilterOperator.EQUAL, DefTypes.LEVEL.getId());
			query.setFilter(levelFilter);
			break;

		case PLAYS:
			query.addSort("play_count", SortDirection.DESCENDING);
			break;

		case RATING:
			query.addSort("rating_avg", SortDirection.DESCENDING);
			break;
		}


		PreparedQuery preparedQuery = DatastoreUtils.mDatastore.prepare(query);

		// Limit
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(limit);

		// Set start cursor
		if (mParameters.nextCursor != null) {
			fetchOptions.startCursor(Cursor.fromWebSafeString(mParameters.nextCursor));
		}

		QueryResultList<Entity> queryResult = preparedQuery.asQueryResultList(fetchOptions);


		ArrayList<LevelInfoEntity> levels = new ArrayList<>();

		// Convert datastore entities to network entities.
		if (table == DatastoreTables.LEVEL_STAT) {
			for (Entity statsEntity : queryResult) {
				// Get the actual published information
				Key levelKey = statsEntity.getParent();

				LevelInfoEntity infoEntity = new LevelInfoEntity();
				infoEntity.defEntity = getLevelDefEntity(levelKey);
				infoEntity.stats = convertDatastoreToLevelStatsEntity(statsEntity);
				infoEntity.userStats = getUserLevelStats(levelKey, mUser.getKey());
				infoEntity.tags = getLevelTags(levelKey);
				levels.add(infoEntity);
			}
		} else if (table == DatastoreTables.PUBLISHED) {
			for (Entity publishedEntity : queryResult) {
				LevelInfoEntity infoEntity = new LevelInfoEntity();

				infoEntity.defEntity = convertDatastoreToLevelDefEntity(publishedEntity);
				infoEntity.stats = getLevelStatsEntity(publishedEntity.getKey());
				infoEntity.userStats = getUserLevelStats(publishedEntity.getKey(), mUser.getKey());
				infoEntity.tags = getLevelTags(publishedEntity.getKey());
				levels.add(infoEntity);
			}
		}

		// Set cursors, we set next cursor if we call this method again.
		mResponse.cursor = queryResult.getCursor().toWebSafeString();
		mParameters.nextCursor = mResponse.cursor;


		// Did we fetch all?
		if (levels.size() < limit) {
			mResponse.fetchedAll = true;
		}

		return levels;
	}

	/**
	 * Get most popular level tags for the specified level
	 * @param levelKey key of the level to get the tags from
	 * @return list with the most popular tags, empty if none was found
	 */
	private static ArrayList<Tags> getLevelTags(Key levelKey) {
		ArrayList<Tags> tags = new ArrayList<>();

		Query query = new Query(levelKey);

		// Skip tags that only has count of 1.
		Filter countFilter = new FilterPredicate("count", FilterOperator.GREATER_THAN, 1);
		query.setFilter(countFilter);

		// Sort
		query.addSort("count", SortDirection.DESCENDING);

		// Only get tag type
		query.addProjection(new PropertyProjection("tag", int.class));

		PreparedQuery preparedQuery = DatastoreUtils.mDatastore.prepare(query);

		// Limit
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(FetchSizes.TAGS);

		List<Entity> entities = preparedQuery.asList(fetchOptions);

		// Convert tags to enumeration
		for (Entity entity : entities) {
			int tagId = (int) entity.getProperty("tag");
			Tags tag = Tags.getEnumFromId(tagId);
			if (tag != null) {
				tags.add(tag);
			}
		}

		return tags;
	}

	/**
	 * Get level stats for the specified level
	 * @param levelKey key of the level to get the stats from
	 * @return new level stats (network) entity, null if not found
	 */
	private static LevelStatsEntity getLevelStatsEntity(Key levelKey) {
		Entity entity = DatastoreUtils.getSingleEntity("level_stat", null, null, levelKey);

		if (entity != null) {
			return convertDatastoreToLevelStatsEntity(entity);
		}

		return null;
	}

	/**
	 * Get user stats for a specified level
	 * @param levelKey key of the level to get the stats from
	 * @param userKey key of the user to get stats from
	 * @return new user level stats (network) entity, null if not found
	 */
	private static UserLevelStatsEntity getUserLevelStats(Key levelKey, Key userKey) {
		Entity entity = DatastoreUtils.getSingleEntity("user_level_stat", "level_key", levelKey, userKey);

		if (entity != null) {
			return convertDatastoreToUserLevelStatsEntity(entity);
		}

		return null;
	}

	/**
	 * Get published level
	 * @param levelKey key of the level to get
	 * @return new level def (network) entity, null if not found
	 */
	private static LevelDefEntity getLevelDefEntity(Key levelKey) {
		try {
			Entity entity = DatastoreUtils.mDatastore.get(levelKey);
			if (entity != null) {
				return convertDatastoreToLevelDefEntity(entity);
			}
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create user level stats entity from a datastore entity
	 * @param datastoreEntity to convert from
	 * @return new user level stats (network) entity
	 */
	private static UserLevelStatsEntity convertDatastoreToUserLevelStatsEntity(Entity datastoreEntity) {
		UserLevelStatsEntity userLevelStatsEntity = new UserLevelStatsEntity();

		userLevelStatsEntity.cCleared = (int) datastoreEntity.getProperty("clear_count");
		userLevelStatsEntity.cPlayed = (int) datastoreEntity.getProperty("play_count");
		userLevelStatsEntity.lastPlayed = (Date) datastoreEntity.getProperty("last_played");
		userLevelStatsEntity.like = (boolean) datastoreEntity.getProperty("like");
		userLevelStatsEntity.rating = (int) datastoreEntity.getProperty("rating");

		return userLevelStatsEntity;
	}

	/**
	 * Create level stats entity from a datastore entity
	 * @param datastoreEntity datastore entity to convert from
	 * @return new level stats (network) entity
	 */
	private static LevelStatsEntity convertDatastoreToLevelStatsEntity(Entity datastoreEntity) {
		LevelStatsEntity levelStatsEntity = new LevelStatsEntity();

		levelStatsEntity.cCleared = (int) datastoreEntity.getProperty("clear_count");
		levelStatsEntity.cLikes = (int) datastoreEntity.getProperty("likes");
		levelStatsEntity.cPlayed = (int) datastoreEntity.getProperty("play_count");
		levelStatsEntity.cRatings = (int) datastoreEntity.getProperty("ratings");
		levelStatsEntity.ratingAverage = (float) datastoreEntity.getProperty("rating_avg");
		levelStatsEntity.ratingSum = (int) datastoreEntity.getProperty("rating_sum");

		return levelStatsEntity;
	}

	/**
	 * Create level def entity from datastore entity
	 * @param datastoreEntity the datastore entity to convert from
	 * @return new level def (network) entity
	 */
	private static LevelDefEntity convertDatastoreToLevelDefEntity(Entity datastoreEntity) {
		LevelDefEntity networkEntity = new LevelDefEntity();

		networkEntity.copyParentId = DatastoreUtils.getUuidProperty(datastoreEntity, "copy_parent_id");

		networkEntity.date = (Date) datastoreEntity.getProperty("date");
		networkEntity.description = (String) datastoreEntity.getProperty("description");
		networkEntity.levelId = DatastoreUtils.getUuidProperty(datastoreEntity, "level_id");
		networkEntity.levelLength = (float) datastoreEntity.getProperty("level_length");
		networkEntity.name = (String) datastoreEntity.getProperty("name");
		networkEntity.resourceId = DatastoreUtils.getUuidProperty(datastoreEntity, "resource_id");
		networkEntity.png = DatastoreUtils.getByteArrayProperty(datastoreEntity, "png");
		networkEntity.type = DefTypes.LEVEL;


		// Set creators
		Key creatorKey = datastoreEntity.getParent();
		Key originalCreatorKey = (Key) datastoreEntity.getProperty("original_creator_key");
		networkEntity.creatorKey = KeyFactory.keyToString(creatorKey);
		networkEntity.originalCreator = KeyFactory.keyToString(originalCreatorKey);
		networkEntity.creator = UserRepo.getUsername(creatorKey);
		networkEntity.originalCreatorKey = UserRepo.getUsername(originalCreatorKey);


		// Skip dependencies, no need for the player to know about them

		return networkEntity;
	}

	/**
	 * Filter by tags
	 */
	private void filterByTags() {
		ArrayList<LevelInfoEntity> foundLevelsWithTags = mResponse.levels;

		while (foundLevelsWithTags.size() < FetchSizes.LEVELS && !mResponse.fetchedAll) {
			ArrayList<LevelInfoEntity> levels = getLevels(FetchSizes.LEVELS * 2);

			// Add OK tags
			for (LevelInfoEntity level : levels) {
				if (hasRequiredTags(level)) {
					foundLevelsWithTags.add(level);
				}
			}
		}
	}

	/**
	 * Checks if the level has all the required tags
	 * @param level the level to check if it has all the required tags
	 * @return true if it has all the required tags
	 */
	private boolean hasRequiredTags(LevelInfoEntity level) {
		return level.tags.containsAll(mParameters.tagFilter);
	}

	/**
	 * Search for levels and add these
	 */
	private void searchLevels() {
		com.google.appengine.api.search.Cursor cursor = null;
		if (mParameters.nextCursor != null) {
			cursor = com.google.appengine.api.search.Cursor.newBuilder().build(mParameters.nextCursor);
		}

		Results<ScoredDocument> foundDocuments = SearchUtils.search("level", mParameters.searchString, FetchSizes.LEVELS, cursor);

		if (foundDocuments.getNumberReturned() < FetchSizes.LEVELS) {
			mResponse.fetchedAll = true;
		}


		// Get the actual published levels from the search
		for (ScoredDocument document : foundDocuments) {
			Key levelKey = KeyFactory.stringToKey(document.getId());

			LevelInfoEntity infoEntity = new LevelInfoEntity();
			infoEntity.defEntity = getLevelDefEntity(levelKey);
			infoEntity.stats = getLevelStatsEntity(levelKey);
			infoEntity.userStats = getUserLevelStats(levelKey, mUser.getKey());
			infoEntity.tags = getLevelTags(levelKey);

			mResponse.levels.add(infoEntity);
		}

		// Set cursor
		mResponse.cursor = foundDocuments.getCursor().toWebSafeString();
	}

	/** Method parameters */
	private LevelGetAllMethod mParameters = null;
	/** Method response */
	private LevelGetAllMethodResponse mResponse = new LevelGetAllMethodResponse();
}
