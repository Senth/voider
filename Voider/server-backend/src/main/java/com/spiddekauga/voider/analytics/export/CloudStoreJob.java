package com.spiddekauga.voider.analytics.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.appengine.tools.pipeline.Job1;
import com.google.appengine.tools.pipeline.Value;

import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.List;

/**
 * Job for printing all the new analytics session to the google cloud storage
 */
class CloudStoreJob extends Job1<Void, List<Session>> {
@Override
public Value<Void> run(List<Session> sessions) throws Exception {
	ObjectMapper jackson = new ObjectMapper();
	GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
	StringBuilder builder = new StringBuilder();

	for (Session session : sessions) {
		String sessionJson = jackson.writeValueAsString(session);
		builder.append(sessionJson).append('\n');
	}

	String json = builder.toString();


	GcsFileOptions.Builder optionBuilder = new GcsFileOptions.Builder();
	optionBuilder.contentEncoding("UTF-8");
	optionBuilder.mimeType("application/json");
	GcsOutputChannel outputChannel = gcsService.createOrReplace(AnalyticsConfig.GCS_FILE, optionBuilder.build());
	PrintWriter printWriter = new PrintWriter(Channels.newWriter(outputChannel, "UTF-8"));
	printWriter.write(json);
	printWriter.close();

	return immediate(null);
}

@Override
public String getJobDisplayName() {
	return "Save JSON in Cloud Storage";
}
}
