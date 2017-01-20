package com.spiddekauga.voider.analytics.export;

import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.spiddekauga.voider.BackendConfig;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Creates a pipeline that converts new analytics datastore entries to big query data
 */
@SuppressWarnings("serial")
public class AnalyticsExportServlet extends HttpServlet {
@Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	PipelineService pipelineService = PipelineServiceFactory.newPipelineService();
	String rootHandle = pipelineService.startNewPipeline(new AnalyticsExportJob(), BackendConfig.getJobSettings());

	if (req.getParameter("redirect") != null) {
		resp.sendRedirect("/_ah/pipeline/status.html?root=" + rootHandle);
	}
}
}
