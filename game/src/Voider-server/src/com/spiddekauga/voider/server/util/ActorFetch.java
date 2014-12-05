package com.spiddekauga.voider.server.util;

import java.util.ArrayList;
import java.util.List;

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
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.voider.network.entities.resource.DefEntity;
import com.spiddekauga.voider.network.entities.resource.FetchStatuses;
import com.spiddekauga.voider.network.entities.resource.UploadTypes;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.ServerConfig.FetchSizes;


/**
 * Common class for fetching actors
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 * @param <DefType> actor definitino
 */
@SuppressWarnings("serial")
public abstract class ActorFetch<DefType extends DefEntity> extends ResourceFetch {

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
		Filter enemyFilter = new FilterPredicate(CPublished.TYPE, FilterOperator.EQUAL, type.getId());
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
			DefType actorDef = datastoreToDefEntity(publishedEntity, newActorDef());
			setAdditionalActorInformation(publishedEntity, actorDef);
			actors.add(actorDef);
		}

		// Set cursor
		mNextCursor = queryResult.getCursor().toWebSafeString();

		// Did we fetch all?
		mFetchStatus = getSuccessStatus(queryResult);
	}

	/**
	 * Checks if we fetched all and returns the correct success status
	 * @param list the results from a query
	 * @return SUCCESS_FETCHED_ALL or SUCCESS_MORE_EXISTS depending on the size of the
	 *         list
	 */
	protected static FetchStatuses getSuccessStatus(List<?> list) {
		if (list.size() < FetchSizes.ACTORS) {
			return FetchStatuses.SUCCESS_FETCHED_ALL;
		} else {
			return FetchStatuses.SUCCESS_MORE_EXISTS;
		}
	}

	/**
	 * @return the next cursor, null if
	 *         {@link #getNewestActors(UploadTypes, String, ArrayList)} hasn't been called
	 */
	protected String getNextCursor() {
		return mNextCursor;
	}

	/**
	 * @return fetch status of {@link #getNewestActors(UploadTypes, String, ArrayList)},
	 *         null if the method hasn't been called.
	 */
	protected FetchStatuses getFetchStatus() {
		return mFetchStatus;
	}

	/**
	 * Builds a search string to search for
	 * @return search string to use when searching
	 */
	protected abstract String buildSearchString();

	/**
	 * Search and set the enemies as a response
	 * @param searchTable name of the table to search in
	 * @param nextCursor
	 * @param actors the actors to set
	 * @return fetchStatus of the search
	 */
	protected FetchStatuses searchAndSetFoundActors(String searchTable, String nextCursor, ArrayList<DefType> actors) {
		String searchString = buildSearchString();

		Results<ScoredDocument> documents = SearchUtils.search(searchTable, searchString, FetchSizes.ACTORS, nextCursor);

		// Convert all found documents to entities
		for (ScoredDocument document : documents) {
			DefType networkEntity = newActorDef();

			// Datastore
			Key datastoreKey = KeyFactory.stringToKey(document.getId());
			Entity datastoreEntity = DatastoreUtils.getEntity(datastoreKey);
			datastoreToDefEntity(datastoreEntity, networkEntity);

			setAdditionalActorInformation(datastoreEntity, networkEntity);
			actors.add(networkEntity);
		}

		// Did we fetch all
		return getSuccessStatus(actors);
	}

	/**
	 * Adds additional information to the actor
	 * @param publishedEntity TODO
	 * @param networkEntity newly created actor that needs information to be set
	 */
	protected abstract void setAdditionalActorInformation(Entity publishedEntity, DefType networkEntity);

	/**
	 * @return new empty actor definition
	 */
	protected abstract DefType newActorDef();

	private String mNextCursor = null;
	private FetchStatuses mFetchStatus = null;
}
