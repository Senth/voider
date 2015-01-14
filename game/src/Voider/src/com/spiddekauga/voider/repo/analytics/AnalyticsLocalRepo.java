package com.spiddekauga.voider.repo.analytics;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.network.analytics.AnalyticsSessionEntity;
import com.spiddekauga.voider.repo.user.UserLocalRepo;

/**
 * Local repository for analytics
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class AnalyticsLocalRepo {
	/**
	 * Private constructor to enforce singleton pattern
	 */
	private AnalyticsLocalRepo() {
		// Does nothing
	}

	/**
	 * @return instance of this class
	 */
	public static AnalyticsLocalRepo getInstance() {
		if (mInstance == null) {
			mInstance = new AnalyticsLocalRepo();
		}
		return mInstance;
	}

	/**
	 * Creates a new session
	 */
	void newSession() {
		if (mSessionId != null) {
			Gdx.app.error("AnalyticsLocalRepo", "Session wasn't null when a new session was created!");
		}
		mSessionId = mSqliteGateway.addSession(new Date());
	}

	/**
	 * Ends a session
	 */
	void endSession() {
		if (mSceneId != null) {
			endScene();
		}

		if (mSessionId != null) {
			mSqliteGateway.endSession(mSessionId, new Date());
		}
		mSessionId = null;
	}

	/**
	 * Start a new scene analytics
	 * @param name scene name
	 */
	void startScene(String name) {
		if (mSceneId != null) {
			Gdx.app.error("AnalyticsLocalRepo", "Scene wasn't null when a new scene was created");
		}

		endLoadTimer();
		mSceneId = mSqliteGateway.addScene(mSessionId, new Date(), mSceneLoadTime, name);
	}

	/**
	 * End the current scene
	 */
	void endScene() {
		if (mSceneId != null) {
			mSqliteGateway.endScene(mSceneId, new Date());
		} else {
			Gdx.app.error("AnalyticsLocalRepo", "Scene id was null when trying to end it");
		}
		mSceneId = null;
		startLoadTimer();
	}

	/**
	 * Start loading timer
	 */
	private void startLoadTimer() {
		mSceneLoadStart = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * End loading timer
	 */
	private void endLoadTimer() {
		mSceneLoadTime = GameTime.getTotalGlobalTimeElapsed() - mSceneLoadStart;
	}

	/**
	 * Add an event to the current scene
	 * @param name event name
	 * @param data extra information about the event
	 */
	void addEvent(String name, String data) {
		if (mSceneId != null) {
			mSqliteGateway.addEvent(mSceneId, new Date(), name, data);
		} else {
			Gdx.app.error("AnalyticsLocalRepo", "Scene id was null during an event");
		}
	}

	/**
	 * Clear analytics
	 */
	void clear() {
		mSqliteGateway.clear();
	}

	/**
	 * @return true if any local analytics exists
	 */
	boolean isAnalyticsExists() {
		return mSqliteGateway.isAnalyticsExists();
	}

	/**
	 * @return get all analytics
	 */
	ArrayList<AnalyticsSessionEntity> getAnalytics() {
		ArrayList<AnalyticsSessionEntity> sessions = mSqliteGateway.getAnalytics();

		// Set additional information for each session
		String platform = getPlatform();
		UUID userAnalyticsId = getUserAnalyticsId();
		String os = getOs();
		for (AnalyticsSessionEntity session : sessions) {
			session.os = os;
			session.platform = platform;
			session.userAnalyticsId = userAnalyticsId;
			session.screenWidth = Gdx.graphics.getWidth();
			session.screenHeight = Gdx.graphics.getHeight();
		}

		return sessions;
	}

	/**
	 * @return Unique analytics user id
	 */
	private UUID getUserAnalyticsId() {
		return UserLocalRepo.getInstance().getAnalyticsId();
	}

	/**
	 * @return platform this client is on
	 */
	private String getPlatform() {
		return Gdx.app.getType().toString();
	}

	/**
	 * @return OS this client uses
	 */
	private String getOs() {
		// Android
		if (Gdx.app.getType() == ApplicationType.Android) {
			return "Android " + Gdx.app.getVersion();
		}
		// iOS
		else if (Gdx.app.getType() == ApplicationType.iOS) {
			return "iOS " + Gdx.app.getVersion();
		}
		// Desktop and anything else
		else {
			return System.getProperty("os.name");
		}
	}

	/** When a scene started loading */
	private float mSceneLoadStart = 0;
	/** Time when the last scene was ended */
	private float mSceneLoadTime = 0;
	/** Current session id */
	private UUID mSessionId = null;
	/** Current scene id */
	private UUID mSceneId = null;
	private AnalyticsSqliteGateway mSqliteGateway = new AnalyticsSqliteGateway();
	private static AnalyticsLocalRepo mInstance = null;
}
