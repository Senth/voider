package com.spiddekauga.voider.server;

import com.spiddekauga.voider.network.misc.ChatMessage;

/**
 * Listens to messages
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IMessageListener {
	/**
	 * Called when a message has received.
	 * @param message the message that was received
	 */
	void onMessage(ChatMessage<?> message);
}
