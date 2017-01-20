package com.spiddekauga.voider.analytics.export;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.spiddekauga.appengine.DatastoreUtils;

import java.util.Date;
import java.util.UUID;

/**
 * Maps sessions from a datastore object into an Session object
 */
@SuppressWarnings("serial")
public class SessionMapper extends MapOnlyMapper<Entity, Session> {
@Override
public void map(Entity value) {
	Entity sessionEntity = DatastoreUtils.getEntity(value.getKey());

	Date startTime = (Date) sessionEntity.getProperty("start_time");
	double length = (Double) sessionEntity.getProperty("length");
	UUID userAnalyticsId = DatastoreUtils.getPropertyUuid(sessionEntity, "user_analytics_id");
	String platform = (String) sessionEntity.getProperty("platform");
	String os = (String) sessionEntity.getProperty("os");
	String screenSize = (String) sessionEntity.getProperty("screen_size");

	// @formatter:off
//		Logger logger = Logger.getLogger("SessionMapper");
//		logger.info("Emitting new session\n"
//				+ "Key: " + value.getKey() + "\n"
//				+ "StartTime: " + startTime + "\n"
//				+ "Length: " + length + "\n"
//				+ "User ID: " + userAnalyticsId + "\n"
//				+ "Platform: " + platform + "\n"
//				+ "OS: " + os + "\n"
//				+ "Screen Size: " + screenSize);
		// @formatter:on

	Session session = new Session(value.getKey(), startTime, length, userAnalyticsId, platform, os, screenSize);
	emit(session);
}
}
