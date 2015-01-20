package com.spiddekauga.voider.repo.analytics;

import java.util.UUID;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.network.analytics.AnalyticsMethod;
import com.spiddekauga.voider.network.analytics.AnalyticsMethodResponse;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.Repo;
import com.spiddekauga.voider.repo.user.UserLocalRepo;

/**
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class AnalyticsRepo extends Repo {
	/**
	 * Private constructor to enforce singleton pattern
	 */
	private AnalyticsRepo() {
		// Does nothing
	}

	/**
	 * @return instance of this class
	 */
	public static AnalyticsRepo getInstance() {
		if (mInstance == null) {
			mInstance = new AnalyticsRepo();
		}
		return mInstance;
	}

	/**
	 * Creates a new session
	 */
	public void newSession() {
		mLocalRepo.newSession();
	}

	/**
	 * Ends a session
	 */
	public void endSession() {
		mLocalRepo.endSession();
	}

	/**
	 * Start a new scene analytics
	 * @param name scene name
	 */
	public void startScene(String name) {
		mLocalRepo.startScene(name);
	}

	/**
	 * End the current scene
	 */
	public void endScene() {
		mLocalRepo.endScene();
	}

	/**
	 * Add an event without data to the current scene
	 * @param name event name
	 */
	public void addEvent(String name) {
		addEvent(name, "");
	}

	/**
	 * Add an event to the current scene
	 * @param name event name
	 * @param data extra information about the event
	 */
	public void addEvent(String name, String data) {
		mLocalRepo.addEvent(name, data);
	}

	/**
	 * @return all events in the current scene. Useful when sending debug messages
	 */
	public String getSessionDebug() {
		return mLocalRepo.getSessionDebug();
	}

	/**
	 * Sync analytics to the server
	 * @param responseListeners listeners of the web response
	 */
	public void sync(IResponseListener... responseListeners) {
		mWebRepo.sync(mLocalRepo.getAnalytics(), getPlatform(), getOs(), getUserAnalyticsId(), addToFront(responseListeners, this));
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof AnalyticsMethodResponse) {
			handleSyncResponse((AnalyticsMethod) method, (AnalyticsMethodResponse) response);
		}
	}

	/**
	 * Handle sync response from the server
	 * @param method parameters sent to the server
	 * @param response server response
	 */
	private void handleSyncResponse(AnalyticsMethod method, AnalyticsMethodResponse response) {
		if (response.isSuccessful()) {
			mLocalRepo.removeAnalytics(method.sessions);
		}
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

	private AnalyticsWebRepo mWebRepo = AnalyticsWebRepo.getInstance();
	private AnalyticsLocalRepo mLocalRepo = AnalyticsLocalRepo.getInstance();
	private static AnalyticsRepo mInstance = null;
}
