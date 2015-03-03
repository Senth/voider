package com.spiddekauga.voider.repo.analytics;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.voider.network.analytics.AnalyticsMethod;
import com.spiddekauga.voider.network.analytics.AnalyticsResponse;
import com.spiddekauga.voider.network.analytics.AnalyticsSessionEntity;
import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebRepo;

/**
 * Web repository for analytics
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class AnalyticsWebRepo extends WebRepo {
	/**
	 * Private constructor to enforce singleton pattern
	 */
	private AnalyticsWebRepo() {
		// Does nothing
	}

	/**
	 * @return instance of this class
	 */
	static AnalyticsWebRepo getInstance() {
		if (mInstance == null) {
			mInstance = new AnalyticsWebRepo();
		}
		return mInstance;
	}


	@Override
	protected void handleResponse(IMethodEntity methodEntity, IEntity response, IResponseListener[] responseListeners) {
		IEntity responseToSend = null;

		if (methodEntity instanceof AnalyticsMethod) {
			responseToSend = handleSyncResponse(response);
		}

		sendResponseToListeners(methodEntity, responseToSend, responseListeners);
	}

	/**
	 * Handle sync analytics response
	 * @param response the response from the server
	 * @return correct response
	 */
	private IEntity handleSyncResponse(IEntity response) {
		if (response instanceof AnalyticsResponse) {
			return response;
		} else {
			AnalyticsResponse methodResponse = new AnalyticsResponse();
			methodResponse.status = GeneralResponseStatuses.FAILED_SERVER_CONNECTION;
			return methodResponse;
		}
	}

	/**
	 * Send local analytics to the server
	 * @param sessions analytics sessions to send to the server
	 * @param platform platform of this device
	 * @param os which OS this device uses
	 * @param userAnalyticsId unique id for this user on this device
	 * @param responseListeners listener of the web response
	 */
	void sync(ArrayList<AnalyticsSessionEntity> sessions, String platform, String os, UUID userAnalyticsId, IResponseListener... responseListeners) {
		AnalyticsMethod method = new AnalyticsMethod();
		method.platform = platform;
		method.os = os;
		method.userAnalyticsId = userAnalyticsId;
		method.sessions = sessions;
		sendInNewThread(method, responseListeners);
	}

	private static AnalyticsWebRepo mInstance = null;
}
