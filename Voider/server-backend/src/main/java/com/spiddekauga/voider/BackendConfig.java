package com.spiddekauga.voider;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.mapreduce.MapSettings;
import com.google.appengine.tools.pipeline.JobSetting;

/**
 * Backend configurations
 */
public class BackendConfig {
public static final String APP_ID = SystemProperty.applicationId.get();
/** Number of shards (threads) for datastore inputs */
public static final int SHARDS_PER_QUERY = 5;
private static final String QUEUE_NAME = "backend";

/**
 * @param settings optional extra parameters
 * @return job settings
 */
public static JobSetting[] getJobSettings(JobSetting... settings) {
	JobSetting[] defaultSettings = new JobSetting[]{new JobSetting.OnQueue(QUEUE_NAME)};

	if (settings.length == 0) {
		return defaultSettings;
	} else {
		JobSetting[] combinedSettings = new JobSetting[settings.length + defaultSettings.length];
		System.arraycopy(defaultSettings, 0, combinedSettings, 0, defaultSettings.length);
		int offset = defaultSettings.length;
		System.arraycopy(settings, 0, combinedSettings, offset, settings.length);
		return combinedSettings;
	}
}

/**
 * @return map settings
 */
public static MapSettings getMapSettings() {
	return new MapSettings.Builder().setWorkerQueueName(QUEUE_NAME).build();
}
}
