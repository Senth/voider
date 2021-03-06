package com.spiddekauga.voider.analytics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.appengine.api.datastore.Key;

/**
 * Analytics session information for BigQuery

 */
public class AnalyticsSession implements Serializable {
	/**
	 * Create a session without any scenes.
	 * @param key datastore key
	 * @param startTime
	 * @param length
	 * @param userAnalyticsId
	 * @param platform
	 * @param os
	 * @param screenSize
	 */
	public AnalyticsSession(Key key, Date startTime, double length, UUID userAnalyticsId, String platform, String os, String screenSize) {
		this.key = key;
		this.startTime = startTime.getTime();
		this.length = length;
		this.userAnalyticsId = userAnalyticsId.toString();
		this.platform = platform;
		this.os = os;
		this.screenSize = screenSize;
	}

	/**
	 * Set all scenes for this session
	 * @param scenes
	 */
	void setScenes(List<AnalyticsScene> scenes) {
		this.scenes = scenes;
	}

	/**
	 * Adds another scene to this session
	 * @param scene
	 */
	void addScene(AnalyticsScene scene) {
		scenes.add(scene);
	}

	/**
	 * @return all scenes
	 */
	public List<AnalyticsScene> getScenes() {
		return scenes;
	}

	/**
	 * @return datastore key of this scene
	 */
	Key getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (key == null) {
			return false;
		} else {
			return key.equals(obj);
		}
	}

	/**
	 * @return start time
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @return length of the session
	 */
	public double getLength() {
		return length;
	}

	/**
	 * @return user analytics id
	 */
	public String getUserAnalyticsId() {
		return userAnalyticsId;
	}

	/**
	 * @return platform
	 */
	public String getPlatform() {
		return platform;
	}

	/**
	 * @return user OS
	 */
	public String getOs() {
		return os;
	}

	/**
	 * @return screen size
	 */
	public String getScreenSize() {
		return screenSize;
	}

	private Key key;
	private long startTime;
	private double length;
	private String userAnalyticsId;
	private String platform;
	private String os;
	private String screenSize;
	private List<AnalyticsScene> scenes = new ArrayList<>();
	private static final long serialVersionUID = -797860034811523577L;
}
