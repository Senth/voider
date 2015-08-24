package com.spiddekauga.voider.server.util;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.gson.Gson;
import com.spiddekauga.appengine.BlobUtils;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.NetworkEntitySerializer;
import com.spiddekauga.voider.network.misc.ChatMessage;


/**
 * Wrapper for the Voider servlet
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 * @param <Method> Method from the client
 */
@SuppressWarnings("serial")
public abstract class VoiderApiServlet<Method extends IMethodEntity> extends HttpServlet {
	/**
	 * Called by the server to handle a post or get call.
	 * @param method the entity that was sent to the method
	 * @return response entity
	 * @throws IOException if an input or output error is detected when the servlet
	 *         handles the GET/POST request
	 * @throws ServletException if the request for the GET/POST could not be handled
	 */
	protected abstract IEntity onRequest(Method method) throws ServletException, IOException;

	/**
	 * Initializes the servlet
	 */
	protected abstract void onInit();

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

	@Override
	protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handleRequest(request, response);
	}

	@Override
	protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handleRequest(request, response);
	}

	/**
	 * Wrapper for handling do/get
	 * @param request the server request from the client
	 * @param response the response to send to the client
	 * @throws IOException if an input or output error is detected when the servlet
	 *         handles the GET/POST request
	 * @throws ServletException if the request for the GET/POST could not be handled
	 */
	@SuppressWarnings("unchecked")
	private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		mRequest = request;
		mResponse = response;

		// Initialize
		initLogger();
		initSession(request);

		// Get method parameters
		try {
			byte[] byteEntity = NetworkGateway.getEntity(mRequest);
			Method methodEntity = null;
			if (byteEntity != null) {
				methodEntity = (Method) NetworkEntitySerializer.deserializeEntity(byteEntity);
			}

			// Handle request
			onInit();
			IEntity responseEntity = onRequest(methodEntity);

			// Send response
			if (responseEntity != null) {
				byte[] responseBytes = NetworkEntitySerializer.serializeEntity(responseEntity);
				NetworkGateway.sendResponse(mResponse, responseBytes);
			}
		} catch (ClassCastException e) {
			// Wrong type of method. Doesn't work
		}

		// Save
		saveSession();
	}

	/**
	 * Initializes the logger
	 */
	private void initLogger() {
		if (mLogger == null) {
			mLogger = Logger.getLogger(getClass().getName());
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
	 * @return get blob information from the current request, null if no uploads were
	 *         made.
	 */
	protected Map<UUID, BlobKey> getUploadedBlobs() {
		return BlobUtils.getBlobKeysFromUpload(mRequest);
	}

	/**
	 * @return get blob information from the current request where the uploaded resources
	 *         contains revisions, null if no uploads were made.
	 */
	protected Map<UUID, Map<Integer, BlobKey>> getUploadedRevisionBlobs() {
		return BlobUtils.getBlobKeysFromUploadRevision(mRequest);
	}

	/**
	 * @return response of the current request
	 */
	protected HttpServletResponse getResponse() {
		return mResponse;
	}

	/**
	 * @return current request
	 */
	protected HttpServletRequest getRequest() {
		return mRequest;
	}

	/**
	 * Sends a message to all clients. If skipClient has been set the client with that id
	 * will not act on the message.
	 * @param chatMessage sends the specified chat message
	 */
	protected void sendMessage(ChatMessage<?> chatMessage) {
		Gson gson = new Gson();
		String json = gson.toJson(chatMessage);

		mLogger.finer("Message to send: " + json);

		ChannelMessage channelMessage = new ChannelMessage(mUser.getUsername(), json);
		mChannelService.sendMessage(channelMessage);
	}


	/**
	 * All session variable enumerations
	 */
	protected enum SessionVariableNames {
		/** The logged in user */
		USER,
	}

	/** Current request */
	private HttpServletRequest mRequest;
	/** Current response */
	private HttpServletResponse mResponse;
	/** Current session */
	private HttpSession mSession = null;
	/** Current user */
	protected User mUser = null;
	/** Logger */
	protected Logger mLogger = null;

	/** Channel service for sending messages */
	private static ChannelService mChannelService = ChannelServiceFactory.getChannelService();
}
