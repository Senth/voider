package com.spiddekauga.voider.repo.stat;

import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.stat.HighscoreEntity;
import com.spiddekauga.voider.network.stat.HighscoreGetMethod;
import com.spiddekauga.voider.network.stat.HighscoreGetMethod.Fetch;
import com.spiddekauga.voider.network.stat.HighscoreGetResponse;
import com.spiddekauga.voider.network.stat.HighscoreGetResponse.Statuses;
import com.spiddekauga.voider.network.stat.HighscoreSyncEntity;
import com.spiddekauga.voider.network.stat.HighscoreSyncMethod;
import com.spiddekauga.voider.network.stat.HighscoreSyncResponse;
import com.spiddekauga.voider.repo.Cache;
import com.spiddekauga.voider.repo.CacheEntity;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebRepo;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Web repository for highscores
 */
class HighscoreWebRepo extends WebRepo {
/** Instance of this class */
private static HighscoreWebRepo mInstance = null;
/** First place cache */
private Cache<UUID, FirstPlaceCache> mFirstPlaceCache = new Cache<>();
/** Top X cache */
private Cache<UUID, TopCache> mTopCache = new Cache<>();
/** User cache */
private Cache<UUID, UserCache> mUserCache = new Cache<>();

/**
 * Private constructor to enforce singleton pattern
 */
private HighscoreWebRepo() {
	EventDispatcher.getInstance().connect(EventTypes.USER_LOGGING_OUT, new IEventListener() {
		@Override
		public void handleEvent(GameEvent event) {
			mUserCache.clear();
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
 * Synchronize highscores
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
	method.fetch = fetchOption;
	method.levelId = levelId;

	HighscoreGetResponse cacheResponse = getCached(levelId, fetchOption);

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
 * @return new response that can be used instead of the server response, null if no cached gameVersion
 * of the level exists
 */
private HighscoreGetResponse getCached(UUID levelId, Fetch fetchOption) {
	HighscoreGetResponse response = new HighscoreGetResponse();
	response.status = Statuses.SUCCESS;

	switch (fetchOption) {
	case FIRST_PLACE: {
		FirstPlaceCache firstPlaceCache = mFirstPlaceCache.get(levelId);

		// Use cache
		if (firstPlaceCache != null) {
			response.firstPlace = firstPlaceCache.get();
		}
		// Try to use top scores
		else {
			TopCache topCache = mTopCache.get(levelId);

			if (topCache != null && !topCache.get().isEmpty()) {
				response.firstPlace = topCache.get().get(0);
			} else {
				return null;
			}
		}
		break;
	}

	case TOP_SCORES: {
		TopCache topCache = mTopCache.get(levelId);

		// Use cache
		if (topCache != null) {
			response.topScores = topCache.get();
		} else {
			return null;
		}

		break;
	}

	case USER_SCORE: {
		UserCache userCache = mUserCache.get(levelId);
		FirstPlaceCache firstPlaceCache = mFirstPlaceCache.get(levelId);

		// Use cache
		if (userCache != null && firstPlaceCache != null) {
			response.firstPlace = firstPlaceCache.get();
			response.userPlace = userCache.getUserPlace();
			response.userScore = userCache.getUserScore();
			response.beforeUser = userCache.getBefore();
			response.afterUser = userCache.getAfter();
		} else {
			return null;
		}
		break;
	}
	}

	return response;
}


// CACHE

@Override
protected void handleResponse(IMethodEntity methodEntity, IEntity response, IResponseListener[] responseListeners) {
	IEntity responseToSend = null;

	if (methodEntity instanceof HighscoreGetMethod) {
		responseToSend = handleGetResponse((HighscoreGetMethod) methodEntity, response);
	} else if (methodEntity instanceof HighscoreSyncMethod) {
		responseToSend = handleSyncResponse(response);
	}


	sendResponseToListeners(methodEntity, responseToSend, responseListeners);
}

/**
 * Handle get highscore response
 * @param method parameters to the server
 * @param response the response from the server
 * @return a correct response for getting highscores
 */
private IEntity handleGetResponse(HighscoreGetMethod method, IEntity response) {
	// Cache result from valid response
	if (response instanceof HighscoreGetResponse) {
		if (((HighscoreGetResponse) response).isSuccessful()) {
			cacheHighscores(method.levelId, (HighscoreGetResponse) response);
		}

		return response;
	} else {
		HighscoreGetResponse methodResponse = new HighscoreGetResponse();
		methodResponse.status = HighscoreGetResponse.Statuses.FAILED_CONNECTION;
		return methodResponse;
	}
}

/**
 * Handle sync highscores response
 * @param response the response from the server
 * @return a correct response for syncing highscores
 */
private IEntity handleSyncResponse(IEntity response) {
	if (response instanceof HighscoreSyncResponse) {
		return response;
	} else {
		HighscoreSyncResponse methodResponse = new HighscoreSyncResponse();
		methodResponse.status = GeneralResponseStatuses.FAILED_SERVER_CONNECTION;
		return methodResponse;
	}
}

/**
 * Add server response to cache
 * @param levelId id of the level
 * @param response response from the server
 */
private void cacheHighscores(UUID levelId, HighscoreGetResponse response) {
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
 * Common class for highscore caches
 * @param <EntityType> derived class
 */
private abstract class HighscoreCache<EntityType> extends CacheEntity<EntityType> {
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
private class FirstPlaceCache extends HighscoreCache<FirstPlaceCache> {
	/** First place highscore */
	private HighscoreEntity mFirstPlace;

	/**
	 * Creates a new first place cache with a new highscore
	 * @param highscore highscore for the first place
	 */
	FirstPlaceCache(HighscoreEntity highscore) {
		mFirstPlace = highscore;
	}

	/**
	 * Default constructor
	 */
	FirstPlaceCache() {
	}

	/**
	 * @return highscore for the first place
	 */
	HighscoreEntity get() {
		return mFirstPlace;
	}

	@Override
	public FirstPlaceCache copy() {
		FirstPlaceCache copy = new FirstPlaceCache();
		copy(copy);
		return copy;
	}

	@Override
	public void copy(FirstPlaceCache copy) {
		super.copy(copy);
		copy.mFirstPlace = mFirstPlace;
	}
}

/**
 * Cache for top X highscores
 */
private class TopCache extends HighscoreCache<TopCache> {
	/** All top X highscores */
	private ArrayList<HighscoreEntity> mHighscores;

	/**
	 * Create top X highscores
	 * @param highscores all top highscores
	 */
	TopCache(ArrayList<HighscoreEntity> highscores) {
		mHighscores = highscores;
	}

	/**
	 * Default constructor
	 */
	TopCache() {
	}

	/**
	 * @return highscore for the top X places
	 */
	ArrayList<HighscoreEntity> get() {
		return mHighscores;
	}	@Override
	public TopCache copy() {
		TopCache copy = new TopCache();
		copy(copy);
		return copy;
	}

	@Override
	public void copy(TopCache copy) {
		super.copy(copy);
		copy.mHighscores = mHighscores;
	}

	;


}

/**
 * Cache for user highscore, including X above/below the user
 */
private class UserCache extends HighscoreCache<UserCache> {
	/** Place of the user */
	private int mPlace;
	/** All highscores before the user */
	private ArrayList<HighscoreEntity> mBefore;
	/** All highscores after the user */
	private ArrayList<HighscoreEntity> mAfter;
	/** User highscore */
	private HighscoreEntity mUser;

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
	 * Default constructor
	 */
	UserCache() {
	}

	/**
	 * @return get player place
	 */
	int getUserPlace() {
		return mPlace;
	}	@Override
	public UserCache copy() {
		UserCache copy = new UserCache();
		copy(copy);
		return copy;
	}

	/**
	 * @return scores before the user. I.e. with higher score
	 */
	ArrayList<HighscoreEntity> getBefore() {
		return mBefore;
	}	@Override
	public void copy(UserCache copy) {
		super.copy(copy);
		copy.mPlace = mPlace;
		copy.mBefore = mBefore;
		copy.mAfter = mAfter;
		copy.mUser = mUser;
	}

	;

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


}
}
