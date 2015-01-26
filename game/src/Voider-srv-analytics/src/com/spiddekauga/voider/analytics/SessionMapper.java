package com.spiddekauga.voider.analytics;

import java.util.Date;
import java.util.UUID;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.spiddekauga.appengine.DatastoreUtils;

/**
 * Maps sessions from a datastore object into an AnalyticsSession object
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class SessionMapper extends MapOnlyMapper<Entity, AnalyticsSession> {
	@Override
	public void map(Entity value) {
		Entity sessionEntity = DatastoreUtils.getEntity(value.getKey());

		Date startTime = (Date) sessionEntity.getProperty("start_time");
		double length = (Double) sessionEntity.getProperty("length");
		UUID userAnalyticsId = DatastoreUtils.getUuidProperty(sessionEntity, "user_analytics_id");
		String platform = (String) sessionEntity.getProperty("platform");
		String os = (String) sessionEntity.getProperty("os");
		String screenSize = (String) sessionEntity.getProperty("screen_size");

		emit(new AnalyticsSession(value.getKey(), startTime, length, userAnalyticsId, platform, os, screenSize));
	}
}
