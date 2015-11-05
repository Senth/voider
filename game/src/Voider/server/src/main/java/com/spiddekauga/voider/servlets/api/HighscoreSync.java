package com.spiddekauga.voider.servlets.api;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.misc.ChatMessage;
import com.spiddekauga.voider.network.misc.ChatMessage.MessageTypes;
import com.spiddekauga.voider.network.stat.HighscoreSyncEntity;
import com.spiddekauga.voider.network.stat.HighscoreSyncMethod;
import com.spiddekauga.voider.network.stat.HighscoreSyncResponse;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Synchronizes highscores with clients
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class HighscoreSync extends VoiderApiServlet<HighscoreSyncMethod> {

	/**
	 * Initializes the sync
	 */
	@Override
	protected void onInit() {
		mResponse = new HighscoreSyncResponse();
		mResponse.status = GeneralResponseStatuses.FAILED_SERVER_ERROR;
		mResponse.syncTime = new Date();
	}

	@Override
	protected IEntity onRequest(HighscoreSyncMethod method) throws ServletException, IOException {
		if (!mUser.isLoggedIn()) {
			mResponse.status = GeneralResponseStatuses.FAILED_USER_NOT_LOGGED_IN;
			return mResponse;
		}

		setHighscoresToSyncToClient(method);
		checkAndResolveConflicts(method);
		syncNewToClient();
		syncNewToServer(method);
		mResponse.status = GeneralResponseStatuses.SUCCESS;

		sendSyncMessage(method);

		return mResponse;
	}

	/**
	 * Set highscores to sync from server to client
	 * @param methodEntity parameters sent to the server
	 */
	private void setHighscoresToSyncToClient(HighscoreSyncMethod methodEntity) {
		// Only newer than last sync
		FilterWrapper filterUploaded = new FilterWrapper("uploaded", FilterOperator.GREATER_THAN, methodEntity.lastSync);
		FilterWrapper filterUsername = new FilterWrapper("username", mUser.getUsername());

		Iterable<Entity> entities = DatastoreUtils.getEntities("highscore", filterUploaded, filterUsername);

		for (Entity serverEntity : entities) {
			HighscoreSyncEntity networkEntity = serverToNetworkEntity(serverEntity);
			mHighscoresToClient.put(networkEntity.levelId, networkEntity);
		}
	}

	/**
	 * Check for conflicts and remove highscores that should be sync both to and from the
	 * server (i.e. from and to the client).
	 * @param methodEntity parameters sent to the server
	 */
	private void checkAndResolveConflicts(HighscoreSyncMethod methodEntity) {
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
	private void syncNewToServer(HighscoreSyncMethod methodEntity) {
		// Check if we should update existing entity?
		for (HighscoreSyncEntity networkEntity : methodEntity.highscores) {

			FilterWrapper levelFilter = new FilterWrapper("level_id", networkEntity.levelId);
			FilterWrapper usernameFilter = new FilterWrapper("username", mUser.getUsername());

			Entity entity = DatastoreUtils.getSingleEntity("highscore", mUser.getKey(), levelFilter, usernameFilter);

			if (entity == null) {
				entity = new Entity("highscore", getLevelKey(networkEntity.levelId));
				DatastoreUtils.setProperty(entity, "level_id", networkEntity.levelId);
				entity.setProperty("username", mUser.getUsername());
			}

			entity.setProperty("score", networkEntity.score);
			entity.setProperty("created", networkEntity.created);
			entity.setProperty("uploaded", mResponse.syncTime);
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
	 * Sends a sync message to all connected clients
	 * @param methodEntity parameters sent to the server
	 */
	private void sendSyncMessage(HighscoreSyncMethod methodEntity) {
		if (mResponse.isSuccessful()) {
			// Only send sync message if something was updated in the server
			if (!methodEntity.highscores.isEmpty()) {
				ChatMessage<Object> chatMessage = new ChatMessage<>();
				chatMessage.skipClient = mUser.getClientId();
				chatMessage.type = MessageTypes.SYNC_HIGHSCORE;
				sendMessage(ChatMessageReceivers.SELF, chatMessage);
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
		networkEntity.levelId = DatastoreUtils.getPropertyUuid(serverEntity, "level_id");
		networkEntity.score = DatastoreUtils.getPropertyInt(serverEntity, "score", 0);
		networkEntity.created = (Date) serverEntity.getProperty("created");

		return networkEntity;
	}


	/** Highscores to sync to the client */
	private HashMap<UUID, HighscoreSyncEntity> mHighscoresToClient = new HashMap<>();
	/** Response */
	private HighscoreSyncResponse mResponse;
}
