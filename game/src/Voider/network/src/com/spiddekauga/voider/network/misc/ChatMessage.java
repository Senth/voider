package com.spiddekauga.voider.network.misc;

import java.io.Serializable;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Chat messages from server to client, or vice versa
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 * @param <DataType> type of data to store
 */
public class ChatMessage<DataType> implements IEntity, Serializable {
	private static final long serialVersionUID = 1L;
	/** Type of message */
	public MessageTypes type = null;
	/** This client will skip processing the message */
	public UUID skipClient = null;
	/** Optional data */
	public DataType data = null;

	/**
	 * Creates an empty and invalid chat message
	 */
	public ChatMessage() {
		// Does nothing
	}

	/**
	 * Creates a message with just a message type
	 * @param type the type of the message
	 */
	public ChatMessage(MessageTypes type) {
		this.type = type;
	}

	/**
	 * Creates a message with a type and data
	 * @param type
	 * @param data
	 */
	public ChatMessage(MessageTypes type, DataType data) {
		this.type = type;
		this.data = data;
	}

	/**
	 * Creates a message with a type and the client to skip
	 * @param type
	 * @param skipClient the client that should skip process this message
	 */
	public ChatMessage(MessageTypes type, UUID skipClient) {
		this.type = type;
		this.skipClient = skipClient;
	}

	/**
	 * Creates a message with a type and data
	 * @param type
	 * @param data
	 * @param skipClient the client that should skip process this message
	 */
	public ChatMessage(MessageTypes type, DataType data, UUID skipClient) {
		this.type = type;
		this.data = data;
		this.skipClient = skipClient;
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
		SYNC_STAT,
	}
}
