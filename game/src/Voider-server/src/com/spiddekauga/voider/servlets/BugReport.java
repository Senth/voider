package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.entities.BugReportEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.BugReportMethod;
import com.spiddekauga.voider.network.entities.method.BugReportMethodResponse;
import com.spiddekauga.voider.network.entities.method.BugReportMethodResponse.Statuses;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Takes bug reports and reports these
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BugReport extends VoiderServlet {
	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BugReportMethodResponse methodResponse = new BugReportMethodResponse();
		methodResponse.status = Statuses.FAILED_SERVER_ERROR;


		// Get method entity
		byte[] entityData = NetworkGateway.getEntity(request);
		IEntity networkEntity = NetworkEntitySerializer.deserializeEntity(entityData);

		if (networkEntity instanceof BugReportMethod) {
			methodResponse.status = Statuses.SUCCESS;

			for (BugReportEntity bugReportEntity : ((BugReportMethod) networkEntity).bugs) {
				boolean success = sendBugReport(bugReportEntity);

				if (!success) {
					methodResponse.status = Statuses.SUCCESS_WITH_ERRORS;
					methodResponse.failedBugReports.add(bugReportEntity);
				}
			}
		}

		byte[] byteResponse = NetworkEntitySerializer.serializeEntity(methodResponse);
		NetworkGateway.sendResponse(response, byteResponse);
	}

	/**
	 * Sends a bug report
	 * @param bugReportEntity entity to the bug report
	 * @return true if the report was sent successfully
	 */
	private boolean sendBugReport(BugReportEntity bugReportEntity) {
		Entity user = getUser(bugReportEntity.userKey);

		if (user == null) {
			return false;
		}





		return true;
	}

	/**
	 * Get a user from the database or cache
	 * @param userKeyString the user key in string format
	 * @return user database entity, null if not found
	 */
	private Entity getUser(String userKeyString) {
		Entity user = mUsersCached.get(userKeyString);

		if (user == null) {
			Key userKey = KeyFactory.stringToKey(userKeyString);

			if (userKey != null) {
				user = DatastoreUtils.getItemByKey(userKey);

				if (user != null) {
					mUsersCached.put(userKeyString, user);
				}
			}
		}

		return user;
	}


	/** Cached users */
	HashMap<String, Entity> mUsersCached = new HashMap<>();
}
