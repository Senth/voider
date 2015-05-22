package com.spiddekauga.voider.server.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
	private void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		mRequest = req;
		mResponse = resp;
		setUrls();

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
	protected boolean isParameterSet(String name) {
		return mRequest.getParameter(name) != null;
	}

	/**
	 * @param name name of the parameter to get
	 * @return parameter value or null if it doesn't exist
	 */
	protected String getParameter(String name) {
		// Check regular parameter
		String value = mRequest.getParameter(name);

		return value;
	}

	/**
	 * Set redirect location
	 * @param url where to redirect
	 */
	protected void redirect(String url) {
		try {
			mResponse.sendRedirect(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send an email
	 * @param email who to send the email to
	 * @param subject
	 * @param content html content of the email
	 */
	protected void sendEmail(String email, String subject, String content) {
		Session session = Session.getDefaultInstance(new Properties());
		MimeMessage message = new MimeMessage(session);

		try {
			mLogger.info("Trying to send a mail to " + email);
			message.setFrom(ServerConfig.EMAIL_ADMIN);
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
			message.setSubject(subject);
			message.setContent(content, "text/html");
			mLogger.info("Calling Transport.send(message);");
			Transport.send(message);
		} catch (MessagingException e) {
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
		return mRequest.getParameterMap();
	}

	/**
	 * @return current request
	 */
	protected HttpServletRequest getRequest() {
		return mRequest;
	}

	/**
	 * @return current response
	 */
	protected HttpServletResponse getResponse() {
		return mResponse;
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
		mServletUri = mRequest.getRequestURI().substring(1);

		// Set root url
		mRootUrl = mRequest.getRequestURL().toString();
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
		mRequest.setAttribute("responseStatus", new ResponseMessage(type, message));
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
	private HttpServletRequest mRequest = null;
	private HttpServletResponse mResponse = null;
	private PrintWriter mOut = null;
	/** Logger */
	protected Logger mLogger = Logger.getLogger(getClass().getSimpleName());
}
