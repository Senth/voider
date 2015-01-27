package com.spiddekauga.voider.config;

import com.google.appengine.tools.mapreduce.MapSettings;
import com.google.appengine.tools.pipeline.JobSetting;


/**
 * Analytics server configuration
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class AnalyticsConfig {
	/** Number of shards for datastore inputs */
	public static final int SHARDS_PER_QUERY = 5;

	/**
	 * @param settings optional extra parameters
	 * @return job settings
	 */
	public static JobSetting[] getJobSettings(JobSetting... settings) {
		JobSetting[] defaultSettings = new JobSetting[] { new JobSetting.OnQueue(QUEUE_NAME) };

		if (settings.length == 0) {
			return defaultSettings;
		} else {
			JobSetting[] combinedSettings = new JobSetting[settings.length + defaultSettings.length];
			for (int i = 0; i < defaultSettings.length; i++) {
				combinedSettings[i] = defaultSettings[i];
			}
			int offset = defaultSettings.length;
			for (int i = 0; i < settings.length; i++) {
				combinedSettings[i + offset] = settings[i];
			}
			return combinedSettings;
		}
	}

	/**
	 * @return map settings
	 */
	public static MapSettings getMapSettings() {
		return new MapSettings.Builder().setWorkerQueueName(QUEUE_NAME).build();
	}

	private static final String QUEUE_NAME = "analytics-queue";
}
