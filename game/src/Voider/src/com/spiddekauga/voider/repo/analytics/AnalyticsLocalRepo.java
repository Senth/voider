package com.spiddekauga.voider.repo.analytics;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.network.analytics.AnalyticsSessionEntity;

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
		mSessionId = mSqliteGateway.addSession(new Date(), getScreenSize());
	}

	/**
	 * Ends a session
	 */
	void endSession() {
		if (mSceneId != null) {
			endScene();
		}

		if (mSessionId != null) {
			mSqliteGateway.endSession(mSessionId, new Date(), getScreenSize());
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
	 * Remove these specified sessions, scenes and events)
	 * @param sessions the sessions to remove
	 */
	void removeAnalytics(ArrayList<AnalyticsSessionEntity> sessions) {
		mSqliteGateway.removeAnalytics(sessions);
	}

	/**
	 * @return true if any local analytics exists
	 */
	boolean isAnalyticsExists() {
		return mSqliteGateway.isAnalyticsExists(mSessionId);
	}

	/**
	 * @return get all analytics
	 */
	ArrayList<AnalyticsSessionEntity> getAnalytics() {
		ArrayList<AnalyticsSessionEntity> sessions = mSqliteGateway.getAnalytics();

		// Remove current session
		Iterator<AnalyticsSessionEntity> sessionIt = sessions.iterator();
		boolean removed = false;
		while (sessionIt.hasNext() && !removed) {
			AnalyticsSessionEntity session = sessionIt.next();
			if (session.sessionId.equals(mSessionId)) {
				sessionIt.remove();
				removed = true;
			}
		}

		return sessions;
	}

	private String getScreenSize() {
		return Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight();
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
