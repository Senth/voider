package com.spiddekauga.voider.backup;

import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.spiddekauga.utils.Time;
import com.spiddekauga.voider.BackendConfig;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Restore the datastore to a specific backup
 */
public class RestoreServlet extends HttpServlet {
@Override
protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	String restoreTo = req.getParameter("restore_to");

	if (restoreTo != null) {
		SimpleDateFormat simpleDateFormat = Time.createIsoDateFormat();
		try {
			Date date = simpleDateFormat.parse(restoreTo);
			PipelineService pipelineService = PipelineServiceFactory.newPipelineService();
			String rootHandle = pipelineService.startNewPipeline(new RestoreJob(), date, BackendConfig.getJobSettings());
			resp.sendRedirect("/_ah/pipeline/status.html?root=" + rootHandle);
		} catch (ParseException e) {
			log("Date parse error", e);
		}
	}
}
}
