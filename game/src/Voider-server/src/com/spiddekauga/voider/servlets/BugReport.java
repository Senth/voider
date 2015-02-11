package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.misc.BugReportEntity;
import com.spiddekauga.voider.network.misc.BugReportMethod;
import com.spiddekauga.voider.network.misc.BugReportResponse;
import com.spiddekauga.voider.server.util.ServerConfig;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Takes bug reports and reports these
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BugReport extends VoiderServlet {
	@Override
	protected void onInit() {
		mResponse = new BugReportResponse();
		mResponse.status = GeneralResponseStatuses.FAILED_SERVER_ERROR;
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {

		if (!mUser.isLoggedIn()) {
			mResponse.status = GeneralResponseStatuses.FAILED_USER_NOT_LOGGED_IN;
			return mResponse;
		}

		if (methodEntity instanceof BugReportMethod) {
			BugReportMethod parameters = (BugReportMethod) methodEntity;
			mResponse.status = GeneralResponseStatuses.SUCCESS;

			for (BugReportEntity bugReportEntity : parameters.bugs) {
				boolean success = sendBugReport(bugReportEntity);

				if (!success) {
					mResponse.status = GeneralResponseStatuses.SUCCESS_PARTIAL;
					mResponse.failedBugReports.add(bugReportEntity.id);
				}
			}
		}

		return mResponse;
	}

	/**
	 * Sends a bug report
	 * @param bugReportEntity entity to the bug report
	 * @return true if the report was sent successfully
	 */
	private boolean sendBugReport(BugReportEntity bugReportEntity) {
		String sentFromEmail = "anonymous@voider-game.com";
		String sentFromName = "Anonymous";
		if (bugReportEntity.userKey != null) {
			Entity user = getUser(bugReportEntity.userKey);
			sentFromEmail = (String) user.getProperty("email");
			sentFromName = (String) user.getProperty("username");
		}

		// Create body
		String body = "";
		body += getFormatedHeadline("System Information", bugReportEntity.systemInformation);
		body += getFormatedHeadline("Description", bugReportEntity.description);
		body += getFormatedHeadline("Exception", bugReportEntity.additionalInformation);


		// Send email
		Properties properties = new Properties();
		Session session = Session.getDefaultInstance(properties);
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(ServerConfig.EMAIL_ADMIN);
			Address[] replyToAddresses = new Address[1];
			replyToAddresses[0] = new InternetAddress(sentFromEmail, sentFromName);
			message.setReplyTo(replyToAddresses);
			message.addRecipient(Message.RecipientType.TO, ServerConfig.EMAIL_ADMIN);
			message.setSubject(getSubject(bugReportEntity));
			message.setContent(body, "text/html");
			Transport.send(message);
		} catch (MessagingException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Get bug report subject
	 * @param bugReportEntity
	 * @return subject for the bug report
	 */
	private String getSubject(BugReportEntity bugReportEntity) {
		String subject = "";
		if (bugReportEntity.type != null) {
			switch (bugReportEntity.type) {
			case UNKNOWN:
				subject = "[BUG_UNKNOWN]";
				break;
			case BUG_CUSTOM:
				subject = "[BUG]";
				break;
			case BUG_EXCEPTION:
				subject = "[BUG_EXN]";
				break;
			case FEATURE:
				subject = "[FEATURE]";
				break;
			}
		}

		return subject + " " + bugReportEntity.subject;
	}

	/**
	 * Get a formatted string containing for a headline
	 * @param headline this text will be bold if the message isn't empty
	 * @param message message from the user
	 * @return formatted headline with content, empty if message is empty.
	 */
	private String getFormatedHeadline(String headline, String message) {
		if (message != null && !message.equals("")) {
			return "<b>" + headline + "</b><br />" + message + "<br /><br />";
		} else {
			return "";
		}
	}

	/**
	 * Get a user from the database or cache
	 * @param userKeyString the user key in string format
	 * @return user database entity, null if not found
	 */
	private Entity getUser(String userKeyString) {
		Entity user = mUsersCached.get(userKeyString);

		if (user == null) {
			Key userKey = KeyFactory.stringToKey(userKeyString);

			if (userKey != null) {
				user = DatastoreUtils.getEntity(userKey);

				if (user != null) {
					mUsersCached.put(userKeyString, user);
				}
			}
		}

		return user;
	}


	private HashMap<String, Entity> mUsersCached = new HashMap<>();
	private BugReportResponse mResponse = null;
}
