package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.entities.ChatMessage;
import com.spiddekauga.voider.network.entities.ChatMessage.MessageTypes;
import com.spiddekauga.voider.network.entities.HighscoreSyncEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.SyncHighscoreMethod;
import com.spiddekauga.voider.network.entities.method.SyncHighscoreMethodResponse;
import com.spiddekauga.voider.network.entities.method.SyncHighscoreMethodResponse.Statuses;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Synchronizes highscores with clients
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class SyncHighscore extends VoiderServlet {

	/**
	 * Initializes the sync
	 */
	@Override
	protected void onInit() {
		mResponse = new SyncHighscoreMethodResponse();
		mResponse.status = Statuses.FAILED_INTERNAL;
		mResponse.syncTime = new Date();
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			mResponse.status = Statuses.FAILED_USER_NOT_LOGGED_IN;
			return mResponse;
		}

		if (methodEntity instanceof SyncHighscoreMethod) {
			setHighscoresToSyncToClient((SyncHighscoreMethod) methodEntity);
			checkForConflictsAndResolve((SyncHighscoreMethod) methodEntity);
			syncNewToClient();
			syncNewToServer((SyncHighscoreMethod) methodEntity);
			mResponse.status = Statuses.SUCCESS;

			sendSyncMessage((SyncHighscoreMethod) methodEntity);
		}

		return mResponse;
	}

	/**
	 * Set highscores to sync from server to client
	 * @param methodEntity parameters sent to the server
	 */
	private void setHighscoresToSyncToClient(SyncHighscoreMethod methodEntity) {
		// Get all highscore with later upload date then last sync
		Query query = new Query("highscore", mUser.getKey());

		// Only older than last sync
		Filter filter = new Query.FilterPredicate("uploaded", FilterOperator.GREATER_THAN, methodEntity.lastSync);
		query.setFilter(filter);

		// Only retrieve necessary element (skip uploaded)
		DatastoreUtils.createUuidProjection(query, "level_id");
		query.addProjection(new PropertyProjection("score", Long.class));
		query.addProjection(new PropertyProjection("created", Date.class));

		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);

		for (Entity serverEntity : preparedQuery.asIterable()) {
			HighscoreSyncEntity networkEntity = serverToNetworkEntity(serverEntity);
			mHighscoresToClient.put(networkEntity.levelId, networkEntity);
		}
	}

	/**
	 * Check for conflicts and remove highscores that should be sync both to and from the
	 * server (i.e. from and to the client).
	 * @param methodEntity parameters sent to the server
	 */
	private void checkForConflictsAndResolve(SyncHighscoreMethod methodEntity) {
		Iterator<HighscoreSyncEntity> fromClientIt = methodEntity.highscores.iterator();
		while (fromClientIt.hasNext()) {
			HighscoreSyncEntity clientHighscore = fromClientIt.next();
			HighscoreSyncEntity serverHighscore = mHighscoresToClient.get(clientHighscore.levelId);

			// Found conflict -> Resolve
			if (serverHighscore != null) {
				// Use client score
				if (clientHighscore.score > serverHighscore.score) {
					mHighscoresToClient.remove(clientHighscore.levelId);
				}
				// Use server score
				else if (serverHighscore.score > clientHighscore.score) {
					fromClientIt.remove();
				}
				// Delete both
				else {
					fromClientIt.remove();
					mHighscoresToClient.remove(clientHighscore.levelId);
				}
			}
		}
	}

	/**
	 * Sync new highscores to the client
	 */
	private void syncNewToClient() {
		Iterator<Entry<UUID, HighscoreSyncEntity>> entityIt = mHighscoresToClient.entrySet().iterator();
		while (entityIt.hasNext()) {
			mResponse.highscores.add(entityIt.next().getValue());
		}
	}

	/**
	 * Sync new highscores to the server
	 * @param methodEntity parameters sent to the server
	 */
	private void syncNewToServer(SyncHighscoreMethod methodEntity) {
		// Check if we should update existing entity?
		for (HighscoreSyncEntity networkEntity : methodEntity.highscores) {

			FilterWrapper filter = new FilterWrapper("level_id", networkEntity.levelId);
			Entity entity = DatastoreUtils.getSingleEntity("highscore", mUser.getKey(), filter);

			if (entity == null) {
				entity = new Entity("highscore", mUser.getKey());
				DatastoreUtils.setProperty(entity, "level_id", networkEntity.levelId);
			}

			entity.setProperty("score", networkEntity.score);
			entity.setProperty("created", networkEntity.created);
			entity.setProperty("uploaded", mResponse.syncTime);
			DatastoreUtils.put(entity);
		}
	}

	/**
	 * Sends a sync message to all connected clients
	 * @param methodEntity parameters sent to the server
	 */
	private void sendSyncMessage(SyncHighscoreMethod methodEntity) {
		if (mResponse.isSuccessful()) {
			// Only send sync message if something was updated in the server
			if (!methodEntity.highscores.isEmpty()) {
				ChatMessage<Object> chatMessage = new ChatMessage<>();
				chatMessage.skipClient = mUser.getClientId();
				chatMessage.type = MessageTypes.SYNC_HIGHSCORE;
				sendMessage(chatMessage);
			}
		}
	}

	/**
	 * Convert a server entity to network entity
	 * @param serverEntity entity from datastore
	 * @return entity that can be sent over the network
	 */
	private static HighscoreSyncEntity serverToNetworkEntity(Entity serverEntity) {
		HighscoreSyncEntity networkEntity = new HighscoreSyncEntity();
		networkEntity.levelId = DatastoreUtils.getUuidProperty(serverEntity, "level_id");
		networkEntity.score = DatastoreUtils.getIntProperty(serverEntity, "score");
		networkEntity.created = (Date) serverEntity.getProperty("created");

		return networkEntity;
	}


	/** Highscores to sync to the client */
	private HashMap<UUID, HighscoreSyncEntity> mHighscoresToClient = new HashMap<>();
	/** Response */
	private SyncHighscoreMethodResponse mResponse;
}
