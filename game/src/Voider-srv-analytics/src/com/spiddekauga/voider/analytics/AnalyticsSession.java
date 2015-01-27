package com.spiddekauga.voider.analytics;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.appengine.api.datastore.Key;

/**
 * Analytics session information for BigQuery
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("unused")
@JsonIgnoreProperties({ "key" })
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
	 * @return all scenes
	 */
	List<AnalyticsScene> getScenes() {
		return scenes;
	}

	/**
	 * @return datastore key of this scene
	 */
	Key getKey() {
		return key;
	}

	private Key key;
	private long startTime;
	private double length;
	private String userAnalyticsId;
	private String platform;
	private String os;
	private String screenSize;
	private List<AnalyticsScene> scenes = null;
	private static final long serialVersionUID = -797860034811523577L;
}
