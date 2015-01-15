package com.spiddekauga.voider.repo.analytics;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.spiddekauga.voider.network.analytics.AnalyticsEventEntity;
import com.spiddekauga.voider.network.analytics.AnalyticsSceneEntity;
import com.spiddekauga.voider.network.analytics.AnalyticsSessionEntity;
import com.spiddekauga.voider.repo.SqliteGateway;

/**
 * SQLite gateway for analytics.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class AnalyticsSqliteGateway extends SqliteGateway {
	/**
	 * Add a new session to the database
	 * @param startTime time the session was started
	 * @param screenSize size of the screen
	 * @return id of the session
	 */
	UUID addSession(Date startTime, String screenSize) {
		UUID sessionId = UUID.randomUUID();

		// @formatter:off
		execSQL("INSERT INTO analytics_session (session_id, start_time, screen_size) VALUES ('" +
				sessionId + "', " +
				startTime.getTime() + ", '" +
				screenSize + "');");
		// @formatter:on

		return sessionId;
	}

	/**
	 * End a created session
	 * @param sessionId the session to end
	 * @param endTime time the session was ended
	 * @param screenSize size of the screen
	 */
	void endSession(UUID sessionId, Date endTime, String screenSize) {
		execSQL("UPDATE analytics_session SET end_time=" + endTime + ", screen_size='" + screenSize + "' WHERE session_id='" + sessionId + "';");
	}


	/**
	 * Add a new scene to the specified session
	 * @param sessionId id of the session to add this scene to
	 * @param startTime time the scene was created
	 * @param loadTime how long it took to load this scene
	 * @param name name of the scene
	 * @return id of the created scene
	 */
	UUID addScene(UUID sessionId, Date startTime, float loadTime, String name) {
		UUID sceneId = UUID.randomUUID();

		// @formatter:off
		execSQL("INSERT INTO analytics_scene (session_id, scene_id, start_time, name, load_time) VALUES ('" +
				sessionId + "', '" +
				sceneId + "', " +
				startTime.getTime() + ", " +
				name + "', " +
				loadTime + ");");
		// @formatter:on

		return sceneId;
	}

	/**
	 * End scene
	 * @param sceneId the scene to end
	 * @param endTime time the scene was ended
	 */
	void endScene(UUID sceneId, Date endTime) {
		execSQL("UPDATE analytics_scene SET end_time=" + endTime + " WHERE scene_id='" + sceneId + "';");
	}

	/**
	 * Add a new event to the specified scene
	 * @param sceneId id of the scene to add this event to
	 * @param time when this event was created
	 * @param name name of the event
	 * @param data additional information about the event
	 */
	void addEvent(UUID sceneId, Date time, String name, String data) {
		// @formatter:off
		execSQL("INSERT INTO analytics_event VALUES ('" +
				sceneId + "', " +
				time.getTime() + ", '" +
				name + "', '" +
				data + "');");
		// @formatter:on
	}

	/**
	 * Remove these specified sessions, scenes and events
	 * @param sessions all session, scenes and events to remove
	 */
	void removeAnalytics(ArrayList<AnalyticsSessionEntity> sessions) {
		for (AnalyticsSessionEntity session : sessions) {
			// Delete sessions
			execSQL("DELETE FROM analytics_session WHERE session_id='" + session.sessionId + "';");

			// Delete scenes
			execSQL("DELETE FROM analytics_scene WHERE session_id='" + session.sessionId + "';");

			// Delete events
			for (AnalyticsSceneEntity scene : session.scenes) {
				execSQL("DELETE FROM analytics_event WHERE scene_id='" + scene.sceneId + "';");
			}
		}
	}


	/**
	 * @return true if there exists any analytics to be synced to the server
	 * @param skipSession skip this session id
	 */
	boolean isAnalyticsExists(UUID skipSession) {
		DatabaseCursor cursor = rawQuery("SELECT NULL FROM analytics_session WHERE session_id<>'" + skipSession + "' LIMIT 1;");

		boolean exists = cursor.next();
		cursor.close();

		return exists;
	}

	/**
	 * @return all analytics to be synced. Note that the session information is
	 *         incomplete. Additional information should be added elsewhere.
	 */
	ArrayList<AnalyticsSessionEntity> getAnalytics() {
		// Set sessions
		ArrayList<AnalyticsSessionEntity> exportSessions = new ArrayList<>();
		HashMap<UUID, AnalyticsSessionEntity> sessions = getSessions();
		for (AnalyticsSessionEntity session : sessions.values()) {
			exportSessions.add(session);
		}

		// Set scenes
		HashMap<UUID, AnalyticsSceneEntity> scenes = getScenes();
		for (AnalyticsSceneEntity scene : scenes.values()) {
			AnalyticsSessionEntity session = sessions.get(scene.sessionId);
			if (session != null) {
				session.scenes.add(scene);
			} else {
				Gdx.app.error("AnalyticsSqliteGateway", "Session not found!");
			}
		}

		// Set events
		ArrayList<AnalyticsEventEntity> events = getEvents();
		for (AnalyticsEventEntity event : events) {
			AnalyticsSceneEntity scene = scenes.get(event.sceneId);
			if (scene != null) {
				scene.events.add(event);
			} else {
				Gdx.app.error("AnalyticsSqliteGateway", "Scene not found!");
			}
		}

		return exportSessions;
	}

	/**
	 * @return all sessions
	 */
	private HashMap<UUID, AnalyticsSessionEntity> getSessions() {
		HashMap<UUID, AnalyticsSessionEntity> sessions = new HashMap<>();

		DatabaseCursor cursor = rawQuery("SELECT FROM analytics_session;");

		while (cursor.next()) {
			AnalyticsSessionEntity session = new AnalyticsSessionEntity();
			session.sessionId = UUID.fromString(cursor.getString(0));
			session.startTime = new Date(cursor.getLong(1));
			session.endTime = new Date(cursor.getLong(2));
			session.screenSize = cursor.getString(3);
			sessions.put(session.sessionId, session);
		}
		cursor.close();

		return sessions;
	}

	/**
	 * @return all scenes
	 */
	private HashMap<UUID, AnalyticsSceneEntity> getScenes() {
		HashMap<UUID, AnalyticsSceneEntity> scenes = new HashMap<>();

		DatabaseCursor cursor = rawQuery("SELECT FROM analytics_scene;");

		while (cursor.next()) {
			AnalyticsSceneEntity scene = new AnalyticsSceneEntity();
			scene.sessionId = UUID.fromString(cursor.getString(0));
			scene.sceneId = UUID.fromString(cursor.getString(1));
			scene.startTime = new Date(cursor.getLong(2));
			scene.endTime = new Date(cursor.getLong(3));
			scene.name = cursor.getString(4);
			scene.loadTime = cursor.getFloat(5);
			scenes.put(scene.sceneId, scene);
		}
		cursor.close();

		return scenes;
	}

	/**
	 * @return all events
	 */
	private ArrayList<AnalyticsEventEntity> getEvents() {
		ArrayList<AnalyticsEventEntity> events = new ArrayList<>();

		DatabaseCursor cursor = rawQuery("SELECT FROM analytics_event;");

		while (cursor.next()) {
			AnalyticsEventEntity event = new AnalyticsEventEntity();
			event.sceneId = UUID.fromString(cursor.getString(0));
			event.time = new Date(cursor.getLong(1));
			event.name = cursor.getString(2);
			event.data = cursor.getString(3);
			events.add(event);
		}
		cursor.close();

		return events;
	}
}
