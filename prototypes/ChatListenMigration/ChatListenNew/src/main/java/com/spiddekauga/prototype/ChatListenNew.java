package com.spiddekauga.prototype;

import java.net.CookieManager;
import java.net.CookiePolicy;

/**
 * Listens to the new Channel API.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ChatListenNew {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CookieManager.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		ChatGateway chatGateway = new ChatGateway("NEW");
		chatGateway.run();
	}

}
