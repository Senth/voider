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
import com.spiddekauga.voider.network.entities.BugReportEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.BugReportMethod;
import com.spiddekauga.voider.network.entities.method.BugReportMethodResponse;
import com.spiddekauga.voider.network.entities.method.BugReportMethodResponse.Statuses;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
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
		// Does nothing
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		BugReportMethodResponse methodResponse = new BugReportMethodResponse();
		methodResponse.status = Statuses.FAILED_SERVER_ERROR;

		if (!mUser.isLoggedIn()) {
			methodResponse.status = Statuses.FAILED_USER_NOT_LOGGED_IN;
			return methodResponse;
		}

		if (methodEntity instanceof BugReportMethod) {
			methodResponse.status = Statuses.SUCCESS;

			for (BugReportEntity bugReportEntity : ((BugReportMethod) methodEntity).bugs) {
				boolean success = sendBugReport(bugReportEntity);

				if (!success) {
					methodResponse.status = Statuses.SUCCESS_WITH_ERRORS;
					methodResponse.failedBugReports.add(bugReportEntity.id);
				}
			}
		}

		return methodResponse;
	}

	/**
	 * Sends a bug report
	 * @param bugReportEntity entity to the bug report
	 * @return true if the report was sent successfully
	 */
	private boolean sendBugReport(BugReportEntity bugReportEntity) {
		Entity user = getUser(bugReportEntity.userKey);

		if (user == null) {
			return false;
		}


		// Create body
		String body = "";
		body += getFormatedHeadline("Last action", bugReportEntity.lastAction);
		body += getFormatedHeadline("Second last action", bugReportEntity.secondLastAction);
		body += getFormatedHeadline("Description", bugReportEntity.description);
		body += getFormatedHeadline("OS", bugReportEntity.systemInformation);
		body += getFormatedHeadline("Exception", bugReportEntity.exception);


		// Send email
		Properties properties = new Properties();
		Session session = Session.getDefaultInstance(properties);
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(ServerConfig.EMAIL_ADMIN, (String) user.getProperty("username")));
			Address[] replyToAddresses = new Address[1];
			replyToAddresses[0] = new InternetAddress((String) user.getProperty("email"), (String) user.getProperty("username"));
			message.setReplyTo(replyToAddresses);
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(ServerConfig.EMAIL_ADMIN));
			message.setSubject("[BUG] " + bugReportEntity.subject);
			message.setContent(body, "text/html");
			Transport.send(message);
		} catch (MessagingException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}

		return true;
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


	/** Cached users */
	HashMap<String, Entity> mUsersCached = new HashMap<>();
}
