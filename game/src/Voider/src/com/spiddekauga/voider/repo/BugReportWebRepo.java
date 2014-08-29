package com.spiddekauga.voider.repo;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.BugReportEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.BugReportMethod;
import com.spiddekauga.voider.network.entities.method.BugReportMethodResponse;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;

/**
 * Send bug reports to the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class BugReportWebRepo extends WebRepo {
	@Override
	protected void handleResponse(IMethodEntity methodEntity, IEntity response, IResponseListener[] callerResponseListeners) {
		IEntity responseToSend = null;

		if (methodEntity instanceof BugReportMethod) {
			if (response instanceof BugReportMethodResponse) {
				responseToSend = response;
			} else {
				BugReportMethodResponse bugReportMethodResponse = new BugReportMethodResponse();
				bugReportMethodResponse.status = BugReportMethodResponse.Statuses.FAILED_CONNECTION;
				responseToSend = bugReportMethodResponse;
			}
		}

		if (responseToSend != null) {
			sendResponseToListeners(methodEntity, responseToSend, callerResponseListeners);
		}
	}

	/**
	 * Send a bug report to the server
	 * @param bugReport the bug report to send to the server
	 * @param responseListeners listens to the web response
	 */
	public void sendBugReport(BugReportEntity bugReport, IResponseListener... responseListeners) {
		BugReportMethod bugReportMethod = new BugReportMethod();
		bugReportMethod.bugs.add(bugReport);

		sendInNewThread(bugReportMethod, responseListeners);
	}

	/**
	 * Send several bug reports to the server
	 * @param bugReports all bug report to send to the server
	 * @param responseListeners listens to the web response
	 */
	public void sendBugReport(ArrayList<BugReportEntity> bugReports, IResponseListener... responseListeners) {
		BugReportMethod bugReportMethod = new BugReportMethod();
		bugReportMethod.bugs = bugReports;

		sendInNewThread(bugReportMethod, responseListeners);
	}

	/**
	 * Private constructor to enforce singleton pattern
	 */
	private BugReportWebRepo() {
		// Does nothing
	}

	/**
	 * @return instance of this class
	 */
	public static BugReportWebRepo getInstance() {
		if (mInstance == null) {
			mInstance = new BugReportWebRepo();
		}
		return mInstance;
	}


	/** Instance of this class */
	private static BugReportWebRepo mInstance = null;
}
