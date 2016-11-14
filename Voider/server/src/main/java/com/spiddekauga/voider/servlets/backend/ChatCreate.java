package com.spiddekauga.voider.servlets.backend;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CConnectedUser;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Creates a chat connection (or closes one)

 */
@SuppressWarnings("serial")
public class ChatCreate extends VoiderServlet {

	@Override
	protected void handleRequest() throws ServletException, IOException {
		String channelKey = getRequest().getParameter("c");

		// Create a Channel using the 'channelKey' we received from the client
		String token = mChannelService.createChannel(channelKey);

		setChannelKey(channelKey);

		// Send the client the 'token' + the 'channelKey' this way the client
		// can start using the new channel
		getResponse().setContentType("text/html");
		StringBuffer sb = new StringBuffer();
		sb.append("{\"channelKey\":\"");
		sb.append(channelKey);
		sb.append("\",\"token\":\"");
		sb.append(token);
		sb.append("\"}");
		getResponse().getWriter().write(sb.toString());
	}

	/**
	 * Set/Add channel key
	 * @param channelKey the channel key
	 */
	private void setChannelKey(String channelKey) {
		mUser.setChannelId(UUID.fromString(channelKey));

		// Add to Datastore
		Entity entity = new Entity(DatastoreTables.CONNECTED_USER, mUser.getKey());
		entity.setProperty(CConnectedUser.CHANNEL_ID, channelKey);
		entity.setProperty(CConnectedUser.CONNECTED_TIME, new Date());

		DatastoreUtils.put(entity);
	}

	private static ChannelService mChannelService = ChannelServiceFactory.getChannelService();
}
