package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.LevelGetCommentMethod;
import com.spiddekauga.voider.network.entities.resource.LevelGetCommentMethodResponse;
import com.spiddekauga.voider.network.entities.resource.LevelGetCommentMethodResponse.Statuses;
import com.spiddekauga.voider.network.entities.stat.LevelCommentEntity;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.FetchSizes;
import com.spiddekauga.voider.server.util.UserRepo;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Get all level comments
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelGetComment extends VoiderServlet {

	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {

		LevelGetCommentMethodResponse methodResponse = new LevelGetCommentMethodResponse();
		methodResponse.status = Statuses.FAILED_INTERNAL;

		if (mUser.isLoggedIn()) {
			if (methodEntity instanceof LevelGetCommentMethod) {
				UUID levelId = ((LevelGetCommentMethod) methodEntity).levelId;
				String cursor = ((LevelGetCommentMethod) methodEntity).cursor;

				// Get level key
				Key levelKey = DatastoreUtils.getSingleKey(DatastoreTables.PUBLISHED.toString(), new FilterWrapper("resuorce_id", levelId));

				QueryResultList<Entity> comments = getComments(levelKey, cursor);
				addCommentsToResponse(comments, methodResponse);


				// Get player's comment for first query
				if (cursor == null) {
					methodResponse.userComment = getUserComment(levelKey);
				}

				methodResponse.status = Statuses.SUCCESS;
			}
		} else {
			methodResponse.status = Statuses.FAILED_USER_NOT_LOGGED_IN;
		}

		return methodResponse;
	}

	/**
	 * Gets comments from the datastore
	 * @param levelKey key of the level to get comments from
	 * @param startCursor where to start getting comments from
	 * @return list of all comment entities
	 */
	private QueryResultList<Entity> getComments(Key levelKey, String startCursor) {
		Query query = new Query(DatastoreTables.LEVEL_COMMENT.toString(), levelKey);


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
			Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.LEVEL_COMMENT.toString(), levelKey,
					new FilterWrapper("user_key", mUser.getKey()));

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
