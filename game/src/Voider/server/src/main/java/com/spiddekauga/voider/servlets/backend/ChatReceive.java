package com.spiddekauga.voider.servlets.backend;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.spiddekauga.voider.server.util.VoiderController;

/**
 * Forwards messages...
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ChatReceive extends VoiderController {
	@Override
	protected void onRequest() {
		String channelKey = getParameter("channelKey");
		String message = getParameter("message");

		mLogger.info("Content-Type: " + getRequest().getContentType() + ", Channel Key: " + channelKey + ", Message: " + message);

		// Forward message to all channels with this key
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		channelService.sendMessage(new ChannelMessage(channelKey, message));
	}
}
