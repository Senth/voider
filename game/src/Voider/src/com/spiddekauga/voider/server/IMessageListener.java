package com.spiddekauga.voider.server;

import com.spiddekauga.voider.network.entities.misc.ChatMessage;

/**
 * Listens to messages
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IMessageListener {
	/**
	 * Called when a message has received.
	 * @param message the message that was receieved
	 */
	void onMessage(ChatMessage<?> message);
}
