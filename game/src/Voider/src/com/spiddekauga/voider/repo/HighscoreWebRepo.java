package com.spiddekauga.voider.repo;

import java.util.ArrayList;
import java.util.Date;

import com.spiddekauga.voider.network.entities.HighscoreEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.SyncHighscoreMethod;
import com.spiddekauga.voider.network.entities.method.SyncHighscoreMethodResponse;

/**
 * Web repository for highscores
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class HighscoreWebRepo extends WebRepo {
	/**
	 * Private constructor to enforce singleton pattern
	 */
	private HighscoreWebRepo() {
		// Does nothing
	}

	/**
	 * @return instance of this class
	 */
	public static HighscoreWebRepo getInstance() {
		if (mInstance == null) {
			mInstance = new HighscoreWebRepo();
		}
		return mInstance;
	}

	/**
	 * Syncronize highscores
	 * @param lastSync date when highscores were last synced
	 * @param highscores all highscores to synchronize
	 * @param responseListeners listens to the web response
	 */
	void sync(Date lastSync, ArrayList<HighscoreEntity> highscores, ICallerResponseListener... responseListeners) {
		SyncHighscoreMethod method = new SyncHighscoreMethod();
		method.lastSync = lastSync;
		method.highscores = highscores;

		sendInNewThread(method, responseListeners);
	}

	@Override
	protected void handleResponse(IMethodEntity methodEntity, IEntity response, ICallerResponseListener[] callerResponseListeners) {
		IEntity responseToSend = null;

		if (methodEntity instanceof SyncHighscoreMethod) {
			responseToSend = handleSyncResponse(response);
		}


		sendResponseToListeners(methodEntity, responseToSend, callerResponseListeners);
	}

	/**
	 * Handle response from sync highscores
	 * @param response the response from the server
	 * @return a correct response for syncing highscores
	 */
	private IEntity handleSyncResponse(IEntity response) {
		if (response instanceof SyncHighscoreMethodResponse) {
			return response;
		} else {
			SyncHighscoreMethodResponse methodResponse = new SyncHighscoreMethodResponse();
			methodResponse.status = SyncHighscoreMethodResponse.Statuses.FAILED_CONNECTION;
			return methodResponse;
		}
	}

	/** Instance of this class */
	private static HighscoreWebRepo mInstance = null;
}
