package com.spiddekauga.voider.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spiddekauga.appengine.DatastoreUtils;

/**
 * Cleanup pipelins that finished, or didn't finish correctly
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Cleanup extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		clearTables();
	}

	/**
	 * Clear tables
	 */
	private void clearTables() {
		for (String table : TABLES_TO_CLEAR) {
			DatastoreUtils.delete(DatastoreUtils.getKeys(table));
		}
	}


	/** All tables to clear */
	// @formatter:off
	private static final String[] TABLES_TO_CLEAR = {
		"MR-IncrementalTask",
		"MR-ShardedJob",
		"pipeline-barrier",
		"pipeline-exception",
		"pipeline-fanoutTask",
		"pipeline-job",
		"pipeline-jobInstanceRecord",
		"pipeline-slot",
	};
	// @formatter:on
}
