package com.spiddekauga.voider.servlets.backend;

import com.spiddekauga.voider.server.util.VoiderController;

/**
 * Forwards messages...

 */
@SuppressWarnings("serial")
public class ChatReceive extends VoiderController {
	@Override
	protected void onRequest() {
		String channelKey = getParameter("channelKey");
		String message = getParameter("message");

		mLogger.info("Content-Type: " + getRequest().getContentType() + ", Channel Key: " + channelKey + ", Message: " + message);
	}
}
