package com.spiddekauga.voider.repo;

import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.HighscoreEntity;
import com.spiddekauga.voider.network.entities.HighscoreSyncEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.HighscoreGetMethod;
import com.spiddekauga.voider.network.entities.method.HighscoreGetMethod.Fetch;
import com.spiddekauga.voider.network.entities.method.HighscoreGetMethodResponse;
import com.spiddekauga.voider.network.entities.method.HighscoreGetMethodResponse.Statuses;
import com.spiddekauga.voider.network.entities.method.HighscoreSyncMethod;
import com.spiddekauga.voider.network.entities.method.HighscoreSyncMethodResponse;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.User.UserEvents;

/**
 * Web repository for highscores
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class HighscoreWebRepo extends WebRepo {
	/**
	 * Private constructor to enforce singleton pattern
	 */
	private HighscoreWebRepo() {

		// Clear user cache when user logs out
		User.getGlobalUser().addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				// Clear cache when user logs out
				if (arg instanceof UserEvents) {
					if (((UserEvents) arg) == UserEvents.LOGOUT) {
						mUserCache.clear();
					}
				}
			}
		});
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
	void sync(Date lastSync, ArrayList<HighscoreSyncEntity> highscores, IResponseListener... responseListeners) {
		HighscoreSyncMethod method = new HighscoreSyncMethod();
		method.lastSync = lastSync;
		method.highscores = highscores;

		sendInNewThread(method, responseListeners);
	}

	/**
	 * Get highscore
	 * @param levelId id of the level
	 * @param fetchOption what to fetch from the server
	 * @param responseListeners listens to the web response
	 */
	void get(UUID levelId, Fetch fetchOption, IResponseListener... responseListeners) {
		HighscoreGetMethod method = new HighscoreGetMethod();
		method.fetch = Fetch.FIRST_PLACE;
		method.levelId = levelId;

		HighscoreGetMethodResponse cacheResponse = getCached(levelId, fetchOption);

		// Use cache
		if (cacheResponse != null) {
			sendResponseToListeners(method, cacheResponse, responseListeners);
		}
		// Fetch new from server
		else {
			sendInNewThread(method, responseListeners);
		}
	}

	/**
	 * Get cache response
	 * @param levelId id of the level
	 * @param fetchOption what to fetch
	 * @return new response that can be used instead of the server response, null if no
	 *         cached version of the level exists
	 */
	private HighscoreGetMethodResponse getCached(UUID levelId, Fetch fetchOption) {
		HighscoreGetMethodResponse response = null;

		switch (fetchOption) {
		case FIRST_PLACE: {
			FirstPlaceCache firstPlaceCache = mFirstPlaceCache.get(levelId);

			// Use cache
			if (firstPlaceCache != null) {
				response = new HighscoreGetMethodResponse();
				response.status = Statuses.SUCCESS;
				response.firstPlace = firstPlaceCache.get();
			}
			break;
		}

		case TOP_SCORES: {
			TopCache topCache = mTopCache.get(levelId);

			// Use cache
			if (topCache != null) {
				response = new HighscoreGetMethodResponse();
				response.topScores = topCache.get();
			}

			break;
		}

		case USER_SCORE: {
			UserCache userCache = mUserCache.get(levelId);
			FirstPlaceCache firstPlaceCache = mFirstPlaceCache.get(levelId);

			// Use cache
			if (userCache != null && firstPlaceCache != null) {
				response = new HighscoreGetMethodResponse();
				response.firstPlace = firstPlaceCache.get();
				response.userPlace = userCache.getUserPlace();
				response.userScore = userCache.getUserScore();
				response.beforeUser = userCache.getBefore();
				response.afterUser = userCache.getAfter();
			}
			break;
		}
		}

		return response;
	}

	@Override
	protected void handleResponse(IMethodEntity methodEntity, IEntity response, IResponseListener[] callerResponseListeners) {
		IEntity responseToSend = null;

		if (methodEntity instanceof HighscoreGetMethod) {
			responseToSend = handleGetResponse((HighscoreGetMethod) methodEntity, response);
		} else if (methodEntity instanceof HighscoreSyncMethod) {
			responseToSend = handleSyncResponse(response);
		}


		sendResponseToListeners(methodEntity, responseToSend, callerResponseListeners);
	}

	/**
	 * Handle get highscore response
	 * @param method parameters to the server
	 * @param response the response from the server
	 * @return a correct response for getting highscores
	 */
	private IEntity handleGetResponse(HighscoreGetMethod method, IEntity response) {
		// Cache result from valid response
		if (response instanceof HighscoreGetMethodResponse) {
			if (((HighscoreGetMethodResponse) response).isSuccessful()) {
				cacheHighscores(method.levelId, (HighscoreGetMethodResponse) response);
			}

			return response;
		} else {
			HighscoreGetMethodResponse methodResponse = new HighscoreGetMethodResponse();
			methodResponse.status = HighscoreGetMethodResponse.Statuses.FAILED_CONNECTION;
			return methodResponse;
		}
	}

	/**
	 * Add server response to cache
	 * @param levelId id of the level
	 * @param response response from the server
	 */
	private void cacheHighscores(UUID levelId, HighscoreGetMethodResponse response) {
		// First place
		if (response.firstPlace != null) {
			mFirstPlaceCache.add(levelId, new FirstPlaceCache(response.firstPlace));
		}

		// Top scores
		if (response.topScores != null) {
			mTopCache.add(levelId, new TopCache(response.topScores));
		}

		// User scores
		if (response.afterUser != null && response.beforeUser != null && response.userScore != null && response.userPlace > 0) {
			UserCache userCache = new UserCache(response.beforeUser, response.userScore, response.afterUser, response.userPlace);
			mUserCache.add(levelId, userCache);
		}
	}

	/**
	 * Handle sync highscores response
	 * @param response the response from the server
	 * @return a correct response for syncing highscores
	 */
	private IEntity handleSyncResponse(IEntity response) {
		if (response instanceof HighscoreSyncMethodResponse) {
			return response;
		} else {
			HighscoreSyncMethodResponse methodResponse = new HighscoreSyncMethodResponse();
			methodResponse.status = HighscoreSyncMethodResponse.Statuses.FAILED_CONNECTION;
			return methodResponse;
		}
	}


	// CACHE
	/** First place cache */
	private Cache<UUID, FirstPlaceCache> mFirstPlaceCache = new Cache<>();
	/** Top X cache */
	private Cache<UUID, TopCache> mTopCache = new Cache<>();
	/** User cache */
	private Cache<UUID, UserCache> mUserCache = new Cache<>();


	/** Instance of this class */
	private static HighscoreWebRepo mInstance = null;


	/**
	 * Common class for highscore caches
	 */
	private abstract class HighscoreCache extends CacheEntity {
		/**
		 * Set common outdated time for all highscore caches
		 */
		HighscoreCache() {
			super(Config.Cache.HIGHSCORE_TIME);
		}
	}

	/**
	 * Cache for first place
	 */
	private class FirstPlaceCache extends HighscoreCache {
		/**
		 * Creates a new first place cache with a new highscore
		 * @param highscore highscore for the first place
		 */
		FirstPlaceCache(HighscoreEntity highscore) {
			mFirstPlace = highscore;
		}

		/**
		 * @return highscore for the first place
		 */
		HighscoreEntity get() {
			return mFirstPlace;
		}

		/** First place highscore */
		private HighscoreEntity mFirstPlace;
	}

	/**
	 * Cache for top X highscores
	 */
	private class TopCache extends HighscoreCache {
		/**
		 * Create top X highscores
		 * @param highscores all top highscores
		 */
		TopCache(ArrayList<HighscoreEntity> highscores) {
			mHighscores = highscores;
		}

		/**
		 * @return highscore for the top X places
		 */
		ArrayList<HighscoreEntity> get() {
			return mHighscores;
		}

		/** All top X highscores */
		private ArrayList<HighscoreEntity> mHighscores;
	}

	/**
	 * Cache for user highscore, including X above/below the user
	 */
	private class UserCache extends HighscoreCache {
		/**
		 * Create user highscores
		 * @param beforeUser highscores with higher score
		 * @param user user highscores
		 * @param afterUser highscores with lower score
		 * @param place user place
		 */
		UserCache(ArrayList<HighscoreEntity> beforeUser, HighscoreEntity user, ArrayList<HighscoreEntity> afterUser, int place) {
			mBefore = beforeUser;
			mAfter = afterUser;
			mUser = user;
			mPlace = place;
		}

		/**
		 * @return get player place
		 */
		int getUserPlace() {
			return mPlace;
		}

		/**
		 * @return scores before the user. I.e. with higher score
		 */
		ArrayList<HighscoreEntity> getBefore() {
			return mBefore;
		}

		/**
		 * @return scores after the user. I.e. with lower score
		 */
		ArrayList<HighscoreEntity> getAfter() {
			return mAfter;
		}

		/**
		 * @return get player score
		 */
		HighscoreEntity getUserScore() {
			return mUser;
		}

		/** Place of the user */
		private int mPlace;
		/** All highscores before the user */
		private ArrayList<HighscoreEntity> mBefore;
		/** All highscores after the user */
		private ArrayList<HighscoreEntity> mAfter;
		/** User highscore */
		private HighscoreEntity mUser;
	}
}
