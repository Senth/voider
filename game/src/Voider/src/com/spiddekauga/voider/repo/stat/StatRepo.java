package com.spiddekauga.voider.repo.stat;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.stat.StatSyncEntity;
import com.spiddekauga.voider.network.stat.StatSyncResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.Repo;

/**
 * Statistics repository
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class StatRepo extends Repo {
	/**
	 * Private constructor to enforce singleton pattern
	 */
	private StatRepo() {
		// Does nothing
	}

	/**
	 * @return instance of this class
	 */
	public static StatRepo getInstance() {
		if (mInstance == null) {
			mInstance = new StatRepo();
		}
		return mInstance;
	}

	/**
	 * Synchronize statistics
	 * @param responseListeners listeners of the web response
	 */
	public void sync(IResponseListener... responseListeners) {
		// Get stats to sync to server
		StatSyncEntity stats = mLocalRepo.getUnsynced();
		mWebRepo.sync(stats, addToFront(responseListeners, this));
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof StatSyncResponse) {
			handleSyncResponse((StatSyncResponse) response);
		}
	}

	/**
	 * Handle sync response from the server
	 * @param response server response
	 */
	private void handleSyncResponse(StatSyncResponse response) {
		if (response.isSuccessful()) {
			mLocalRepo.setAsSynced(response.syncEntity.syncDate);
		}
	}

	/** Local repository */
	private StatLocalRepo mLocalRepo = StatLocalRepo.getInstance();
	/** Web repository */
	private StatWebRepo mWebRepo = StatWebRepo.getInstance();
	/** Instance of this class */
	private static StatRepo mInstance = null;
}
