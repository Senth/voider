package com.spiddekauga.voider.servlets.api;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.resource.CommentFetchMethod;
import com.spiddekauga.voider.network.resource.CommentFetchResponse;
import com.spiddekauga.voider.network.resource.FetchStatuses;
import com.spiddekauga.voider.network.stat.CommentEntity;
import com.spiddekauga.voider.server.util.ServerConfig;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CResourceComment;
import com.spiddekauga.voider.server.util.ServerConfig.FetchSizes;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Get resource comments in batches
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class CommentFetch extends VoiderApiServlet {

	@Override
	protected void onInit() {
		mResponse = new CommentFetchResponse();
		mResponse.status = FetchStatuses.FAILED_SERVER_ERROR;
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			mResponse.status = FetchStatuses.FAILED_USER_NOT_LOGGED_IN;
			return mResponse;
		}

		if (methodEntity instanceof CommentFetchMethod) {
			UUID resourceId = ((CommentFetchMethod) methodEntity).resourceId;
			String cursor = ((CommentFetchMethod) methodEntity).nextCursor;

			// Get level key
			Key resourceKey = getResourceKey(resourceId);

			QueryResultList<Entity> comments = getComments(resourceKey, cursor);
			addCommentsToResponse(comments);

			// Get player's comment for first query
			if (cursor == null) {
				mResponse.userComment = getUserComment(resourceKey);
			}

			// Fetched all
			if (mResponse.comments.size() < ServerConfig.FetchSizes.COMMENTS || mResponse.cursor == null) {
				mResponse.status = FetchStatuses.SUCCESS_FETCHED_ALL;
			} else {
				mResponse.status = FetchStatuses.SUCCESS_MORE_EXISTS;
			}
		}

		return mResponse;
	}

	/**
	 * Get resource key from resource
	 * @param resourceId
	 * @return get resource key
	 */
	private Key getResourceKey(UUID resourceId) {
		FilterWrapper idFilter = new FilterWrapper(CPublished.RESOURCE_ID, resourceId);
		return DatastoreUtils.getSingleKey(T_PUBLISHED, idFilter);
	}

	/**
	 * Gets comments from the datastore
	 * @param resourceKey key of the resource to get comments from
	 * @param startCursor where to start getting comments from
	 * @return batch list of comments
	 */
	private QueryResultList<Entity> getComments(Key resourceKey, String startCursor) {
		Query query = new Query(T_COMMENT, resourceKey);

		// Sort after latest comment
		query.addSort(CResourceComment.DATE, SortDirection.DESCENDING);
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
	 */
	private void addCommentsToResponse(QueryResultList<Entity> comments) {
		// Convert to network entities
		for (Entity comment : comments) {
			CommentEntity networkEntity = createNetworkEntity(comment);
			mResponse.comments.add(networkEntity);
		}

		// Set cursor
		Cursor cursor = comments.getCursor();
		if (cursor != null) {
			mResponse.cursor = cursor.toWebSafeString();
		}
	}

	/**
	 * Get user comment for the specified level
	 * @param levelKey the level to get the user comment from
	 * @return the user comment, null if not found.
	 */
	private CommentEntity getUserComment(Key levelKey) {
		Entity entity = DatastoreUtils.getSingleEntity(T_COMMENT, levelKey, new FilterWrapper(CResourceComment.USERNAME, mUser.getUsername()));

		if (entity != null) {
			return createNetworkEntity(entity);
		}

		return null;
	}

	/**
	 * Create a level comment entity from a datastore entity
	 * @param datastoreEntity the datastore entity
	 * @return a level comment entity
	 */
	private CommentEntity createNetworkEntity(Entity datastoreEntity) {
		CommentEntity networkEntity = new CommentEntity();
		networkEntity.comment = (String) datastoreEntity.getProperty(CResourceComment.COMMENT);
		networkEntity.date = (Date) datastoreEntity.getProperty(CResourceComment.DATE);
		networkEntity.username = (String) datastoreEntity.getProperty(CResourceComment.USERNAME);

		return networkEntity;
	}

	private CommentFetchResponse mResponse = null;

	// Tables
	private static final String T_PUBLISHED = DatastoreTables.PUBLISHED;
	private static final String T_COMMENT = DatastoreTables.RESOURCE_COMMENT;
}
