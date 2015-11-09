package com.spiddekauga.voider.server.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.misc.ChatMessage;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CConnectedUser;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CMaintenance;
import com.spiddekauga.voider.server.util.ServerConfig.MaintenanceModes;

/**
 * Base class for all Voider servlets.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public abstract class VoiderServlet extends HttpServlet {
	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp);
	}

	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp);
	}

	/**
	 * Handle request here first
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		mRequest = request;
		mResponse = response;

		initSession(request);

		handleRequest();

		saveSession();
	}

	/**
	 * Handle both get and post requests
	 * @throws ServletException
	 * @throws IOException
	 */
	protected abstract void handleRequest() throws ServletException, IOException;

	/**
	 * Initializes the session and all it's variables
	 * @param request server request
	 */
	private void initSession(HttpServletRequest request) {
		mSession = request.getSession();

		// Initialize user
		initUser();
	}

	/**
	 * Initializes the user
	 */
	private void initUser() {
		Object object = getSessionVariable(SessionVariableNames.USER);

		// Found user
		if (object instanceof User) {
			mUser = (User) object;
		}
		// Create new user
		else {
			mUser = new User();
		}
	}

	/**
	 * Saves the session variables
	 */
	private void saveSession() {
		if (mUser.isChanged()) {
			setSessionVariable(SessionVariableNames.USER, mUser);
		}
	}

	/**
	 * Send a message
	 * @param receiver who we should send the message to
	 * @param chatMessage sends the specified chat message
	 */
	protected void sendMessage(ChatMessageReceivers receiver, ChatMessage<?> chatMessage) {
		Gson gson = new Gson();
		String json = gson.toJson(chatMessage);

		Iterable<Entity> sendTo = new ArrayList<>();

		switch (receiver) {
		case SELF:
			if (mUser.isLoggedIn()) {
				sendTo = DatastoreUtils.getEntities(DatastoreTables.CONNECTED_USER, mUser.getKey());
			} else {
				mLogger.warning("User isn't logged in when trying to send a message to SELF");
			}
			break;

		case ALL:
			sendTo = DatastoreUtils.getEntities(DatastoreTables.CONNECTED_USER);
			break;
		}

		for (Entity entity : sendTo) {
			String channelId = (String) entity.getProperty(CConnectedUser.CHANNEL_ID);
			sendMessage(channelId, json);
		}
	}

	/**
	 * Send a message to a specific client
	 * @param channelId
	 * @param message the message to send in json format
	 */
	private void sendMessage(String channelId, String message) {
		if (channelId != null) {
			ChannelMessage channelMessage = new ChannelMessage(channelId, message);
			mChannelService.sendMessage(channelMessage);
		}
	}

	/**
	 * Send an email
	 * @param email who to send the email to
	 * @param subject
	 * @param content HTML content of the email
	 */
	protected void sendEmail(String email, String subject, String content) {
		Session session = Session.getDefaultInstance(new Properties());
		MimeMessage message = new MimeMessage(session);

		try {
			message.setFrom(ServerConfig.EMAIL_ADMIN);
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
			message.setSubject(subject);
			message.setContent(content, "text/html");
			Transport.send(message);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets a session variable
	 * @param name the session's variable name
	 * @return the variable stored in this place, or null if not found
	 */
	protected Object getSessionVariable(SessionVariableNames name) {
		return mSession.getAttribute(name.name());
	}

	/**
	 * Sets a session variable
	 * @param name the session's variable name
	 * @param variable the variable to set in the session
	 */
	protected void setSessionVariable(SessionVariableNames name, Object variable) {
		mSession.setAttribute(name.name(), variable);
	}

	/**
	 * @return current request
	 */
	protected HttpServletRequest getRequest() {
		return mRequest;
	}

	/**
	 * @return current response
	 */
	protected HttpServletResponse getResponse() {
		return mResponse;
	}

	/**
	 * @return current maintenance mode
	 */
	protected MaintenanceModes getMaintenanceMode() {
		Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.MAINTENANCE);

		MaintenanceModes mode = MaintenanceModes.UP;

		if (entity != null) {
			String modeString = (String) entity.getProperty(CMaintenance.MODE);
			if (modeString != null) {
				mode = MaintenanceModes.fromString(modeString);
			}
		}

		return mode;
	}

	/**
	 * All session variable enumerations
	 */
	protected enum SessionVariableNames {
		/** The logged in user */
		USER,
	}

	/**
	 * Who we can send messages to
	 */
	public enum ChatMessageReceivers {
		/** Self, all clients (if logged in) */
		SELF,
		/** Broadcast to everyone */
		ALL,
	}

	/** Current user */
	protected User mUser = null;
	/** Logger */
	protected Logger mLogger = Logger.getLogger(getClass().getSimpleName());

	private HttpSession mSession = null;
	private HttpServletRequest mRequest = null;
	private HttpServletResponse mResponse = null;

	/** Channel service for sending messages */
	private static ChannelService mChannelService = ChannelServiceFactory.getChannelService();
}
