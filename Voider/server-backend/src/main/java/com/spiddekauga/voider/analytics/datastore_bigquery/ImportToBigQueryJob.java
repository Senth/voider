package com.spiddekauga.voider.analytics.datastore_bigquery;

import com.google.appengine.tools.mapreduce.GoogleCloudStorageFileSet;
import com.google.appengine.tools.mapreduce.outputs.BigQueryStoreResult;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Value;
import com.spiddekauga.appengine.pipeline.BigQueryLoadGoogleCloudStorageFilesJob;
import com.spiddekauga.appengine.pipeline.BigQueryLoadJobReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Job for importing analytics to the google cloud storage
 */
class ImportToBigQueryJob extends Job0<List<BigQueryLoadJobReference>> {
@Override
public Value<List<BigQueryLoadJobReference>> run() throws Exception {

	BigQueryLoadGoogleCloudStorageFilesJob bigQueryJob = new BigQueryLoadGoogleCloudStorageFilesJob(AnalyticsConfig.BIG_DATASET_NAME,
			AnalyticsConfig.BIG_TABLE_NAME, AnalyticsConfig.APP_ID);

	List<String> fileNames = new ArrayList<>();
	fileNames.add(AnalyticsConfig.GCS_FILENAME);
	GoogleCloudStorageFileSet fileSet = new GoogleCloudStorageFileSet(AnalyticsConfig.GCS_BUCKET, fileNames);
	BigQueryStoreResult<GoogleCloudStorageFileSet> storeResult = new BigQueryStoreResult<GoogleCloudStorageFileSet>(fileSet,
			AnalyticsConfig.getClientSchema());

	return futureCall(bigQueryJob, immediate(storeResult), AnalyticsConfig.getJobSettings());
}

@Override
public String getJobDisplayName() {
	return "Import to Big Query";
}
}
