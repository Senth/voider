package com.spiddekauga.voider.network.misc;

import com.spiddekauga.voider.network.entities.IEntity;

import java.io.Serializable;

/**
 * Chat messages from server to client, or vice versa
 * @param <DataType> type of data to store
 */
public class ServerMessage<DataType> implements IEntity, Serializable {
private static final long serialVersionUID = 1L;
/** Type of message */
public MessageTypes type = null;
/** Optional data */
public DataType data = null;

/**
 * Creates an empty and invalid chat message
 */
public ServerMessage() {
	// Does nothing
}

/**
 * Creates a message with just a message type
 * @param type the type of the message
 */
public ServerMessage(MessageTypes type) {
	this.type = type;
}

/**
 * Creates a message with a type and data
 * @param type
 * @param data
 */
public ServerMessage(MessageTypes type, DataType data) {
	this.type = type;
	this.data = data;
}

/**
 * All message types when sending messages from channel/chat.
 */
public enum MessageTypes {
	/** Synchronize downloaded resources */
	SYNC_COMMUNITY_DOWNLOAD,
	/** Synchronize user resources */
	SYNC_USER_RESOURCES,
	/** Synchronize highscores */
	SYNC_HIGHSCORE,
	/** Synchronize statistics */
	SYNC_STATS,
	/** Server maintenance */
	SERVER_MAINTENANCE,
	/** New MOTD message */
	MOTD,
}
}
