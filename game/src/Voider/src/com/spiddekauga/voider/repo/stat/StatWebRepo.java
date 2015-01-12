package com.spiddekauga.voider.repo.stat;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.stat.StatSyncEntity;
import com.spiddekauga.voider.network.entities.stat.StatSyncMethod;
import com.spiddekauga.voider.network.entities.stat.StatSyncMethodResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebRepo;

/**
 * Web repository for statistics
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class StatWebRepo extends WebRepo {

	/**
	 * Private constructor to enforce singleton pattern
	 */
	private StatWebRepo() {
		// Does nothing
	}

	/**
	 * @return instance of this class
	 */
	public static StatWebRepo getInstance() {
		if (mInstance == null) {
			mInstance = new StatWebRepo();
		}
		return mInstance;
	}

	/**
	 * Synchronize statistics
	 * @param stats all statistics that should be synced
	 * @param responseListeners listener of the web response
	 */
	void sync(StatSyncEntity stats, IResponseListener... responseListeners) {
		StatSyncMethod method = new StatSyncMethod();
		method.syncEntity = stats;
		sendInNewThread(method, responseListeners);
	}

	@Override
	protected void handleResponse(IMethodEntity methodEntity, IEntity response, IResponseListener[] responseListeners) {
		IEntity responseToSend = null;

		if (methodEntity instanceof StatSyncMethod) {
			responseToSend = handleSyncResponse(response);
		}

		sendResponseToListeners(methodEntity, responseToSend, responseListeners);
	}

	/**
	 * Handle sync response
	 * @param response the response from the server
	 * @return a correct response for syncing
	 */
	private IEntity handleSyncResponse(IEntity response) {
		if (response instanceof StatSyncMethodResponse) {
			return response;
		} else {
			StatSyncMethodResponse methodResponse = new StatSyncMethodResponse();
			methodResponse.status = GeneralResponseStatuses.FAILED_SERVER_CONNECTION;
			return methodResponse;
		}
	}

	/** Instance of this class */
	private static StatWebRepo mInstance = null;
}
