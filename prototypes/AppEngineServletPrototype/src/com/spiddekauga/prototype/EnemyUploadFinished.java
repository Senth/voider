package com.spiddekauga.prototype;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Enemy has been uploaded
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class EnemyUploadFinished extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		mLogger.info("Upload done");


	}

	/** Logger */
	private static final Logger mLogger = Logger.getLogger(EnemyUploadFinished.class.getName());
}
