package com.spiddekauga.voider.analytics.datastore_bigquery;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;

/**
 * Creates a pipeline that converts new analytics datastore entries to

 */
@SuppressWarnings("serial")
public class AnalyticsDatastoreToBigQueryServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PipelineService pipelineService = PipelineServiceFactory.newPipelineService();
		String rootHandle = pipelineService.startNewPipeline(new AnalyticsDatastoreToBigQueryJob(), AnalyticsConfig.getJobSettings());

		if (req.getParameter("redirect") != null) {
			resp.sendRedirect("/_ah/pipeline/status.html?root=" + rootHandle);
		}
	}
}
