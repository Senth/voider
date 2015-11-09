package com.spiddekauga.voider.servlets.backend;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Called when a channel has been disconnected
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ChatDisconnected extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		mLogger.info(req.getParameterMap().toString());
		// TODO what's this?

		removeChannelFromDatastore();
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	/**
	 * Remove channel key when the user is disconnect
	 */
	private void removeChannelFromDatastore() {

		// TODO
		// FilterWrapper channelFilter = new FilterWrapper(CConnectedUser.CHANNEL_ID,
		// channelId);
		// Key key = DatastoreUtils.getSingleKey(DatastoreTables.CONNECTED_USER,
		// channelFilter);
		//
		// if (key != null) {
		// DatastoreUtils.delete(key);
		// }
	}

	private final Logger mLogger = Logger.getLogger(getClass().getSimpleName());
}
