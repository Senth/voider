package com.spiddekauga.voider.server.util;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.network.resource.FetchStatuses;
import com.spiddekauga.voider.network.resource.UploadTypes;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.ServerConfig.FetchSizes;

import java.util.ArrayList;
import java.util.List;


/**
 * Common class for fetching actors
 * @param <Method>
 * @param <DefType> actor definitino
 */
@SuppressWarnings("serial")
public abstract class ActorFetch<Method extends IMethodEntity, DefType extends DefEntity> extends ResourceFetch<Method, DefType> {

private String mNextCursor = null;
private FetchStatuses mFetchStatus = null;

@Override
protected void onInit() {
	mFetchStatus = null;
	mNextCursor = null;
}

/**
 * Get all newest actors of the specified type
 * @param type the type to get
 * @param cursor continues from this cursor if not null
 * @param actors add all found actors to this list
 */
protected void getNewestActors(UploadTypes type, String cursor, ArrayList<DefType> actors) {
	mFetchStatus = FetchStatuses.FAILED_SERVER_ERROR;

	Query query = new Query(DatastoreTables.PUBLISHED);

	// Only search for enemies
	Filter enemyFilter = new FilterPredicate(CPublished.TYPE, FilterOperator.EQUAL, type.toId());
	query.setFilter(enemyFilter);

	// By Newest
	query.addSort(CPublished.DATE, SortDirection.DESCENDING);

	PreparedQuery preparedQuery = DatastoreUtils.prepare(query);

	// Limit
	FetchOptions fetchOptions = FetchOptions.Builder.withLimit(FetchSizes.ACTORS);

	// Set start cursor
	if (cursor != null) {
		fetchOptions.startCursor(Cursor.fromWebSafeString(cursor));
	}

	QueryResultList<Entity> queryResult = preparedQuery.asQueryResultList(fetchOptions);

	for (Entity publishedEntity : queryResult) {
		DefType actorDef = datastoreToDefEntity(publishedEntity, newNetworkDef());
		setAdditionalDefInformation(publishedEntity, actorDef);
		actors.add(actorDef);
	}

	// Set cursor
	mNextCursor = queryResult.getCursor().toWebSafeString();

	// Did we fetch all?
	mFetchStatus = getSuccessStatus(queryResult);
}

@Override
protected FetchStatuses getSuccessStatus(List<?> list) {
	if (list.size() < FetchSizes.ACTORS) {
		return FetchStatuses.SUCCESS_FETCHED_ALL;
	} else {
		return FetchStatuses.SUCCESS_MORE_EXISTS;
	}
}

/**
 * @return the next cursor, null if {@link #getNewestActors(UploadTypes, String, ArrayList)} hasn't
 * been called
 */
protected String getNextCursor() {
	return mNextCursor;
}

/**
 * @return fetch status of {@link #getNewestActors(UploadTypes, String, ArrayList)}, null if the
 * method hasn't been called.
 */
protected FetchStatuses getFetchStatus() {
	return mFetchStatus;
}
}
