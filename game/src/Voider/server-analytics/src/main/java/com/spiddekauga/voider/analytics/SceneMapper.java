package com.spiddekauga.voider.analytics;

import java.util.Date;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.spiddekauga.appengine.DatastoreUtils;

/**
 * Maps scenes from a datastore object into an AnalyticsScene object
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class SceneMapper extends MapOnlyMapper<Entity, AnalyticsScene> {
	@Override
	public void map(Entity value) {
		Entity sceneEntity = DatastoreUtils.getEntity(value.getKey());

		Date startTime = (Date) sceneEntity.getProperty("start_time");
		double length = (Double) sceneEntity.getProperty("length");
		String name = (String) sceneEntity.getProperty("name");
		double loadTime = (Double) sceneEntity.getProperty("load_time");
		boolean dropout = (Boolean) sceneEntity.getProperty("dropout");

		// @formatter:off
//		Logger logger = Logger.getLogger("SceneMapper");
//		logger.info("Emitting new Scene\n"
//				+ "Key: " + value.getKey()
//				+ "\nStart Time: " + startTime
//				+ "\nLength: " + length
//				+ "\nName: " + name
//				+ "\nLoad Time: " + loadTime
//				+ "\nDropout: " + dropout);
		// @formatter:on

		emit(new AnalyticsScene(value.getKey(), value.getParent(), startTime, length, name, loadTime, dropout));
	}
}
