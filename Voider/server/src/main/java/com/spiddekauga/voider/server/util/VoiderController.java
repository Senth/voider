package com.spiddekauga.voider.server.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.xml.bind.DatatypeConverter;

import com.google.common.base.CaseFormat;

/**
 * Controller for the all web stuff

 */
@SuppressWarnings("serial")
public abstract class VoiderController extends VoiderServlet {

	@Override
	protected void handleRequest() throws ServletException, IOException {
		setUrls();

		mOut = getResponse().getWriter();
		getResponse().setContentType("text/html");

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
	protected boolean isParameterSet(String name) {
		return getRequest().getParameter(name) != null;
	}

	/**
	 * @param name name of the parameter to get
	 * @return parameter value or null if it doesn't exist
	 */
	protected String getParameter(String name) {
		// Check regular parameter
		String value = getRequest().getParameter(name);

		return value;
	}

	/**
	 * Set redirect location
	 * @param url where to redirect
	 */
	protected void redirect(String url) {
		try {
			getResponse().sendRedirect(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		return DatatypeConverter.printBase64Binary(buffer).split("=")[0];
	}

	/**
	 * @return get all parameters
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, String[]> getParameters() {
		return getRequest().getParameterMap();
	}

	/**
	 * @return root URL
	 */
	protected String getRootUrl() {
		return mRootUrl;
	}

	/**
	 * @return servlet path/URI
	 */
	protected String getServletUri() {
		return mServletUri;
	}

	/**
	 * Set server and servlet url
	 */
	private void setUrls() {
		// Remove first string;
		mServletUri = getRequest().getRequestURI().substring(1);

		// Set root url
		mRootUrl = getRequest().getRequestURL().toString();
		int serveltPos = mRootUrl.indexOf(mServletUri);
		if (serveltPos != -1) {
			mRootUrl = mRootUrl.substring(0, serveltPos);
		}
	}

	/**
	 * Set the status response
	 * @param type type of the message
	 * @param message
	 */
	protected void setResponseMessage(String type, String message) {
		getRequest().setAttribute("responseStatus", new ResponseMessage(type, message));
	}

	/**
	 * @return printer writer for writing the response
	 */
	protected PrintWriter getResponsePrintWriter() {
		return mOut;
	}

	/**
	 * Forward/Redirect to the HTML page
	 */
	protected void forwardToHtml() {
		try {
			String jspName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, getClass().getSimpleName()) + ".jsp";
			getRequest().getRequestDispatcher(jspName).forward(getRequest(), getResponse());
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * For sending success and error messages
	 */
	public class ResponseMessage {
		/**
		 * New response message
		 * @param type type of message (usually success or error)
		 * @param message text message
		 */
		public ResponseMessage(String type, String message) {
			this.type = type;
			this.message = message;
		}

		/**
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/** Type of message */
		private String type;
		/** Message */
		private String message;
	}

	private String mRootUrl = null;
	private String mServletUri = null;
	private PrintWriter mOut = null;

}
