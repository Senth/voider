package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
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
import com.spiddekauga.voider.network.entities.stat.StatSyncEntity;
import com.spiddekauga.voider.network.entities.stat.StatSyncEntity.LevelStats;
import com.spiddekauga.voider.network.entities.stat.StatSyncMethod;
import com.spiddekauga.voider.network.entities.stat.StatSyncMethodResponse;
import com.spiddekauga.voider.network.entities.stat.StatSyncMethodResponse.Statuses;
import com.spiddekauga.voider.network.entities.stat.Tags;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Synchronizes various statisticts
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
			LevelStats userLevelStats = serverToNetworkEntity(serverEntity);
			mUserStatsToClient.put(userLevelStats.id, userLevelStats);
			mResponse.syncEntity.levelStats.add(userLevelStats);
		}
	}

	/**
	 * Check and resolve sync conflicts
	 */
	private void checkAndResolveConflicts() {
		Iterator<LevelStats> fromClientIt = mParameters.levelStats.iterator();
		while (fromClientIt.hasNext()) {
			LevelStats clientStats = fromClientIt.next();
			LevelStats serverStats = mUserStatsToClient.get(clientStats.id);

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
	private static void fixPlayAndClearCountConflict(LevelStats clientStats, LevelStats serverStats) {
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
	private void fixMiscUserLevelStatConflict(LevelStats clientStats, LevelStats serverStats) {
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
	private static void setUserLevelStats(LevelStats from, LevelStats to) {
		to.bookmark = from.bookmark;
		to.rating = from.rating;
		to.tags = from.tags;
	}

	/**
	 * Sync to server
	 */
	private void syncToServer() {
		// TODO
		// Update user_level_stat


		// Update level_stat
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
	private static LevelStats serverToNetworkEntity(Entity serverEntity) {
		LevelStats levelStats = new LevelStats();

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
	private HashMap<UUID, LevelStats> mUserStatsToClient = new HashMap<>();
	/** Parameters */
	private StatSyncEntity mParameters = null;
	/** Response */
	private StatSyncMethodResponse mResponse = new StatSyncMethodResponse();
}
