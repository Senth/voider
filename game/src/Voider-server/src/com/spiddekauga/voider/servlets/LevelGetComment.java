package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelCommentEntity;
import com.spiddekauga.voider.network.entities.method.LevelGetCommentMethod;
import com.spiddekauga.voider.network.entities.method.LevelGetCommentMethodResponse;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.FetchSizes;
import com.spiddekauga.voider.server.util.UserRepo;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Get all level comments
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelGetComment extends VoiderServlet {

	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			return;
		}

		LevelGetCommentMethodResponse methodResponse = new LevelGetCommentMethodResponse();

		byte[] byteEntity = NetworkGateway.getEntity(request);
		IEntity networkEntity = NetworkEntitySerializer.deserializeEntity(byteEntity);

		if (networkEntity instanceof LevelGetCommentMethod) {
			UUID levelId = ((LevelGetCommentMethod) networkEntity).levelId;
			String cursor = ((LevelGetCommentMethod) networkEntity).cursor;

			// Get level key
			Key levelKey = DatastoreUtils.getSingleKey(DatastoreTables.PUBLISHED.toString(), "resource_id", levelId);

			QueryResultList<Entity> comments = getComments(levelKey, cursor);
			addCommentsToResponse(comments, methodResponse);


			// Get player's comment for first query
			if (cursor == null) {
				methodResponse.userComment = getUserComment(levelKey);
			}
		}

		byte[] byteResponse = NetworkEntitySerializer.serializeEntity(methodResponse);
		NetworkGateway.sendResponse(response, byteResponse);
	}

	/**
	 * Gets comments from the datastore
	 * @param levelKey key of the level to get comments from
	 * @param startCursor where to start getting comments from
	 * @return list of all comment entities
	 */
	private QueryResultList<Entity> getComments(Key levelKey, String startCursor) {
		Query query = new Query(DatastoreTables.LEVEL_COMMENT.toString(), levelKey);


		// Only get certain properties
		query.addProjection(new PropertyProjection("user_key", Key.class));
		query.addProjection(new PropertyProjection("comment", String.class));
		query.addProjection(new PropertyProjection("date", Date.class));


		// Sort after latest comment
		query.addSort("date", SortDirection.DESCENDING);


		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);

		// Limit
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(FetchSizes.COMMENTS);

		// Start from previous cursor
		if (startCursor != null) {
			fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor));
		}

		return preparedQuery.asQueryResultList(fetchOptions);
	}

	/**
	 * Add the datastore comments to the method response
	 * @param comments the comments to add to the method response
	 * @param methodResponse where to add the comments to
	 */
	private void addCommentsToResponse(QueryResultList<Entity> comments, LevelGetCommentMethodResponse methodResponse) {
		// Convert to network entities
		for (Entity comment : comments) {
			LevelCommentEntity networkEntity = createLevelCommentEntity(comment);
			methodResponse.levelComments.add(networkEntity);
		}

		// Set cursor
		Cursor cursor = comments.getCursor();
		if (cursor != null) {
			methodResponse.cursor = cursor.toWebSafeString();
		}
	}

	/**
	 * Get user comment for the specified level
	 * @param levelKey the level to get the user comment from
	 * @return the user comment, null if not found.
	 */
	private LevelCommentEntity getUserComment(Key levelKey) {
		try {
			Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.LEVEL_COMMENT.toString(), "user_key", mUser.getKey(), levelKey);

			if (entity != null) {
				return createLevelCommentEntity(entity);
			}

		} catch (TooManyResultsException e) {
			// Does nothing
		}

		return null;
	}

	/**
	 * Create a level comment entity from a datastore entity
	 * @param datastoreEntity the datastore entity
	 * @return a level comment entity
	 */
	private LevelCommentEntity createLevelCommentEntity(Entity datastoreEntity) {
		LevelCommentEntity networkEntity = new LevelCommentEntity();
		networkEntity.comment = (String) datastoreEntity.getProperty("comment");
		networkEntity.date = (Date) datastoreEntity.getProperty("date");
		networkEntity.username = UserRepo.getUsername((Key) datastoreEntity.getProperty("user_key"));

		return networkEntity;
	}
}
