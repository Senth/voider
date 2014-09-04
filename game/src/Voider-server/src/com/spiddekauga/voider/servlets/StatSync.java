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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
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
		}

		return mResponse;
	}

	/**
	 * Get stats to sync to the client
	 */
	private void syncToClient() {
		// Only newer than last sync
		FilterWrapper filterLastSync = new FilterWrapper("updated", FilterOperator.GREATER_THAN, mParameters.syncDate);

		Iterable<Entity> entities = DatastoreUtils.getEntities("user_level_stat", mUser.getKey(), filterLastSync);

		for (Entity serverEntity : entities) {
			LevelStat userLevelStats = serverToNetworkEntity(serverEntity);
			mUserStatsToClient.put(userLevelStats.id, userLevelStats);
			mResponse.syncEntity.levelStats.add(userLevelStats);
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

		int cCleared = serverStats.cCleared + clientStats.cCleared;
		serverStats.cCleared = cCleared;
		clientStats.cCleared = cCleared;
	}

	/**
	 * Set latest settings for level/ćampaign user stats
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
		to.tags = from.tags;
	}

	/**
	 * Sync to server
	 */
	private void syncToServer() {
		for (LevelStat levelStat : mParameters.levelStats) {
			Key levelKey = getLevelKey(levelStat.id);

			if (levelKey != null) {
				LevelStat oldStat = updateUserLevelStats(levelKey, levelStat);
				updateLevelStats(levelKey, levelStat, oldStat);
				updateLevelTags(levelKey, levelStat.tags);
			}
		}

		// Send sync response
		if (mParameters.levelStats.isEmpty()) {
			sendMessage(new ChatMessage<>(MessageTypes.SYNC_STAT, mUser.getClientId()));
		}
	}

	/**
	 * Update user level stats
	 * @param levelKey key of the level
	 * @param levelStat stats to set
	 * @return old statistics
	 */
	private LevelStat updateUserLevelStats(Key levelKey, LevelStat levelStat) {
		FilterWrapper levelFilter = new FilterWrapper("level_key", levelKey);
		Entity userEntity = DatastoreUtils.getSingleEntity("user_level_stat", mUser.getKey(), levelFilter);

		ArrayList<Integer> tagIds = Tags.toIdList(levelStat.tags);
		LevelStat oldStat = new LevelStat();

		// No entity for this level exists yet
		if (userEntity == null) {
			userEntity = new Entity("user_level_key", mUser.getKey());
			userEntity.setProperty("level_key", levelKey);
		}
		// Add old tags and set old rating
		else {
			@SuppressWarnings("unchecked")
			Collection<Long> oldTagIds = (Collection<Long>) userEntity.getProperty("tags");
			if (oldTagIds != null) {
				for (Long oldTagId : oldTagIds) {
					tagIds.add(oldTagId.intValue());
				}
			}

			oldStat.rating = DatastoreUtils.getIntProperty(userEntity, "rating");
			oldStat.bookmark = (boolean) userEntity.getProperty("bookmark");
		}

		// Set stats
		userEntity.setUnindexedProperty("last_played", levelStat.lastPlayed);
		userEntity.setUnindexedProperty("rating", levelStat.rating);
		userEntity.setUnindexedProperty("play_count", levelStat.cPlayed);
		userEntity.setUnindexedProperty("clear_count", levelStat.cCleared);
		userEntity.setUnindexedProperty("bookmark", levelStat.bookmark);
		userEntity.setProperty("updated", mResponse.syncEntity.syncDate);

		if (!tagIds.isEmpty()) {
			userEntity.setUnindexedProperty("tags", tagIds);
		}

		DatastoreUtils.put(userEntity);

		return oldStat;
	}

	/**
	 * Update level stats
	 * @param levelKey key of the level
	 * @param newStat new user statistics
	 * @param oldStat old user statistics
	 */
	private void updateLevelStats(Key levelKey, LevelStat newStat, LevelStat oldStat) {
		Entity levelEntity = DatastoreUtils.getSingleEntity("level_stat", levelKey);

		// No entity for this
		if (levelEntity == null) {
			levelEntity = new Entity("level_stat", levelKey);
			levelEntity.setProperty("rating_avg", 0d);
			levelEntity.setProperty("bookmarks", 0);
		}


		// Update stats
		// Play count
		incrementProperty(levelEntity, "play_count", newStat.cPlaysToSync);

		// Clear count
		incrementProperty(levelEntity, "clear_count", newStat.cClearsToSync);


		// Removed bookmark
		if (oldStat.bookmark && !newStat.bookmark) {
			incrementProperty(levelEntity, "bookmarks", -1);
		}
		// Added bookmark
		else if (!oldStat.bookmark && newStat.bookmark) {
			incrementProperty(levelEntity, "bookmarks", 1);
		}


		// Rating
		if (oldStat.rating != newStat.rating) {
			// Sum
			incrementProperty(levelEntity, "rating_sum", newStat.rating - oldStat.rating);

			// Added rating
			if (oldStat.rating == 0 && newStat.rating > 0) {
				incrementProperty(levelEntity, "ratings", 1);
			}
			// Removed rating
			else if (newStat.rating == 0 && oldStat.rating > 0) {
				incrementProperty(levelEntity, "ratings", -1);
			}

			// Calculate new average rating
			long sum = (long) levelEntity.getProperty("rating_sum");
			long cRatings = (long) levelEntity.getProperty("ratings");
			double average = ((double) sum) / cRatings;
			levelEntity.setProperty("rating_avg", average);
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
	 * Update level tags
	 * @param levelKey level to update
	 * @param tags new level tags to add to the level
	 */
	private void updateLevelTags(Key levelKey, ArrayList<Tags> tags) {
		for (Tags tag : tags) {
			FilterWrapper tagFilter = new FilterWrapper("tag", tag.getId());
			Entity entity = DatastoreUtils.getSingleEntity("level_tag", levelKey, tagFilter);

			if (entity == null) {
				entity = new Entity("level_tag", levelKey);
				entity.setProperty("tag", tag.getId());
			}

			incrementProperty(entity, "count", 1);

			DatastoreUtils.put(entity);
		}
	}

	/**
	 * Get level key for the specified level_id
	 * @param levelId id for the level
	 * @return level key from the level
	 */
	private static Key getLevelKey(UUID levelId) {
		return DatastoreUtils.getSingleKey("published", new FilterWrapper("resource_id", levelId));
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
		Entity levelEntity = DatastoreUtils.getEntity((Key) serverEntity.getProperty("level_key"));
		if (levelEntity != null) {
			levelStats.id = DatastoreUtils.getUuidProperty(levelEntity, "resource_id");
		} else {
			return null;
		}

		levelStats.bookmark = (boolean) serverEntity.getProperty("bookmark");
		levelStats.cCleared = ((Long) serverEntity.getProperty("clear_count")).intValue();
		levelStats.cPlayed = ((Long) serverEntity.getProperty("play_count")).intValue();
		levelStats.lastPlayed = (Date) serverEntity.getProperty("last_played");
		levelStats.rating = ((Long) serverEntity.getProperty("rating")).intValue();

		levelStats.tags = Tags.toTagList((ArrayList<Long>) serverEntity.getProperty("tags"));

		return levelStats;
	}

	/** User level stats to sync to the client */
	private HashMap<UUID, LevelStat> mUserStatsToClient = new HashMap<>();
	/** Parameters */
	private StatSyncEntity mParameters = null;
	/** Response */
	private StatSyncMethodResponse mResponse = new StatSyncMethodResponse();
}
