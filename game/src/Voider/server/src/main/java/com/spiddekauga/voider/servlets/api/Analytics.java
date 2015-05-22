package com.spiddekauga.voider.servlets.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.analytics.AnalyticsEventEntity;
import com.spiddekauga.voider.network.analytics.AnalyticsMethod;
import com.spiddekauga.voider.network.analytics.AnalyticsResponse;
import com.spiddekauga.voider.network.analytics.AnalyticsSceneEntity;
import com.spiddekauga.voider.network.analytics.AnalyticsSessionEntity;
import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CAnalyticsEvent;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CAnalyticsScene;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CAnalyticsSession;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Analytics for Voider
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Analytics extends VoiderApiServlet {
	@Override
	protected void onInit() {
		mResponse = new AnalyticsResponse();
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {

		if (mUser.isLoggedIn()) {
			if (methodEntity instanceof AnalyticsMethod) {
				mParameters = (AnalyticsMethod) methodEntity;
				saveSessions();

				if (mResponse.status == null) {
					mResponse.status = GeneralResponseStatuses.SUCCESS;
				}
			} else {
				mResponse.status = GeneralResponseStatuses.FAILED_SERVER_ERROR;
			}
		} else {
			mResponse.status = GeneralResponseStatuses.FAILED_USER_NOT_LOGGED_IN;
		}

		return mResponse;
	}

	/**
	 * Parse sessions
	 */
	private void saveSessions() {
		ArrayList<Entity> sessionEntities = new ArrayList<>();
		for (AnalyticsSessionEntity networkEntity : mParameters.sessions) {
			Entity datastoreEntity = new Entity(DatastoreTables.ANALYTICS_SESSION);
			datastoreEntity.setProperty(CAnalyticsSession.EXPORTED, false);

			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsSession.START_TIME, networkEntity.startTime);
			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsSession.USER_ANALYTICS_ID, mParameters.userAnalyticsId);
			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsSession.PLATFORM, mParameters.platform);
			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsSession.OS, mParameters.os);
			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsSession.SCREEN_SIZE, networkEntity.screenSize);

			// Length
			long diffDate = networkEntity.endTime.getTime() - networkEntity.startTime.getTime();
			double seconds = diffDate / 1000d;
			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsSession.LENGTH, seconds);

			sessionEntities.add(datastoreEntity);
		}

		List<Key> sessionKeys = DatastoreUtils.put(sessionEntities);

		if (sessionKeys != null && sessionKeys.size() == mParameters.sessions.size()) {
			for (int i = 0; i < mParameters.sessions.size(); i++) {
				saveScenes(sessionKeys.get(i), mParameters.sessions.get(i).scenes);
			}
		} else {
			mResponse.status = GeneralResponseStatuses.SUCCESS_PARTIAL;
		}
	}

	/**
	 * Save scenes
	 * @param sessionKey session the scenes belong to
	 * @param scenes all scenes to save
	 */
	private void saveScenes(Key sessionKey, List<AnalyticsSceneEntity> scenes) {
		ArrayList<Entity> sceneEntities = new ArrayList<>();

		for (AnalyticsSceneEntity networkEntity : scenes) {
			Entity datastoreEntity = new Entity(DatastoreTables.ANALYTICS_SCENE, sessionKey);

			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsScene.START_TIME, networkEntity.startTime);
			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsScene.NAME, networkEntity.name);
			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsScene.LOAD_TIME, networkEntity.loadTime);
			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsScene.DROPOUT, networkEntity.dropout);
			datastoreEntity.setProperty(CAnalyticsScene.EXPORTED, false);

			// Length
			long diffMs = networkEntity.endTime.getTime() - networkEntity.startTime.getTime();
			double seconds = diffMs / 1000d;
			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsScene.LENGTH, seconds);

			sceneEntities.add(datastoreEntity);
		}

		List<Key> sceneKeys = DatastoreUtils.put(sceneEntities);

		if (sceneKeys != null && sceneKeys.size() == scenes.size()) {
			for (int i = 0; i < scenes.size(); ++i) {
				AnalyticsSceneEntity scene = scenes.get(i);
				saveEvents(sceneKeys.get(i), scene.startTime, scene.events);
			}
		} else {
			mResponse.status = GeneralResponseStatuses.SUCCESS_PARTIAL;
		}
	}

	/**
	 * Save events
	 * @param sceneKey scene the event belongs to
	 * @param sceneStartTime start time of the scene
	 * @param events all events to save
	 */
	private void saveEvents(Key sceneKey, Date sceneStartTime, List<AnalyticsEventEntity> events) {
		ArrayList<Entity> eventEntities = new ArrayList<>();

		for (AnalyticsEventEntity networkEntity : events) {
			Entity datastoreEntity = new Entity(DatastoreTables.ANALYTICS_EVENT, sceneKey);

			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsEvent.NAME, networkEntity.name);
			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsEvent.DATA, networkEntity.data);
			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsEvent.TYPE, networkEntity.type.toId());
			datastoreEntity.setProperty(CAnalyticsEvent.EXPORTED, false);

			// Time
			long diffMs = networkEntity.time.getTime() - sceneStartTime.getTime();
			double time = diffMs * 0.001;
			DatastoreUtils.setUnindexedProperty(datastoreEntity, CAnalyticsEvent.TIME, time);

			eventEntities.add(datastoreEntity);
		}

		List<Key> eventKeys = DatastoreUtils.put(eventEntities);

		if (eventKeys == null || eventKeys.size() != events.size()) {
			mResponse.status = GeneralResponseStatuses.SUCCESS_PARTIAL;
		}
	}

	/** Parameters */
	private AnalyticsMethod mParameters = null;
	/** Response */
	private AnalyticsResponse mResponse = null;
}
