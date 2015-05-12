package com.spiddekauga.voider.servlets.backend;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

/**
 * Creates a chat connection (or closes one)
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ChatCreate extends HttpServlet {

	@Override
	protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String channelKey = request.getParameter("c");

		// Create a Channel using the 'channelKey' we received from the client
		String token = mChannelService.createChannel(channelKey);

		// Send the client the 'token' + the 'channelKey' this way the client
		// can start using the new channel
		response.setContentType("text/html");
		StringBuffer sb = new StringBuffer();
		sb.append("{\"channelKey\":\"");
		sb.append(channelKey);
		sb.append("\",\"token\":\"");
		sb.append(token);
		sb.append("\"}");
		response.getWriter().write(sb.toString());
	}

	private static ChannelService mChannelService = ChannelServiceFactory.getChannelService();
}
