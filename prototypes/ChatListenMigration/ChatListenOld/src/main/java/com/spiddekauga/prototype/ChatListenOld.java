package com.spiddekauga.prototype;


/**
 * Uses the old Channel API to listen to the chat
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ChatListenOld {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ChatGateway chatGateway = new ChatGateway("OLD");
		chatGateway.run();
	}
}
