package com.spiddekauga.utils.bigquery;

import java.io.Serializable;

import com.google.api.services.bigquery.model.JobReference;
import com.google.appengine.tools.mapreduce.Marshallers;
import com.google.appengine.tools.mapreduce.impl.util.SerializableValue;

/**
 * Result of the bigquery load files pipeline job.
 */
@SuppressWarnings("javadoc")
public class BigQueryLoadJobReference implements Serializable {

	private static final long serialVersionUID = -5045977572520245900L;
	private final String status;
	private final SerializableValue<JobReference> jobReference;

	public BigQueryLoadJobReference(String status, JobReference jobReference) {
		this.status = status;
		this.jobReference = SerializableValue.of(Marshallers.getGenericJsonMarshaller(JobReference.class), jobReference);
	}

	public String getStatus() {
		return status;
	}

	public JobReference getJobReference() {
		return jobReference.getValue();
	}
}
