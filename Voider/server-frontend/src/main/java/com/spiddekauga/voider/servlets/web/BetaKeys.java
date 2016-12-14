package com.spiddekauga.voider.servlets.web;

import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.server.util.DatastoreTables;
import com.spiddekauga.voider.server.util.DatastoreTables.CBetaGroup;
import com.spiddekauga.voider.server.util.DatastoreTables.CBetaKey;
import com.spiddekauga.voider.server.util.VoiderController;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;

/**
 * Display all available beta keys for a group
 */
@SuppressWarnings("serial")
public class BetaKeys extends VoiderController {

private ArrayList<String> mBetaKeys = null;
private String mGroup = null;

@Override
protected void onInit() throws ServletException, IOException {
	mBetaKeys = new ArrayList<>();
	mGroup = "No group with that hash";
}

@Override
protected void onGet() throws ServletException, IOException {
	if (isAllParametersSet()) {
		displayBetaKeys();
	}

	getRequest().setAttribute("group", mGroup);
	getRequest().setAttribute("keys", mBetaKeys);
	try {
		getRequest().getRequestDispatcher("beta-keys.jsp").forward(getRequest(), getResponse());
	} catch (ServletException | IOException e) {
		e.printStackTrace();
	}
}

/**
 * @return true if we have all required parameters
 */
private boolean isAllParametersSet() {
	return isParameterSet("group") && isParameterSet("hash");
}

/**
 * Display beta keys
 */
private void displayBetaKeys() {
	String name = getParameter("group");
	String hash = getParameter("hash");


	// Get the group and check so the has is correct
	Entity groupEntity = DatastoreUtils.getSingleEntity(DatastoreTables.BETA_GROUP, new FilterWrapper(CBetaGroup.NAME, name));
	if (groupEntity == null) {
		return;
	}

	// Hash correct?
	if (!hash.equals(groupEntity.getProperty(CBetaGroup.HASH))) {
		return;
	}

	mGroup = name;

	// Get all keys from this group
	Iterable<Entity> betaEntities = DatastoreUtils.getEntities(DatastoreTables.BETA_KEY, groupEntity.getKey());

	for (Entity entity : betaEntities) {
		Boolean used = (Boolean) entity.getProperty(CBetaKey.USED);
		if (used != null && !used) {
			String betaKey = (String) entity.getProperty(CBetaKey.KEY);
			if (betaKey != null) {
				mBetaKeys.add(betaKey);
			}
		}
	}
}
}