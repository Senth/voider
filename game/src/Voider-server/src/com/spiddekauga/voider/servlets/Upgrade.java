package com.spiddekauga.voider.servlets;

import java.io.IOException;

import javax.servlet.ServletException;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Does an upgrade for the server
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Upgrade extends VoiderServlet {
	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		channelService.sendMessage(new ChannelMessage("Senth", "Testing message"));
		return null;
	}
}
