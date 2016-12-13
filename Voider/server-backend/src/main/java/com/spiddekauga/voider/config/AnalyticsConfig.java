package com.spiddekauga.voider.config;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.appengine.tools.mapreduce.MapSettings;
import com.google.appengine.tools.pipeline.JobSetting;


/**
 * Analytics server configuration

 */
public class AnalyticsConfig {
	/** Number of shards for datastore inputs */
	public static final int SHARDS_PER_QUERY = 5;
	/** Big Query dataset for analytics */
	public static final String BIG_DATASET_NAME = "analytics";
	/** Big Query table for analytics */
	public static final String BIG_TABLE_NAME = "clientEvents";

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

	private static TableSchema mClientSchema = new TableSchema();

	// Initialize table schemas
	/**
	 * Field types
	 */
	private static enum Types {
		STRING,
		INTEGER,
		FLOAT,
		RECORD,
		BOOLEAN,
		TIMESTAMP,
	}

	/**
	 * Helper method for creating table schema fields
	 * @param name the name of the field
	 * @param type type of the field
	 * @return table field schema
	 */
	private static TableFieldSchema newFieldSchema(String name, Types type) {
		return new TableFieldSchema().setName(name).setType(type.name());
	}

	/**
	 * Helper method for creating table schema fields with a record
	 * @param name name of the field
	 * @param fields schema of the fields
	 * @return field schema
	 */
	private static TableFieldSchema newFieldSchema(String name, List<TableFieldSchema> fields) {
		return newFieldSchema(name, Types.RECORD).setFields(fields).setMode("REPEATED");
	}

	/**
	 * @return BigQuery table schema for client events
	 */
	public static TableSchema getClientSchema() {
		return mClientSchema;
	}

	static {
		// Event fields
		ArrayList<TableFieldSchema> eventFields = new ArrayList<>();
		eventFields.add(newFieldSchema("time", Types.FLOAT));
		eventFields.add(newFieldSchema("name", Types.STRING));
		eventFields.add(newFieldSchema("data", Types.STRING));
		eventFields.add(newFieldSchema("type", Types.INTEGER));

		// Scene fields
		ArrayList<TableFieldSchema> sceneFields = new ArrayList<>();
		sceneFields.add(newFieldSchema("startTime", Types.INTEGER));
		sceneFields.add(newFieldSchema("length", Types.FLOAT));
		sceneFields.add(newFieldSchema("name", Types.STRING));
		sceneFields.add(newFieldSchema("loadTime", Types.FLOAT));
		sceneFields.add(newFieldSchema("dropout", Types.BOOLEAN));
		sceneFields.add(newFieldSchema("events", eventFields));

		// Session fields
		ArrayList<TableFieldSchema> sessionFields = new ArrayList<>();
		sessionFields.add(newFieldSchema("startTime", Types.INTEGER));
		sessionFields.add(newFieldSchema("length", Types.FLOAT));
		sessionFields.add(newFieldSchema("userAnalyticsId", Types.STRING));
		sessionFields.add(newFieldSchema("platform", Types.STRING));
		sessionFields.add(newFieldSchema("os", Types.STRING));
		sessionFields.add(newFieldSchema("screenSize", Types.STRING));
		sessionFields.add(newFieldSchema("scenes", sceneFields));

		mClientSchema.setFields(sessionFields);
	}

}
