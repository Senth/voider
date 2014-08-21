package com.spiddekauga.voider.repo;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.HighscoreEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.SyncHighscoreMethod;
import com.spiddekauga.voider.network.entities.method.SyncHighscoreMethodResponse;
import com.spiddekauga.voider.utils.Synchronizer;
import com.spiddekauga.voider.utils.User;

/**
 * Common highscore repository for both web and local.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class HighscoreRepo extends Repo {
	/**
	 * Private constructor to enforce singleton pattern
	 */
	private HighscoreRepo() {
		// Does nothing
	}

	/**
	 * @return instance of this class
	 */
	public static HighscoreRepo getInstance() {
		if (mInstance == null) {
			mInstance = new HighscoreRepo();
		}
		return mInstance;
	}

	/**
	 * Synchronize highscores. Should preferably only be called from Synchronizer
	 * @param responseListeners listens to the web responce (when syncing is done)
	 */
	public void sync(ICallerResponseListener... responseListeners) {
		ArrayList<HighscoreEntity> unsyncedHighscores = mLocalRepo.getUnsynced();
		mWebRepo.sync(mLocalRepo.getSyncDate(), unsyncedHighscores, addToFront(responseListeners, this));
	}

	/**
	 * Try to set a new highscore and if successful, sync it.
	 * @param levelId level to set the highscore for
	 * @param score new highscore to set
	 */
	public void setHighscoreAndSync(UUID levelId, int score) {
		if (isNewHighscore(levelId, score)) {
			mLocalRepo.setHighscore(levelId, score);

			if (User.getGlobalUser().isOnline()) {
				sync(Synchronizer.getInstance());
			}
		}
	}

	/**
	 * Get player highscore for the specified level
	 * @param levelId level to get player highscore from
	 * @return highscore for this level, null if no highscore was found
	 */
	public HighscoreEntity getPlayerHighscore(UUID levelId) {
		return mLocalRepo.getHighscore(levelId);
	}

	/**
	 * Checks if the specified highscore is a new one
	 * @param levelId level to check the highscore for
	 * @param score new score to check
	 * @return true if it's a new highscore. True if no highscore exists for this level.
	 *         False if score is lower than current highscore.
	 */
	public boolean isNewHighscore(UUID levelId, int score) {
		return mLocalRepo.isNewHighscore(levelId, score);
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof SyncHighscoreMethodResponse) {
			handleSyncResponse((SyncHighscoreMethod) method, (SyncHighscoreMethodResponse) response);
		}
	}

	/**
	 * Handle sync response from the server
	 * @param method parameters to the server
	 * @param response server response
	 */
	private void handleSyncResponse(SyncHighscoreMethod method, SyncHighscoreMethodResponse response) {
		// Set highscores as synced
		if (response.isSuccessful()) {
			mLocalRepo.setSynced(method.highscores);
		}
	}

	/** Local repository */
	private HighscoreLocalRepo mLocalRepo = HighscoreLocalRepo.getInstance();
	/** Web repository */
	private HighscoreWebRepo mWebRepo = HighscoreWebRepo.getInstance();

	/** Instance of this class */
	private static HighscoreRepo mInstance = null;
}
