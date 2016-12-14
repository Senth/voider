package com.spiddekauga.voider.server.util;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.entities.NetworkEntitySerializer;
import com.spiddekauga.voider.network.misc.ServerMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Helper class for sending messages
 */
public class MessageSender {
private static final Logger mLogger = Logger.getLogger(MessageSender.class.getName());

/**
 * Send a message through the Channel API
 * @param receiver who we should send the message to
 * @param senderKey user key in the datastore, can be null if receiver is {@link Receivers#ALL}.
 * @param serverMessage sends the specified chat message
 */
public static void sendMessage(Receivers receiver, Key senderKey, ServerMessage<?> serverMessage) {
	List<String> sendToIds = getClientIds(receiver, senderKey);
	String serializedMessage = NetworkEntitySerializer.serializeServerMessage(serverMessage);

	NetworkGateway.sendMessage(sendToIds, serializedMessage);
}

/**
 * Get all receivers of the server message
 * @param receiver who we should send the message to
 * @param senderKey user key in the datastore, can be null if receiver is {@link Receivers#ALL}.
 * @return list with all clients we should send the message to
 */
private static List<String> getClientIds(Receivers receiver, Key senderKey) {
	Iterable<Entity> sendTo = new ArrayList<>();
	switch (receiver) {
	case SELF_ALL:
		if (senderKey != null) {
			sendTo = DatastoreUtils.getEntities(DatastoreTables.CONNECTED_USER, senderKey);
		} else {
			mLogger.warning("User isn't logged in when trying to send a message to SELF");
		}
		break;

	case SELF_OTHERS:
		if (senderKey != null) {
			DatastoreUtils.FilterWrapper notThisClient = new DatastoreUtils.FilterWrapper(DatastoreTables.CConnectedUser.CHANNEL_ID, Query.FilterOperator.NOT_EQUAL, senderKey);
			sendTo = DatastoreUtils.getEntities(DatastoreTables.CONNECTED_USER, senderKey, notThisClient);
		} else {
			mLogger.warning("User isn't logged in when trying to send a message to SELF");
		}
		break;

	case ALL:
		sendTo = DatastoreUtils.getEntities(DatastoreTables.CONNECTED_USER);
		break;
	}

	List<String> sendToIds = new ArrayList<>();
	for (Entity entity : sendTo) {
		String channelId = (String) entity.getProperty(DatastoreTables.CConnectedUser.CHANNEL_ID);
		sendToIds.add(channelId);
	}

	return sendToIds;
}

/**
 * Who we can send messages to
 */
public enum Receivers {
	/** Self, all clients (if logged in) */
	SELF_ALL,
	/** Send to all other clients the user is connected to */
	SELF_OTHERS,
	/** Broadcast to everyone */
	ALL,
}
}
