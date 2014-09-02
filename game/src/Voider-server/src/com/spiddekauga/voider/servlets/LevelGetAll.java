package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
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
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.LevelDefEntity;
import com.spiddekauga.voider.network.entities.resource.LevelGetAllMethod;
import com.spiddekauga.voider.network.entities.resource.LevelGetAllMethodResponse;
import com.spiddekauga.voider.network.entities.resource.UploadTypes;
import com.spiddekauga.voider.network.entities.resource.LevelGetAllMethodResponse.Statuses;
import com.spiddekauga.voider.network.entities.stat.LevelInfoEntity;
import com.spiddekauga.voider.network.entities.stat.LevelStatsEntity;
import com.spiddekauga.voider.network.entities.stat.Tags;
import com.spiddekauga.voider.network.entities.stat.UserLevelStatsEntity;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.FetchSizes;
import com.spiddekauga.voider.server.util.UserRepo;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Get all levels with specified filters
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelGetAll extends VoiderServlet {
	@Override
	protected void onInit() {
		mResponse = new LevelGetAllMethodResponse();
		mResponse.status = Statuses.FAILED_SERVER_ERROR;
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (mUser.isLoggedIn()) {
			if (methodEntity instanceof LevelGetAllMethod) {
				mParameters = (LevelGetAllMethod) methodEntity;

				getAndSetLevelResponse();
			}
		} else {
			mResponse.status = Statuses.FAILED_USER_NOT_LOGGED_IN;
		}

		return mResponse;
	}

	/**
	 * Get and set the levels to send back in response
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
			if (mResponse.status != Statuses.SUCCESS_FETCHED_ALL) {
				mResponse.status = Statuses.SUCCESS_MORE_EXISTS;
			}
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
			Filter levelFilter = new FilterPredicate("type", FilterOperator.EQUAL, UploadTypes.LEVEL_DEF.getId());
			query.setFilter(levelFilter);
			break;

		case PLAYS:
			query.addSort("play_count", SortDirection.DESCENDING);
			break;

		case RATING:
			query.addSort("rating_avg", SortDirection.DESCENDING);
			break;
		}


		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);

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
			mResponse.status = Statuses.SUCCESS_FETCHED_ALL;
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

		Query query = new Query("level_tag", levelKey);

		// Skip tags that only has count of 1.
		Filter countFilter = new FilterPredicate("count", FilterOperator.GREATER_THAN, 1);
		query.setFilter(countFilter);

		// Sort
		query.addSort("count", SortDirection.DESCENDING);

		// Only get tag type
		query.addProjection(new PropertyProjection("tag", Long.class));

		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);

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
		Entity entity = DatastoreUtils.getSingleEntity("level_stat", levelKey);

		if (entity != null) {
			return convertDatastoreToLevelStatsEntity(entity);
		}

		return null;
	}

	/**
	 * Get user stats for a specified level
	 * @param levelKey key of the level to get the stats from
	 * @param userKey key of the user to get stats from
	 * @return new user level stats (network) entity, new empty if not found
	 */
	private static UserLevelStatsEntity getUserLevelStats(Key levelKey, Key userKey) {
		Entity entity = DatastoreUtils.getSingleEntity("user_level_stat", new FilterWrapper("level_key", levelKey));

		if (entity != null) {
			return convertDatastoreToUserLevelStatsEntity(entity);
		} else {
			return new UserLevelStatsEntity();
		}
	}

	/**
	 * Get published level
	 * @param levelKey key of the level to get
	 * @return new level def (network) entity, null if not found
	 */
	private static LevelDefEntity getLevelDefEntity(Key levelKey) {
		Entity entity = DatastoreUtils.getEntity(levelKey);
		if (entity != null) {
			return convertDatastoreToLevelDefEntity(entity);
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
		userLevelStatsEntity.bookmarked = (boolean) datastoreEntity.getProperty("like");
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

		levelStatsEntity.cCleared = ((Long) datastoreEntity.getProperty("clear_count")).intValue();
		levelStatsEntity.cBookmarks = ((Long) datastoreEntity.getProperty("likes")).intValue();
		levelStatsEntity.cPlayed = ((Long) datastoreEntity.getProperty("play_count")).intValue();
		levelStatsEntity.cRatings = ((Long) datastoreEntity.getProperty("ratings")).intValue();
		levelStatsEntity.ratingAverage = ((Double) datastoreEntity.getProperty("rating_avg")).floatValue();
		levelStatsEntity.ratingSum = ((Long) datastoreEntity.getProperty("rating_sum")).intValue();

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
		networkEntity.levelLength = ((Double) datastoreEntity.getProperty("level_length")).floatValue();
		networkEntity.name = (String) datastoreEntity.getProperty("name");
		networkEntity.resourceId = DatastoreUtils.getUuidProperty(datastoreEntity, "resource_id");
		networkEntity.png = DatastoreUtils.getByteArrayProperty(datastoreEntity, "png");
		networkEntity.type = UploadTypes.LEVEL_DEF;


		// Set creators
		Key creatorKey = datastoreEntity.getParent();
		Key originalCreatorKey = (Key) datastoreEntity.getProperty("original_creator_key");
		networkEntity.creatorKey = KeyFactory.keyToString(creatorKey);
		networkEntity.originalCreatorKey = KeyFactory.keyToString(originalCreatorKey);
		networkEntity.creator = UserRepo.getUsername(creatorKey);
		networkEntity.originalCreator = UserRepo.getUsername(originalCreatorKey);


		// Skip dependencies, no need for the player to know about them

		return networkEntity;
	}

	/**
	 * Filter by tags
	 */
	private void filterByTags() {
		ArrayList<LevelInfoEntity> foundLevelsWithTags = mResponse.levels;

		while (foundLevelsWithTags.size() < FetchSizes.LEVELS && mResponse.status != Statuses.SUCCESS_FETCHED_ALL) {
			ArrayList<LevelInfoEntity> levels = getLevels(FetchSizes.LEVELS * 2);

			// Add OK tags
			for (LevelInfoEntity level : levels) {
				if (hasRequiredTags(level)) {
					foundLevelsWithTags.add(level);
				}
			}
		}

		if (mResponse.status != Statuses.SUCCESS_FETCHED_ALL) {
			mResponse.status = Statuses.SUCCESS_MORE_EXISTS;
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

		Results<ScoredDocument> foundDocuments = SearchUtils.search("level", mParameters.searchString.toLowerCase(), FetchSizes.LEVELS, cursor);

		if (foundDocuments == null || foundDocuments.getCursor() == null || foundDocuments.getNumberReturned() < FetchSizes.LEVELS) {
			mResponse.status = Statuses.SUCCESS_FETCHED_ALL;
		}

		if (foundDocuments != null) {
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
			if (foundDocuments.getCursor() != null) {
				mResponse.cursor = foundDocuments.getCursor().toWebSafeString();
			}

			if (mResponse.status != Statuses.SUCCESS_FETCHED_ALL) {
				mResponse.status = Statuses.SUCCESS_MORE_EXISTS;
			}
		}
	}

	/** Method parameters */
	private LevelGetAllMethod mParameters = null;
	/** Method response */
	private LevelGetAllMethodResponse mResponse = null;
}
