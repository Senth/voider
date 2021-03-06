package com.spiddekauga.voider.servlets.api;

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
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.appengine.SearchUtils.Builder.CombineOperators;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.network.resource.FetchStatuses;
import com.spiddekauga.voider.network.resource.LevelDefEntity;
import com.spiddekauga.voider.network.resource.LevelFetchMethod;
import com.spiddekauga.voider.network.resource.LevelFetchResponse;
import com.spiddekauga.voider.network.resource.LevelLengthSearchRanges;
import com.spiddekauga.voider.network.resource.LevelSpeedSearchRanges;
import com.spiddekauga.voider.network.resource.UploadTypes;
import com.spiddekauga.voider.network.stat.LevelInfoEntity;
import com.spiddekauga.voider.network.stat.LevelStatsEntity;
import com.spiddekauga.voider.network.stat.Tags;
import com.spiddekauga.voider.server.util.ResourceFetch;
import com.spiddekauga.voider.server.util.ServerConfig;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CLevelStat;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.ServerConfig.FetchSizes;
import com.spiddekauga.voider.server.util.ServerConfig.SearchTables;
import com.spiddekauga.voider.server.util.ServerConfig.SearchTables.SLevel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

/**
 * Get all levels with specified filters
 */
@SuppressWarnings("serial")
public class LevelFetch extends ResourceFetch<LevelFetchMethod, LevelInfoEntity> {
/** Method parameters */
private LevelFetchMethod mParameters = null;
/** Method response */
private LevelFetchResponse mResponse = null;

@Override
protected void onInit() {
	mResponse = new LevelFetchResponse();
	mResponse.status = FetchStatuses.FAILED_SERVER_ERROR;
}

@Override
protected IEntity onRequest(LevelFetchMethod method) throws ServletException, IOException {
	if (mUser.isLoggedIn()) {
		mParameters = method;
		getAndSetLevelResponse();
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
		mLogger.info("Search filter");
		mResponse.status = searchAndSetFoundDefs(SearchTables.LEVEL, mParameters.nextCursor, mResponse.levels);
	}
	// Tag filter
	else if (mParameters.tags != null && !mParameters.tags.isEmpty()) {
		mLogger.info("Search by tags");
		filterByTags();
	}
	// Just sort, i.e. get the levels
	else {
		mLogger.info("Sort");
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
		Filter levelFilter = new FilterPredicate(CPublished.TYPE, FilterOperator.EQUAL, UploadTypes.LEVEL_DEF.toId());
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
			Entity publishedEntity = DatastoreUtils.getEntity(levelKey);

			LevelInfoEntity infoEntity = new LevelInfoEntity();
			datastoreToDefEntity(publishedEntity, infoEntity.defEntity);
			datastoreToLevelStatsEntity(statsEntity, infoEntity.stats);
			searchToNetworkEntity(levelKey, infoEntity);
			levels.add(infoEntity);
		}
	} else if (DatastoreTables.PUBLISHED.equals(table)) {
		for (Entity publishedEntity : queryResult) {
			LevelInfoEntity infoEntity = new LevelInfoEntity();
			datastoreToDefEntity(publishedEntity, infoEntity.defEntity);
			getLevelStatsEntity(publishedEntity.getKey(), infoEntity.stats);
			searchToNetworkEntity(publishedEntity.getKey(), infoEntity);
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

@Override
protected String buildSearchString() {
	SearchUtils.Builder builder = new SearchUtils.Builder();

	// Free text search
	if (mParameters.searchString != null && mParameters.searchString.length() >= ServerConfig.SEARCH_TEXT_LENGTH_MIN) {
		builder.text(mParameters.searchString);
	}

	// Level length
	appendSearchEnumArray(SLevel.LEVEL_LENGTH_CAT, mParameters.levelLengths, LevelLengthSearchRanges.values().length, builder);

	// Level speed
	appendSearchEnumArray(SLevel.LEVEL_SPEED_CAT, mParameters.levelSpeeds, LevelSpeedSearchRanges.values().length, builder);

	// Tags
	if (!mParameters.tags.isEmpty()) {
		String[] tagStrings = new String[mParameters.tags.size()];
		for (int i = 0; i < tagStrings.length; i++) {
			tagStrings[i] = mParameters.tags.get(i).toSearchId();
		}
		builder.text(SLevel.TAGS, CombineOperators.AND, tagStrings);
	}

	return builder.build();
}

@Override
protected LevelInfoEntity newNetworkDef() {
	return new LevelInfoEntity();
}

@Override
protected LevelInfoEntity datastoreToDefEntity(Entity datastoreEntity, LevelInfoEntity networkEntity) {
	datastoreToDefEntity(datastoreEntity, networkEntity.defEntity);
	return networkEntity;
}

@Override
protected void setAdditionalDefInformation(Entity datastoreEntity, LevelInfoEntity networkEntity) {
	Key key = datastoreEntity.getKey();
	getLevelStatsEntity(datastoreEntity.getKey(), networkEntity.stats);
	searchToNetworkEntity(key, networkEntity);
}

/**
 * Get level stats for the specified level
 * @param levelKey key of the level to get the stats from
 * @param levelStatsEntity entity to set
 */
private static void getLevelStatsEntity(Key levelKey, LevelStatsEntity levelStatsEntity) {
	Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.LEVEL_STAT, levelKey);

	if (entity != null) {
		datastoreToLevelStatsEntity(entity, levelStatsEntity);
	}
}

/**
 * Sets information from the search document
 * @param key datastore key of the published, this is the same as the document id
 * @param levelInfoEntity sets the information in this instance
 */
private void searchToNetworkEntity(Key key, LevelInfoEntity levelInfoEntity) {
	String documentId = KeyFactory.keyToString(key);
	Document document = SearchUtils.getDocument(SearchTables.LEVEL, documentId);

	if (document != null) {
		// Length
		levelInfoEntity.defEntity.levelLength = SearchUtils.getFloat(document, SLevel.LEVEL_LENGTH);

		// Speed
		levelInfoEntity.defEntity.levelSpeed = SearchUtils.getFloat(document, SLevel.LEVEL_SPEED);

		// Tags
		ArrayList<String> tagStrings = SearchUtils.getTexts(document, SLevel.TAGS);
		for (String tag : tagStrings) {
			levelInfoEntity.tags.add(Tags.fromId(tag));
		}
	}
}

/**
 * Create level stats entity from a datastore entity
 * @param datastoreEntity datastore entity to convert from
 * @param levelStatsEntity entity to set
 */
private static void datastoreToLevelStatsEntity(Entity datastoreEntity, LevelStatsEntity levelStatsEntity) {
	levelStatsEntity.cCleared = DatastoreUtils.getPropertyInt(datastoreEntity, CLevelStat.CLEAR_COUNT, 0);
	levelStatsEntity.cBookmarks = DatastoreUtils.getPropertyInt(datastoreEntity, CLevelStat.BOOKMARS, 0);
	levelStatsEntity.cPlayed = DatastoreUtils.getPropertyInt(datastoreEntity, CLevelStat.PLAY_COUNT, 0);
	levelStatsEntity.cRatings = DatastoreUtils.getPropertyInt(datastoreEntity, CLevelStat.RATINGS, 0);
	levelStatsEntity.cDeaths = DatastoreUtils.getPropertyInt(datastoreEntity, CLevelStat.DEATH_COUNT, 0);
	levelStatsEntity.ratingAverage = DatastoreUtils.getPropertyFloat(datastoreEntity, CLevelStat.RATING_AVG, 0.0f);
	levelStatsEntity.ratingSum = DatastoreUtils.getPropertyInt(datastoreEntity, CLevelStat.RATING_SUM, 0);
}

@Override
protected FetchStatuses getSuccessStatus(List<?> list) {
	if (list.size() < FetchSizes.LEVELS) {
		return FetchStatuses.SUCCESS_FETCHED_ALL;
	} else {
		return FetchStatuses.SUCCESS_MORE_EXISTS;
	}
}

@Override
protected <DefType extends DefEntity> DefType datastoreToDefEntity(Entity datastoreEntity, DefType networkEntity) {
	super.datastoreToDefEntity(datastoreEntity, networkEntity);

	// Level information
	LevelDefEntity levelDefEntity = (LevelDefEntity) networkEntity;
	levelDefEntity.levelId = DatastoreUtils.getPropertyUuid(datastoreEntity, CPublished.LEVEL_ID);

	return networkEntity;
}
}
