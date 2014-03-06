package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.entities.DefTypes;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelDefEntity;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod.SortOrders;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethodResponse;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
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

	}

	/**
	 * Get levels a specified sorting algorithm. Cursor will be set
	 * @param sort the sorting order to get the levels in
	 * @param limit maximum amount of levels to get
	 * @return all found levels
	 */
	private ArrayList<LevelDefEntity> getLevels(SortOrders sort, int limit) {
		// Which table to search in?
		DatastoreTables table = null;
		switch (sort) {
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
		switch (sort) {
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

		mResponse.cursor = queryResult.getCursor().toWebSafeString();

		ArrayList<LevelDefEntity> levels = new ArrayList<>();

		// TODO convert datastore entities to network entities.

		return levels;
	}

	/**
	 * Create level entity from datastore entity
	 * @param datastoreEntity the datastore entity to convert from
	 * @return new level network entity
	 */
	private LevelDefEntity convertDatastoreToNetworkEntity(Entity datastoreEntity) {
		LevelDefEntity networkEntity = new LevelDefEntity();

		networkEntity.copyParentId = DatastoreUtils.getUuidProperty(datastoreEntity, "copy_parent_id");

		networkEntity.date = (Date) datastoreEntity.getProperty("date");
		networkEntity.description = (String) datastoreEntity.getProperty("description");
		networkEntity.levelId = DatastoreUtils.getUuidProperty(datastoreEntity, "level_id");
		networkEntity.levelLength = (float) datastoreEntity.getProperty("level_length");
		networkEntity.name = (String) datastoreEntity.getProperty("name");
		networkEntity.resourceId = DatastoreUtils.getUuidProperty(datastoreEntity, "resource_id");
		networkEntity.png = DatastoreUtils.getByteArrayProperty(datastoreEntity, "png");


		// Set creators
		Key creatorKey = datastoreEntity.getParent();
		Key originalCreatorKey = (Key) datastoreEntity.getProperty("original_creator_key");
		networkEntity.creatorKey = KeyFactory.keyToString(creatorKey);
		networkEntity.originalCreator = KeyFactory.keyToString(originalCreatorKey);
		networkEntity.creator = UserRepo.getUsername(creatorKey);
		networkEntity.originalCreatorKey = UserRepo.getUsername(originalCreatorKey);


		// TODO Set dependencies

		return networkEntity;
	}

	/**
	 * Filter by tags
	 */
	private void filterByTags() {

	}

	/**
	 * Search for levels and add these
	 */
	private void searchLevels() {
		//
	}

	/** Method parameters */
	private LevelGetAllMethod mParameters = null;
	/** Method response */
	private LevelGetAllMethodResponse mResponse = new LevelGetAllMethodResponse();
}
