package com.spiddekauga.voider.analytics;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.spiddekauga.appengine.DatastoreUtils;

/**
 * Maps events from a datastore object into an AnalyticsEvent object
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class EventMapper extends MapOnlyMapper<Entity, AnalyticsEvent> {
	@Override
	public void map(Entity value) {
		Entity eventEntity = DatastoreUtils.getEntity(value.getKey());

		double time = (Double) eventEntity.getProperty("time");
		String name = (String) eventEntity.getProperty("name");
		String data = (String) eventEntity.getProperty("data");
		int type = ((Long) eventEntity.getProperty("type")).intValue();

		// @formatter:off
//		Logger logger = Logger.getLogger("EntityMapper");
//		logger.info("Emitting new event\n"
//				+ "Key: " + value.getKey()
//				+ "\nTime: " + time
//				+ "\nName: " + name
//				+ "\nData: " + data
//				+ "\nType: " + type);
		// @formatter:on

		emit(new AnalyticsEvent(time, type, name, data));
	}
}
