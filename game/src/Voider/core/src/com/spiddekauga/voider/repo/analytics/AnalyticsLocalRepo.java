package com.spiddekauga.voider.repo.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.network.analytics.AnalyticsEventEntity;
import com.spiddekauga.voider.network.analytics.AnalyticsEventTypes;
import com.spiddekauga.voider.network.analytics.AnalyticsSceneEntity;
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
		startLoadTimer();
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

		// Remove "Scene" from the scene name
		String fixedName = name;
		int sceneIndex = name.indexOf("Scene");
		if (sceneIndex != -1) {
			fixedName = name.substring(0, sceneIndex);
		}

		mSceneId = mSqliteGateway.addScene(mSessionId, new Date(), mSceneLoadTime, fixedName);
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
	 * @param type event type
	 * @param data extra information about the event
	 */
	void addEvent(String name, AnalyticsEventTypes type, String data) {
		if (mSceneId != null) {
			mSqliteGateway.addEvent(mSceneId, new Date(), name, type, data);
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

	/**
	 * @return formatted debug string for the current session
	 */
	String getSessionDebug() {
		ArrayList<AnalyticsSessionEntity> sessions = mSqliteGateway.getAnalytics();

		// Get current session
		AnalyticsSessionEntity session = null;
		for (AnalyticsSessionEntity sessionEntity : sessions) {
			if (sessionEntity.sessionId.equals(mSessionId)) {
				session = sessionEntity;
				break;
			}
		}

		if (session != null) {
			StringBuilder stringBuilder = new StringBuilder();

			sortScenes(session);
			if (!session.scenes.isEmpty()) {
				appendLastActions(session.scenes.get(session.scenes.size() - 1), stringBuilder);
				stringBuilder.append("</br></br>\n\n");
			}
			stringBuilder.append("<h3>Scenes</h3>");
			for (AnalyticsSceneEntity scene : session.scenes) {
				appendScene(scene, stringBuilder);
			}

			return stringBuilder.toString();
		} else {
			return "";
		}
	}

	/**
	 * Sort all the scenes by time
	 * @param session sort all scenes by asc time
	 */
	private static void sortScenes(AnalyticsSessionEntity session) {
		Collections.sort(session.scenes, mSceneComparator);
	}

	/**
	 * Appends the 10 (or less) latest actions in the current scene
	 * @param scene latest scene
	 * @param stringBuilder where to append latest action
	 */
	private static void appendLastActions(AnalyticsSceneEntity scene, StringBuilder stringBuilder) {
		int startIndex = scene.events.size() - 1;
		int endIndex = startIndex - 9;

		if (endIndex < 0) {
			endIndex = 0;
		}

		stringBuilder.append("<h3>Last 10 actions in <b>" + scene.name + "</b> scene</h3>");
		tableHeader(stringBuilder);

		for (int i = startIndex; i >= endIndex; --i) {
			appendEvent(scene, scene.events.get(i), stringBuilder);
		}
	}


	/**
	 * Append scene with events information
	 * @param scene the scene to append
	 * @param stringBuilder where to append the scene with events
	 */
	private static void appendScene(AnalyticsSceneEntity scene, StringBuilder stringBuilder) {
		stringBuilder.append("<h4>" + scene.name + " events</h4>\n");
		stringBuilder.append("<table>\n");
		tableHeader(stringBuilder);

		for (AnalyticsEventEntity event : scene.events) {
			appendEvent(scene, event, stringBuilder);
		}

		stringBuilder.append("</table>\n");
	}

	/**
	 * Append event information
	 * @param scene scene information
	 * @param event the event to append
	 * @param stringBuilder where to append the event
	 */
	private static void appendEvent(AnalyticsSceneEntity scene, AnalyticsEventEntity event, StringBuilder stringBuilder) {
		tableRowStart(stringBuilder);

		// Scene name
		tableColumn(scene.name, stringBuilder);

		// Event name
		tableColumn(event.name, stringBuilder);

		// Event time (as seconds since scene started)
		long diffTimeMili = event.time.getTime() - scene.startTime.getTime();
		double diffTime = diffTimeMili * 0.001;
		tableColumn(String.format("%.2f", diffTime), stringBuilder);

		// Event data
		tableColumn(event.data, stringBuilder);

		tableRowEnd(stringBuilder);
	}

	/**
	 * @return current screen size
	 */
	private String getScreenSize() {
		return Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight();
	}

	/**
	 * Sorting scenes by starting time
	 */
	private static Comparator<AnalyticsSceneEntity> mSceneComparator = new Comparator<AnalyticsSceneEntity>() {
		@Override
		public int compare(AnalyticsSceneEntity o1, AnalyticsSceneEntity o2) {
			// Null checking
			if (o1 == null) {
				if (o2 == null) {
					return 0;
				} else {
					return -1;
				}
			} else if (o2 == null) {
				return 1;
			}

			// Regular checking
			return o1.startTime.compareTo(o2.startTime);
		}

	};

	/**
	 * Appends a new table header
	 * @param header
	 * @param stringBuilder
	 */
	private static void tableHeader(String header, StringBuilder stringBuilder) {
		stringBuilder.append("<th style=\"border: 1px solid black;\">");
		stringBuilder.append(header);
		stringBuilder.append("</th>\n");
	}

	/**
	 * Append all table headers to the table
	 * @param stringBuilder
	 */
	private static void tableHeader(StringBuilder stringBuilder) {
		tableRowStart(stringBuilder);
		tableHeader("Scene", stringBuilder);
		tableHeader("Event Name", stringBuilder);
		tableHeader("Time", stringBuilder);
		tableHeader("Data", stringBuilder);
		tableRowEnd(stringBuilder);
	}

	/**
	 * Appends a new table column
	 * @param value text in the column
	 * @param stringBuilder
	 */
	private static void tableColumn(String value, StringBuilder stringBuilder) {
		stringBuilder.append("<td style=\"border: 1px solid black;\">");
		stringBuilder.append(value);
		stringBuilder.append("</td>\n");
	}

	/**
	 * Start table row
	 * @param stringBuilder
	 */
	private static void tableRowStart(StringBuilder stringBuilder) {
		stringBuilder.append("<tr>\n");
	}

	/**
	 * End table row
	 * @param stringBuilder
	 */
	private static void tableRowEnd(StringBuilder stringBuilder) {
		stringBuilder.append("</tr>\n");
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
