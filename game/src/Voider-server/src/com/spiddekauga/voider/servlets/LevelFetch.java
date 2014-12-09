package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.FetchStatuses;
import com.spiddekauga.voider.network.entities.resource.LevelDefEntity;
import com.spiddekauga.voider.network.entities.resource.LevelFetchMethod;
import com.spiddekauga.voider.network.entities.resource.LevelFetchMethodResponse;
import com.spiddekauga.voider.network.entities.resource.UploadTypes;
import com.spiddekauga.voider.network.entities.stat.LevelInfoEntity;
import com.spiddekauga.voider.network.entities.stat.LevelStatsEntity;
import com.spiddekauga.voider.network.entities.stat.Tags;
import com.spiddekauga.voider.server.util.ResourceFetch;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CLevelStat;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CLevelTag;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.ServerConfig.FetchSizes;
import com.spiddekauga.voider.server.util.ServerConfig.SearchTables;
import com.spiddekauga.voider.server.util.ServerConfig.SearchTables.SLevel;

/**
 * Get all levels with specified filters
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelFetch extends ResourceFetch {
	@Override
	protected void onInit() {
		mResponse = new LevelFetchMethodResponse();
		mResponse.status = FetchStatuses.FAILED_SERVER_ERROR;
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (mUser.isLoggedIn()) {
			if (methodEntity instanceof LevelFetchMethod) {
				mParameters = (LevelFetchMethod) methodEntity;

				getAndSetLevelResponse();
			}
		} else {
			mResponse.status = FetchStatuses.FAILED_USER_NOT_LOGGED_IN;
		}

		return mResponse;
	}

	/**
	 * Get and set the levels to send back in response
	 */
	private void getAndSetLevelResponse() {
		// Search filter
		if (mParameters.search) {
			searchLevels();
		}
		// Tag filter
		else if (mParameters.tags != null && !mParameters.tags.isEmpty()) {
			filterByTags();
		}
		// Just sort, i.e. get the levels
		else {
			mResponse.levels = getLevels(FetchSizes.LEVELS);
			if (mResponse.status != FetchStatuses.SUCCESS_FETCHED_ALL) {
				mResponse.status = FetchStatuses.SUCCESS_MORE_EXISTS;
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
		String table = null;
		switch (mParameters.sort) {
		// Level stats
		case BOOKMARKS:
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
		case BOOKMARKS:
			query.addSort(CLevelStat.BOOKMARS, SortDirection.DESCENDING);
			break;

		case NEWEST:
			query.addSort(CPublished.DATE, SortDirection.DESCENDING);
			// Only search for levels
			Filter levelFilter = new FilterPredicate(CPublished.TYPE, FilterOperator.EQUAL, UploadTypes.LEVEL_DEF.getId());
			query.setFilter(levelFilter);
			break;

		case PLAYS:
			query.addSort(CLevelStat.PLAY_COUNT, SortDirection.DESCENDING);
			break;

		case RATING:
			query.addSort(CLevelStat.RATING_AVG, SortDirection.DESCENDING);
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
		if (DatastoreTables.LEVEL_STAT.equals(table)) {
			for (Entity statsEntity : queryResult) {
				// Get the actual published information
				Key levelKey = statsEntity.getParent();

				LevelInfoEntity infoEntity = new LevelInfoEntity();
				infoEntity.defEntity = getLevelDefEntity(levelKey);
				infoEntity.stats = datastoreToLevelStatsEntity(statsEntity);
				infoEntity.tags = getLevelTags(levelKey);
				levels.add(infoEntity);
			}
		} else if (DatastoreTables.PUBLISHED.equals(table)) {
			for (Entity publishedEntity : queryResult) {
				LevelInfoEntity infoEntity = new LevelInfoEntity();

				infoEntity.defEntity = datastoreToLevelDefEntity(publishedEntity);
				infoEntity.stats = getLevelStatsEntity(publishedEntity.getKey());
				infoEntity.tags = getLevelTags(publishedEntity.getKey());
				levels.add(infoEntity);
			}
		}

		// Set cursors, we set next cursor if we call this method again.
		mResponse.cursor = queryResult.getCursor().toWebSafeString();
		mParameters.nextCursor = mResponse.cursor;


		// Did we fetch all?
		if (levels.size() < limit) {
			mResponse.status = FetchStatuses.SUCCESS_FETCHED_ALL;
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

		Query query = new Query(DatastoreTables.LEVEL_TAG, levelKey);

		// Sort
		query.addSort(CLevelTag.COUNT, SortDirection.DESCENDING);

		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);

		// Limit
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(FetchSizes.TAGS);

		List<Entity> entities = preparedQuery.asList(fetchOptions);

		// Convert tags to enumeration
		for (Entity entity : entities) {
			int tagId = DatastoreUtils.getIntProperty(entity, CLevelTag.TAG);
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
		Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.LEVEL_STAT, levelKey);

		if (entity != null) {
			return datastoreToLevelStatsEntity(entity);
		}

		return null;
	}

	/**
	 * Get published level
	 * @param levelKey key of the level to get
	 * @return new level def (network) entity, null if not found
	 */
	private static LevelDefEntity getLevelDefEntity(Key levelKey) {
		Entity entity = DatastoreUtils.getEntity(levelKey);
		if (entity != null) {
			return datastoreToLevelDefEntity(entity);
		}

		return null;
	}

	/**
	 * Create level stats entity from a datastore entity
	 * @param datastoreEntity datastore entity to convert from
	 * @return new level stats (network) entity
	 */
	private static LevelStatsEntity datastoreToLevelStatsEntity(Entity datastoreEntity) {
		LevelStatsEntity levelStatsEntity = new LevelStatsEntity();

		levelStatsEntity.cCleared = ((Long) datastoreEntity.getProperty(CLevelStat.CLEAR_COUNT)).intValue();
		levelStatsEntity.cBookmarks = ((Long) datastoreEntity.getProperty(CLevelStat.BOOKMARS)).intValue();
		levelStatsEntity.cPlayed = ((Long) datastoreEntity.getProperty(CLevelStat.PLAY_COUNT)).intValue();
		levelStatsEntity.cRatings = ((Long) datastoreEntity.getProperty(CLevelStat.RATINGS)).intValue();
		levelStatsEntity.ratingAverage = ((Double) datastoreEntity.getProperty(CLevelStat.RATING_AVG)).floatValue();
		levelStatsEntity.ratingSum = ((Long) datastoreEntity.getProperty(CLevelStat.RATING_SUM)).intValue();

		return levelStatsEntity;
	}

	/**
	 * Create level def entity from datastore entity
	 * @param datastoreEntity the datastore entity to convert from
	 * @return new level def (network) entity
	 */
	private static LevelDefEntity datastoreToLevelDefEntity(Entity datastoreEntity) {
		LevelDefEntity networkEntity = datastoreToDefEntity(datastoreEntity, new LevelDefEntity());

		// Datastore
		networkEntity.levelId = DatastoreUtils.getUuidProperty(datastoreEntity, CPublished.LEVEL_ID);

		// Search
		Document document = SearchUtils.getDocument(SearchTables.LEVEL, KeyFactory.keyToString(datastoreEntity.getKey()));
		if (document != null) {
			networkEntity.levelLength = SearchUtils.getFloat(document, SLevel.LEVEL_LENGTH);
			networkEntity.levelSpeed = SearchUtils.getFloat(document, SLevel.LEVEL_SPEED);
		}

		return networkEntity;
	}

	/**
	 * Filter by tags
	 */
	private void filterByTags() {
		ArrayList<LevelInfoEntity> foundLevelsWithTags = mResponse.levels;

		while (foundLevelsWithTags.size() < FetchSizes.LEVELS && mResponse.status != FetchStatuses.SUCCESS_FETCHED_ALL) {
			int fetchLimit = (mParameters.tags.size() + 1) * FetchSizes.LEVELS;
			ArrayList<LevelInfoEntity> levels = getLevels(fetchLimit);

			// Add OK tags
			for (LevelInfoEntity level : levels) {
				if (hasRequiredTags(level)) {
					foundLevelsWithTags.add(level);
				}
			}
		}

		if (mResponse.status != FetchStatuses.SUCCESS_FETCHED_ALL) {
			mResponse.status = FetchStatuses.SUCCESS_MORE_EXISTS;
		}
	}

	/**
	 * Checks if the level has all the required tags
	 * @param level the level to check if it has all the required tags
	 * @return true if it has all the required tags
	 */
	private boolean hasRequiredTags(LevelInfoEntity level) {
		return level.tags.containsAll(mParameters.tags);
	}

	/**
	 * Search for levels and add these
	 */
	private void searchLevels() {
		com.google.appengine.api.search.Cursor cursor = null;
		if (mParameters.nextCursor != null) {
			cursor = com.google.appengine.api.search.Cursor.newBuilder().build(mParameters.nextCursor);
		}

		Results<ScoredDocument> foundDocuments = SearchUtils.search(SearchTables.LEVEL, mParameters.searchString.toLowerCase(), FetchSizes.LEVELS,
				cursor);

		if (foundDocuments == null || foundDocuments.getCursor() == null || foundDocuments.getNumberReturned() < FetchSizes.LEVELS) {
			mResponse.status = FetchStatuses.SUCCESS_FETCHED_ALL;
		}

		if (foundDocuments != null) {
			// Get the actual published levels from the search
			for (ScoredDocument document : foundDocuments) {
				Key levelKey = KeyFactory.stringToKey(document.getId());

				LevelInfoEntity infoEntity = new LevelInfoEntity();
				infoEntity.defEntity = getLevelDefEntity(levelKey);
				infoEntity.stats = getLevelStatsEntity(levelKey);
				infoEntity.tags = getLevelTags(levelKey);

				mResponse.levels.add(infoEntity);
			}

			// Set cursor
			if (foundDocuments.getCursor() != null) {
				mResponse.cursor = foundDocuments.getCursor().toWebSafeString();
			}

			if (mResponse.status != FetchStatuses.SUCCESS_FETCHED_ALL) {
				mResponse.status = FetchStatuses.SUCCESS_MORE_EXISTS;
			}
		}
	}

	/** Method parameters */
	private LevelFetchMethod mParameters = null;
	/** Method response */
	private LevelFetchMethodResponse mResponse = null;
}
