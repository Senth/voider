package com.spiddekauga.voider.server.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

/**
 * Controller for the all web stuff
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public abstract class VoiderController extends HttpServlet {
	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp);
	}

	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp);
	}

	/**
	 * Handle both get and post requests
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		mRequest = req;
		mResponse = resp;

		mOut = mResponse.getWriter();
		mResponse.setContentType("text/html");

		onInit();
		onRequest();


		mOut.close();
	}

	/**
	 * This method is called on init (before onRequest is called)
	 */
	protected void onInit() {
		// Does nothing
	}

	/**
	 * Handle the request
	 */
	protected abstract void onRequest();

	/**
	 * @param name name of the parameter
	 * @return true if a parameter exists
	 */
	protected boolean isParameterExists(String name) {
		return mRequest.getParameter(name) != null;
	}

	/**
	 * @param name name of the parameter to get
	 * @return parameter value or null if it doesn't exist
	 */
	protected String getParameter(String name) {
		return mRequest.getParameter(name);
	}

	/**
	 * Set redirect location
	 * @param url where to redirect
	 */
	protected void redirect(String url) {
		mResponse.setHeader("Location", url);
	}

	/**
	 * Convert a UUID to Base64
	 * @param uuid
	 * @return base64 string
	 */
	protected static String toBase64(UUID uuid) {
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		byte[] buffer = new byte[16];

		for (int i = 0; i < 8; i++) {
			buffer[i] = (byte) (msb >>> 8 * (7 - i));
		}
		for (int i = 8; i < 16; i++) {
			buffer[i] = (byte) (lsb >>> 8 * (7 - i));
		}

		return DatatypeConverter.printBase64Binary(buffer);
	}

	private HttpServletRequest mRequest = null;
	private HttpServletResponse mResponse = null;
	private PrintWriter mOut = null;
}
