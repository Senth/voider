package com.spiddekauga.voider.analytics.export;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.appengine.tools.cloudstorage.GcsFilename;

import java.util.ArrayList;
import java.util.List;

import static com.spiddekauga.voider.BackendConfig.APP_ID;


/**
 * Analytics server configuration
 */
class AnalyticsConfig {
/** Big Query dataset for analytics */
static final String BIG_DATASET_NAME = "analytics";
/** Big Query table for analytics */
static final String BIG_TABLE_NAME = "clientEvents";
static final String GCS_FILENAME = "analytics.json";
/** Temporary Google Cloud Storage filename */
static final String GCS_BUCKET = APP_ID;
static final GcsFilename GCS_FILE = new GcsFilename(GCS_BUCKET, GCS_FILENAME);
private static TableSchema mClientSchema = new TableSchema();

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
 * Helper method for creating table schema fields
 * @param name the name of the field
 * @param type type of the field
 * @return table field schema
 */
private static TableFieldSchema newFieldSchema(String name, Types type) {
	return new TableFieldSchema().setName(name).setType(type.name());
}

/**
 * @return BigQuery table schema for client events
 */
static TableSchema getClientSchema() {
	return mClientSchema;
}

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

}
