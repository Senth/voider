package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.search.Document;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.misc.ChatMessage;
import com.spiddekauga.voider.network.entities.misc.ChatMessage.MessageTypes;
import com.spiddekauga.voider.network.entities.stat.StatSyncEntity;
import com.spiddekauga.voider.network.entities.stat.StatSyncEntity.LevelStat;
import com.spiddekauga.voider.network.entities.stat.StatSyncMethod;
import com.spiddekauga.voider.network.entities.stat.StatSyncMethodResponse;
import com.spiddekauga.voider.network.entities.stat.StatSyncMethodResponse.Statuses;
import com.spiddekauga.voider.network.entities.stat.Tags;
import com.spiddekauga.voider.server.util.ServerConfig;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CLevelStat;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CLevelTag;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CResourceComment;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CUserLevelStat;
import com.spiddekauga.voider.server.util.ServerConfig.SearchTables;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Synchronizes various statistics
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class StatSync extends VoiderServlet {
	@Override
	protected void onInit() {
		mResponse = new StatSyncMethodResponse();
		mResponse.status = Statuses.FAILED_INTERNAL;
		mResponse.syncEntity.syncDate = new Date();
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			mResponse.status = Statuses.FAILED_USER_NOT_LOGGED_IN;
			return mResponse;
		}

		if (methodEntity instanceof StatSyncMethod) {
			mParameters = ((StatSyncMethod) methodEntity).syncEntity;
			syncToClient();
			checkAndResolveConflicts();
			syncToServer();

			mResponse.status = Statuses.SUCCESS;
		}

		return mResponse;
	}

	/**
	 * Get stats to sync to the client
	 */
	private void syncToClient() {
		// Only newer than last sync
		FilterWrapper filterLastSync = new FilterWrapper(CUserLevelStat.UPDATED, FilterOperator.GREATER_THAN, mParameters.syncDate);

		Iterable<Entity> entities = DatastoreUtils.getEntities(T_USER_STAT, mUser.getKey(), filterLastSync);

		for (Entity serverEntity : entities) {
			LevelStat userLevelStats = serverToNetworkEntity(serverEntity);
			Key levelKey = (Key) serverEntity.getProperty(CUserLevelStat.LEVEL_KEY);
			getComments(userLevelStats, levelKey);
			mUserStatsToClient.put(userLevelStats.id, userLevelStats);
			mResponse.syncEntity.levelStats.add(userLevelStats);
		}
	}

	/**
	 * Get user comments
	 * @param userLevelStats
	 * @param levelKey key for the level
	 */
	private void getComments(LevelStat userLevelStats, Key levelKey) {
		FilterWrapper userFilter = new FilterWrapper(CResourceComment.USERNAME, mUser.getUsername());

		Entity entity = DatastoreUtils.getSingleEntity(T_COMMENT, levelKey, userFilter);

		if (entity != null) {
			userLevelStats.comment = (String) entity.getProperty(CResourceComment.COMMENT);
		}
	}

	/**
	 * Check and resolve sync conflicts
	 */
	private void checkAndResolveConflicts() {
		Iterator<LevelStat> fromClientIt = mParameters.levelStats.iterator();
		while (fromClientIt.hasNext()) {
			LevelStat clientStats = fromClientIt.next();
			LevelStat serverStats = mUserStatsToClient.get(clientStats.id);

			// Found conflict -> Update to correct amount of played and clear count on
			// both server and cilent. Choose other settings from the latest settings
			if (serverStats != null) {
				fixPlayAndClearCountConflict(clientStats, serverStats);
				fixMiscUserLevelStatConflict(clientStats, serverStats);
			}
		}
	}

	/**
	 * Set correct play and clear count for a conflict
	 * @param clientStats stats from the client
	 * @param serverStats stats from the server
	 */
	private static void fixPlayAndClearCountConflict(LevelStat clientStats, LevelStat serverStats) {
		int cPlayed = serverStats.cPlayed + clientStats.cPlaysToSync;
		serverStats.cPlayed = cPlayed;
		clientStats.cPlayed = cPlayed;

		int cCleared = serverStats.cCleared + clientStats.cClearsToSync;
		serverStats.cCleared = cCleared;
		clientStats.cCleared = cCleared;
	}

	/**
	 * Set latest settings for level/campaign user stats
	 * @param clientStats stats from the client
	 * @param serverStats stats from the server
	 */
	private void fixMiscUserLevelStatConflict(LevelStat clientStats, LevelStat serverStats) {
		// Use client stats
		if (mParameters.syncDate.after(serverStats.updated)) {
			setUserLevelStats(clientStats, serverStats);
		}
		// Use server stats
		else {
			setUserLevelStats(serverStats, clientStats);
		}

		// Set last played
		if (clientStats.lastPlayed.after(serverStats.lastPlayed)) {
			serverStats.lastPlayed = clientStats.lastPlayed;
		} else {
			clientStats.lastPlayed = serverStats.lastPlayed;
		}
	}

	/**
	 * Set misc user level stats from another
	 * @param from set from this
	 * @param to set to this
	 */
	private static void setUserLevelStats(LevelStat from, LevelStat to) {
		to.bookmark = from.bookmark;
		to.rating = from.rating;
		to.tags = from.tags; // <--- TODO should we change this, how does tagging work?
		to.comment = from.comment;
	}

	/**
	 * Sync to server
	 */
	private void syncToServer() {
		for (LevelStat levelStat : mParameters.levelStats) {
			Key levelKey = getLevelKey(levelStat.id);

			if (levelKey != null) {
				LevelStat oldStat = updateUserLevelStats(levelKey, levelStat);
				updateGlobalLevelStats(levelKey, levelStat, oldStat);
				updateGlobalLevelTags(levelKey, levelStat.tags);
				updatedLevelComment(levelKey, levelStat.comment);
			}
		}

		// Send sync response
		if (!mParameters.levelStats.isEmpty()) {
			sendMessage(new ChatMessage<>(MessageTypes.SYNC_STAT, mUser.getClientId()));
		}
	}

	/**
	 * Update level comments
	 * @param levelKey key of the level
	 * @param comment possibly a new comment
	 */
	private void updatedLevelComment(Key levelKey, String comment) {
		// Get old comment
		FilterWrapper userFliter = new FilterWrapper(CResourceComment.USERNAME, mUser.getUsername());
		Entity oldEntity = DatastoreUtils.getSingleEntity(T_COMMENT, levelKey, userFliter);

		// Maybe update or remove old comment
		if (oldEntity != null) {
			String oldComment = (String) oldEntity.getProperty(CResourceComment.COMMENT);

			// Remove comment
			if (comment.isEmpty()) {
				DatastoreUtils.delete(oldEntity.getKey());
			}
			// Update comment
			else if (!oldComment.equals(comment)) {
				oldEntity.setUnindexedProperty(CResourceComment.COMMENT, comment);
				oldEntity.setProperty(CResourceComment.DATE, mResponse.syncEntity.syncDate);
			}
		}
		// Create new comment
		else if (!comment.isEmpty()) {
			Entity newEntity = new Entity(T_COMMENT, levelKey);
			newEntity.setProperty(CResourceComment.USERNAME, mUser.getUsername());
			newEntity.setUnindexedProperty(CResourceComment.COMMENT, comment);
			newEntity.setProperty(CResourceComment.DATE, mResponse.syncEntity.syncDate);
			DatastoreUtils.put(newEntity);
		}
	}

	/**
	 * Update user level stats
	 * @param levelKey key of the level
	 * @param levelStat stats to set
	 * @return old statistics
	 */
	private LevelStat updateUserLevelStats(Key levelKey, LevelStat levelStat) {
		FilterWrapper levelFilter = new FilterWrapper(CUserLevelStat.LEVEL_KEY, levelKey);
		Entity userEntity = DatastoreUtils.getSingleEntity(T_USER_STAT, mUser.getKey(), levelFilter);


		ArrayList<Integer> tagIds = new ArrayList<>();
		LevelStat oldStat = new LevelStat();

		// No entity for this level exists yet
		if (userEntity == null) {
			userEntity = new Entity(T_USER_STAT, mUser.getKey());
			userEntity.setProperty(CUserLevelStat.LEVEL_KEY, levelKey);
		}
		// Add old tags and set old rating
		else {
			@SuppressWarnings("unchecked")
			Collection<Long> oldTagIds = (Collection<Long>) userEntity.getProperty(CUserLevelStat.TAGS);
			if (oldTagIds != null) {
				for (Long oldTagId : oldTagIds) {
					tagIds.add(oldTagId.intValue());
				}
			}

			oldStat.rating = DatastoreUtils.getIntProperty(userEntity, CUserLevelStat.RATING);
			oldStat.bookmark = (boolean) userEntity.getProperty(CUserLevelStat.BOOKMARK);
		}

		// Set stats
		userEntity.setUnindexedProperty(CUserLevelStat.LAST_PLAYED, levelStat.lastPlayed);
		userEntity.setUnindexedProperty(CUserLevelStat.RATING, levelStat.rating);
		userEntity.setUnindexedProperty(CUserLevelStat.PLAY_COUNT, levelStat.cPlayed);
		userEntity.setUnindexedProperty(CUserLevelStat.CLEAR_COUNT, levelStat.cCleared);
		userEntity.setUnindexedProperty(CUserLevelStat.BOOKMARK, levelStat.bookmark);
		userEntity.setProperty(CUserLevelStat.UPDATED, mResponse.syncEntity.syncDate);

		// Add tags
		if (!levelStat.tags.isEmpty()) {
			Iterator<Tags> newTagIt = levelStat.tags.iterator();
			while (newTagIt.hasNext()) {
				Tags newTag = newTagIt.next();

				// Tag limit OK
				if (tagIds.size() < ServerConfig.UserInfo.TAGS_MAX) {
					tagIds.add(newTag.getId());
				}
				// Too many tags, remove
				else {
					newTagIt.remove();
				}
			}

			userEntity.setUnindexedProperty(CUserLevelStat.TAGS, tagIds);
		}

		DatastoreUtils.put(userEntity);

		return oldStat;
	}

	/**
	 * Update global level stats
	 * @param levelKey key of the level
	 * @param clientStat new user statistics
	 * @param serverStat old user statistics
	 */
	private void updateGlobalLevelStats(Key levelKey, LevelStat clientStat, LevelStat serverStat) {
		Entity levelEntity = DatastoreUtils.getSingleEntity(T_LEVEL_STAT, levelKey);

		// No entity for this
		if (levelEntity == null) {
			levelEntity = new Entity(T_LEVEL_STAT, levelKey);
			levelEntity.setProperty(CLevelStat.RATING_AVG, 0d);
			levelEntity.setProperty(CLevelStat.BOOKMARS, 0);
		}


		// Update stats
		// Play count
		incrementProperty(levelEntity, CLevelStat.PLAY_COUNT, clientStat.cPlaysToSync);

		// Clear count
		incrementProperty(levelEntity, CLevelStat.CLEAR_COUNT, clientStat.cClearsToSync);


		// Removed bookmark
		if (serverStat.bookmark && !clientStat.bookmark) {
			incrementProperty(levelEntity, CLevelStat.BOOKMARS, -1);
		}
		// Added bookmark
		else if (!serverStat.bookmark && clientStat.bookmark) {
			incrementProperty(levelEntity, CLevelStat.BOOKMARS, 1);
		}


		// Rating
		if (serverStat.rating != clientStat.rating) {
			// Sum
			incrementProperty(levelEntity, CLevelStat.RATING_SUM, clientStat.rating - serverStat.rating);

			// Added rating
			if (serverStat.rating == 0 && clientStat.rating > 0) {
				incrementProperty(levelEntity, CLevelStat.RATINGS, 1);
			}
			// Removed rating
			else if (clientStat.rating == 0 && serverStat.rating > 0) {
				incrementProperty(levelEntity, CLevelStat.RATINGS, -1);
			}

			// Calculate new average rating
			long sum = (long) levelEntity.getProperty(CLevelStat.RATING_SUM);
			long cRatings = (long) levelEntity.getProperty(CLevelStat.RATINGS);
			double average = ((double) sum) / cRatings;
			levelEntity.setProperty(CLevelStat.RATING_AVG, average);
		}

		DatastoreUtils.put(levelEntity);
	}

	/**
	 * Increment a value to an existing entity
	 * @param entity the entity to use
	 * @param propertyName name of the property to increment
	 * @param value the value to increment with (can be negative)
	 */
	private static void incrementProperty(Entity entity, String propertyName, Number value) {
		Long oldValue = (Long) entity.getProperty(propertyName);
		if (oldValue == null) {
			oldValue = 0L;
		}
		Long newValue = oldValue + value.longValue();
		entity.setProperty(propertyName, newValue);
	}

	/**
	 * Update global level tags
	 * @param levelKey level to update
	 * @param tags new level tags to add to the level
	 */
	private void updateGlobalLevelTags(Key levelKey, ArrayList<Tags> tags) {
		for (Tags tag : tags) {
			FilterWrapper tagFilter = new FilterWrapper(CLevelTag.TAG, tag.getId());
			Entity entity = DatastoreUtils.getSingleEntity(T_TAG, levelKey, tagFilter);

			if (entity == null) {
				entity = new Entity(T_TAG, levelKey);
				entity.setProperty(CLevelTag.TAG, tag.getId());
			}

			incrementProperty(entity, CLevelTag.COUNT, 1);

			DatastoreUtils.put(entity);
		}


		// Update search with the 5 most popular tags
		Query query = new Query(T_TAG, levelKey);
		query.addSort(CLevelTag.COUNT, SortDirection.DESCENDING);
		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(ServerConfig.FetchSizes.TAGS);

		String tagList = "";
		for (Entity entity : preparedQuery.asIterable(fetchOptions)) {
			if (!tagList.isEmpty()) {
				tagList += " ";
			}

			tagList += entity.getProperty(CLevelTag.TAG);
		}

		Document document = SearchUtils.getDocument(SearchTables.LEVEL, KeyFactory.keyToString(levelKey));
		if (document != null) {
			// TODO check if tags needs updating

			// TODO build document
		}
	}

	/**
	 * Get level key for the specified level_id
	 * @param levelId id for the level
	 * @return level key from the level
	 */
	private static Key getLevelKey(UUID levelId) {
		return DatastoreUtils.getSingleKey(T_PUBLISHED, new FilterWrapper(CPublished.RESOURCE_ID, levelId));
	}

	/**
	 * Convert a server entity to a network entity
	 * @param serverEntity entity from datastore
	 * @return entity that can be sent over the network
	 */
	@SuppressWarnings("unchecked")
	private static LevelStat serverToNetworkEntity(Entity serverEntity) {
		LevelStat levelStats = new LevelStat();

		// Set correct level/campaign id
		Entity levelEntity = DatastoreUtils.getEntity((Key) serverEntity.getProperty(CUserLevelStat.LEVEL_KEY));
		if (levelEntity != null) {
			levelStats.id = DatastoreUtils.getUuidProperty(levelEntity, "resource_id");
		} else {
			return null;
		}

		levelStats.bookmark = (boolean) serverEntity.getProperty(CUserLevelStat.BOOKMARK);
		levelStats.cCleared = ((Long) serverEntity.getProperty(CUserLevelStat.CLEAR_COUNT)).intValue();
		levelStats.cPlayed = ((Long) serverEntity.getProperty(CUserLevelStat.PLAY_COUNT)).intValue();
		levelStats.lastPlayed = (Date) serverEntity.getProperty(CUserLevelStat.LAST_PLAYED);
		levelStats.rating = ((Long) serverEntity.getProperty(CUserLevelStat.RATING)).intValue();
		levelStats.updated = (Date) serverEntity.getProperty(CUserLevelStat.UPDATED);

		levelStats.tags = Tags.toTagList((ArrayList<Long>) serverEntity.getProperty(CUserLevelStat.TAGS));

		return levelStats;
	}

	/** User level stats to sync to the client */
	private HashMap<UUID, LevelStat> mUserStatsToClient = new HashMap<>();
	private StatSyncEntity mParameters = null;
	private StatSyncMethodResponse mResponse = new StatSyncMethodResponse();

	// Tables
	private static final String T_PUBLISHED = DatastoreTables.PUBLISHED;
	private static final String T_USER_STAT = DatastoreTables.USER_LEVEL_STAT;
	private static final String T_LEVEL_STAT = DatastoreTables.LEVEL_STAT;
	private static final String T_COMMENT = DatastoreTables.RESOURCE_COMMENT;
	private static final String T_TAG = DatastoreTables.LEVEL_TAG;
}
