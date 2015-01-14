package com.spiddekauga.voider.repo.analytics;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.repo.Repo;

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

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		// TODO Auto-generated method stub
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
	void endSession() {
		mLocalRepo.endSession();
	}

	/**
	 * Start a new scene analytics
	 * @param name scene name
	 */
	void startScene(String name) {
		mLocalRepo.startScene(name);
	}

	/**
	 * End the current scene
	 */
	void endScene() {
		mLocalRepo.endScene();
	}

	/**
	 * Add an event to the current scene
	 * @param name event name
	 * @param data extra information about the event
	 */
	void addEvent(String name, String data) {
		mLocalRepo.addEvent(name, data);
	}

	private AnalyticsLocalRepo mLocalRepo = AnalyticsLocalRepo.getInstance();
	private static AnalyticsRepo mInstance = null;
}
