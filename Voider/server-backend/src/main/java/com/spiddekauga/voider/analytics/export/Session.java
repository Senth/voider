package com.spiddekauga.voider.analytics.export;

import com.google.appengine.api.datastore.Key;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Analytics session information for BigQuery
 */
public class Session implements Serializable {
private static final long serialVersionUID = -797860034811523577L;
private Key key;
private long startTime;
private double length;
private String userAnalyticsId;
private String platform;
private String os;
private String screenSize;
private List<Scene> scenes = new ArrayList<>();

/**
 * Create a session without any scenes.
 * @param key datastore key
 */
public Session(Key key, Date startTime, double length, UUID userAnalyticsId, String platform, String os, String screenSize) {
	this.key = key;
	this.startTime = startTime.getTime();
	this.length = length;
	this.userAnalyticsId = userAnalyticsId.toString();
	this.platform = platform;
	this.os = os;
	this.screenSize = screenSize;
}

/**
 * Adds another scene to this session
 */
void addScene(Scene scene) {
	scenes.add(scene);
}

/**
 * @return all scenes
 */
public List<Scene> getScenes() {
	return scenes;
}

/**
 * Set all scenes for this session
 */
void setScenes(List<Scene> scenes) {
	this.scenes = scenes;
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
	return key != null && key.equals(obj);
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
}
