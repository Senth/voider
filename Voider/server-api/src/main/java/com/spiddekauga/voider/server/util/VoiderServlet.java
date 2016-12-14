package com.spiddekauga.voider.server.util;

import com.google.appengine.api.blobstore.BlobKey;
import com.spiddekauga.appengine.BlobUtils;
import com.spiddekauga.servlet.AppServlet;
import com.spiddekauga.voider.user.User;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;

/**
 * Common servlet for all Voider calls
 */
public abstract class VoiderServlet extends AppServlet {
protected User mUser = null;
private boolean mHandlesRequestDuringMaintenance = false;


/**
 * Call this to set if this servlet should call {@link #onPost()} during maintenance mode. This
 * method must be called in {@link AppServlet#onInit()}.
 * @param handlesRequestDuringMaintenance set to true if this servlet handles request during
 * maintenance. Default is false.
 */
protected void setHandlesRequestDuringMaintenance(boolean handlesRequestDuringMaintenance) {
	mHandlesRequestDuringMaintenance = handlesRequestDuringMaintenance;
}

@Override
protected void onPost() throws ServletException, IOException {
	handleRequest();
}

@Override
protected void onGet() throws ServletException, IOException {
	handleRequest();
}

@Override
protected void onInit() throws ServletException, IOException {
	super.onInit();
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
 * Gets a session variable
 * @param name the session's variable name
 * @return the variable stored in this place, or null if not found
 */
protected Object getSessionVariable(SessionVariableNames name) {
	return getSessionVariable(name.name());
}

private void handleRequest() throws ServletException, IOException {
	if (Maintenance.getMaintenanceMode() == Maintenance.Modes.UP || mHandlesRequestDuringMaintenance) {
		onRequest();
	}

	saveSession();
}

/**
 * Called by the server to handle a request
 * @throws IOException      if an input or output error is detected
 * @throws ServletException if the request could not be handled
 */
protected abstract void onRequest() throws ServletException, IOException;

/**
 * Saves the session variables
 */
private void saveSession() {
	if (mUser.isChanged()) {
		setSessionVariable(SessionVariableNames.USER, mUser);
	}
}

/**
 * Sets a session variable
 * @param name the session's variable name
 * @param variable the variable to set in the session
 */
protected void setSessionVariable(SessionVariableNames name, Object variable) {
	setSessionVariable(name.name(), variable);
}

/**
 * @param name name of the parameter
 * @return true if a parameter exists
 */
protected boolean isParameterSet(String name) {
	return getRequest().getParameter(name) != null;
}

/**
 * @param name name of the parameter to get
 * @return parameter value or null if it doesn't exist
 */
protected String getParameter(String name) {
	return getRequest().getParameter(name);
}

/**
 * @return get blob information from the current request, null if no uploads were made.
 */
protected Map<UUID, BlobKey> getUploadedBlobs() {
	return BlobUtils.getBlobKeysFromUpload(getRequest());
}

/**
 * @return get blob information from the current request where the uploaded resources contains
 * revisions, null if no uploads were made.
 */
protected Map<UUID, Map<Integer, BlobKey>> getUploadedRevisionBlobs() {
	return BlobUtils.getBlobKeysFromUploadRevision(getRequest());
}

/**
 * All session variable enumerations
 */
protected enum SessionVariableNames {
	/** The logged in user */
	USER,
}
}
