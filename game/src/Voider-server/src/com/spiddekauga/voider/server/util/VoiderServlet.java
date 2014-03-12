package com.spiddekauga.voider.server.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * Wrapper for the Voider servlet
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class VoiderServlet extends HttpServlet {
	/**
	 * Called by the server to handle a post or get call.
	 * @param request the server request
	 * @param response the response to send to the client
	 * @throws IOException if an input or output error is detected when the servlet handles the GET/POST request
	 * @throws ServletException if the request for the GET/POST could not be handled
	 */
	protected abstract void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

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
	 * @throws IOException if an input or output error is detected when the servlet handles the GET/POST request
	 * @throws ServletException if the request for the GET/POST could not be handled
	 */
	private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		initSession(request);
		onRequest(request, response);
		saveSession();
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
	 * All session variable enumerations
	 */
	protected enum SessionVariableNames {
		/** The logged in user */
		USER,
	}

	/** Current session */
	private HttpSession mSession = null;
	/** Current user */
	protected User mUser = null;

	/** Serialized version id */
	private static final long serialVersionUID = 6754888059125843132L;
}
