package com.spiddekauga.utils.bigquery;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.services.bigquery.model.ErrorProto;
import com.google.api.services.bigquery.model.Job;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.mapreduce.impl.pipeline.DeleteFilesJob;
import com.google.appengine.tools.mapreduce.impl.util.SerializableValue;
import com.google.appengine.tools.pipeline.FutureValue;
import com.google.appengine.tools.pipeline.Job1;
import com.google.appengine.tools.pipeline.Job2;
import com.google.appengine.tools.pipeline.Value;
import com.google.common.base.Optional;

/**
 * A pipeline job to either retry or clean up the files from google cloud storage loaded
 * by the bigquery load {@link Job} based on its status.
 */
// TODO : make clean up optional. There can be cases where the users want to retain the
// files
// after load
@SuppressWarnings("javadoc")
final class RetryLoadOrCleanupJob extends Job2<BigQueryLoadJobReference, BigQueryLoadJobReference, Integer> {

	private static final long serialVersionUID = 6203149802079995465L;
	private static final Logger log = Logger.getLogger(RetryLoadOrCleanupJob.class.getName());

	private final String dataset;
	private final String tableName;
	private final String projectId;
	private final List<GcsFilename> bundle;
	private final SerializableValue<TableSchema> schema;

	/**
	 * @param bundle list of GCS files to load
	 * @param schema wrapper around a non-serializable {@link TableSchema} object.
	 */
	RetryLoadOrCleanupJob(String dataset, String tableName, String projectId, List<GcsFilename> bundle, SerializableValue<TableSchema> schema) {
		this.dataset = dataset;
		this.tableName = tableName;
		this.projectId = projectId;
		this.bundle = bundle;
		this.schema = schema;
	}

	@Override
	public Value<BigQueryLoadJobReference> run(BigQueryLoadJobReference pollResult, Integer numRetries) throws Exception {
		String queue = Optional.fromNullable(getOnQueue()).or("default");

		Job pollJob = BigQueryLoadGoogleCloudStorageFilesJob.getBigquery().jobs()
				.get(pollResult.getJobReference().getProjectId(), pollResult.getJobReference().getJobId()).execute();
		ErrorProto fatalError = pollJob.getStatus().getErrorResult();
		List<ErrorProto> errors = pollJob.getStatus().getErrors();
		if (fatalError != null) {
			log.severe("Job failed while writing to Bigquery. Retrying...#attempt " + numRetries + " Error details : " + fatalError.getReason()
					+ ": " + fatalError.getMessage() + " at " + fatalError.getLocation());

			log.severe(getErrorString(errors));

			return futureCall(new BigQueryLoadFileSetJob(dataset, tableName, projectId, bundle, schema), immediate(++numRetries), onQueue(queue));
		}
		if (errors != null) {
			log.log(Level.SEVERE, "Bigquery load job for files " + bundle
					+ " completed with following errors. Bigquery does not consider these errors fatal. Hence the job went to completion.");
			log.severe(getErrorString(errors));
		}
		FutureValue<Void> deleteJob = futureCall(new DeleteFilesJob(), immediate(bundle));
		return futureCall(new ReturnResult<BigQueryLoadJobReference>(), immediate(new BigQueryLoadJobReference("DONE", pollJob.getJobReference())),
				waitFor(deleteJob), onQueue(queue));
	}

	/**
	 * Formats all errors into a human-readable string
	 * @param errors all errors
	 * @return formatted string
	 */
	private String getErrorString(List<ErrorProto> errors) {
		if (errors == null) {
			return "";
		}

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("All Errors");
		int cErrors = 0;
		for (ErrorProto error : errors) {
			cErrors++;
			stringBuilder.append("\nError #").append(cErrors);
			stringBuilder.append("\n\tREASON:   ").append(error.getReason());
			stringBuilder.append("\n\tMESSAGE:  ").append(error.getMessage());
			stringBuilder.append("\n\tLOCATION: ").append(error.getLocation());
		}

		return stringBuilder.toString();
	}

	/**
	 * Return the argument passed as of the job.
	 */
	private static class ReturnResult<R> extends Job1<R, R> {
		private static final long serialVersionUID = -5043192512880493589L;

		@Override
		public Value<R> run(R r) throws Exception {
			return immediate(r);
		}
	}
}
