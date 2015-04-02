package com.spiddekauga.voider.servlets.api;

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
import com.spiddekauga.voider.network.analytics.AnalyticsEventEntity;
import com.spiddekauga.voider.network.analytics.AnalyticsSceneEntity;
import com.spiddekauga.voider.network.analytics.AnalyticsSessionEntity;
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
		body += getFormattedAnalyticsHtml(bugReportEntity.analyticsSession);
		body += getFormattedTrac(bugReportEntity);


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
	private static String getSubject(BugReportEntity bugReportEntity) {
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
	private static String getFormatedHeadline(String headline, String message) {
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


	// ------------------------------------
	// Analytics HTML
	// ------------------------------------
	/**
	 * Get analytics information as HTML
	 * @param session analytics session, if null this method does nothing
	 * @return formatted string for analytics information, empty string if session is null
	 *         or empty
	 */
	private static String getFormattedAnalyticsHtml(AnalyticsSessionEntity session) {
		if (session != null && !session.scenes.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder();
			appendLastActionsHtml(session.scenes.get(session.scenes.size() - 1), stringBuilder);
			stringBuilder.append("</br></br>\n\n");

			stringBuilder.append("<h3>Scenes</h3>");
			for (AnalyticsSceneEntity scene : session.scenes) {
				appendSceneHtml(scene, stringBuilder);
			}

			return getFormatedHeadline("Analytics", stringBuilder.toString());
		} else {
			return "";
		}
	}

	/**
	 * Appends the 10 (or less) latest actions in the current scene
	 * @param scene latest scene
	 * @param stringBuilder where to append latest action
	 */
	private static void appendLastActionsHtml(AnalyticsSceneEntity scene, StringBuilder stringBuilder) {
		int startIndex = scene.events.size() - 1;
		int endIndex = startIndex - 9;

		if (endIndex < 0) {
			endIndex = 0;
		}

		stringBuilder.append("<h3>Last 10 actions in <b>" + scene.name + "</b> scene</h3>");
		tableHeaderHtml(stringBuilder);

		for (int i = startIndex; i >= endIndex; --i) {
			appendEventHtml(scene, scene.events.get(i), stringBuilder);
		}
	}


	/**
	 * Append scene with events information
	 * @param scene the scene to append
	 * @param stringBuilder where to append the scene with events
	 */
	private static void appendSceneHtml(AnalyticsSceneEntity scene, StringBuilder stringBuilder) {
		stringBuilder.append("<h4>" + scene.name + " events</h4>\n");
		stringBuilder.append("<table>\n");
		tableHeaderHtml(stringBuilder);

		for (AnalyticsEventEntity event : scene.events) {
			appendEventHtml(scene, event, stringBuilder);
		}

		stringBuilder.append("</table>\n");
	}

	/**
	 * Append event information
	 * @param scene scene information
	 * @param event the event to append
	 * @param stringBuilder where to append the event
	 */
	private static void appendEventHtml(AnalyticsSceneEntity scene, AnalyticsEventEntity event, StringBuilder stringBuilder) {
		tableRowStartHtml(stringBuilder);

		// Scene name
		tableColumnHtml(scene.name, stringBuilder);

		// Event name
		tableColumnHtml(event.name, stringBuilder);

		// Event time (as seconds since scene started)
		long diffTimeMili = event.time.getTime() - scene.startTime.getTime();
		double diffTime = diffTimeMili * 0.001;
		tableColumnHtml(String.format("%.2f", diffTime), stringBuilder);

		// Event data
		tableColumnHtml(event.data, stringBuilder);

		tableRowEndHtml(stringBuilder);
	}

	/**
	 * Appends a new table header
	 * @param header
	 * @param stringBuilder
	 */
	private static void tableHeaderHtml(String header, StringBuilder stringBuilder) {
		stringBuilder.append("<th style=\"border: 1px solid black;\">");
		stringBuilder.append(header);
		stringBuilder.append("</th>\n");
	}

	/**
	 * Append all table headers to the table
	 * @param stringBuilder
	 */
	private static void tableHeaderHtml(StringBuilder stringBuilder) {
		tableRowStartHtml(stringBuilder);
		tableHeaderHtml("Scene", stringBuilder);
		tableHeaderHtml("Event", stringBuilder);
		tableHeaderHtml("Time", stringBuilder);
		tableHeaderHtml("Data", stringBuilder);
		tableRowEndHtml(stringBuilder);
	}

	/**
	 * Appends a new table column
	 * @param value text in the column
	 * @param stringBuilder
	 */
	private static void tableColumnHtml(String value, StringBuilder stringBuilder) {
		stringBuilder.append("<td style=\"border: 1px solid black;\">");
		stringBuilder.append(value);
		stringBuilder.append("</td>\n");
	}

	/**
	 * Start table row
	 * @param stringBuilder
	 */
	private static void tableRowStartHtml(StringBuilder stringBuilder) {
		stringBuilder.append("<tr>\n");
	}

	/**
	 * End table row
	 * @param stringBuilder
	 */
	private static void tableRowEndHtml(StringBuilder stringBuilder) {
		stringBuilder.append("</tr>\n");
	}

	// ------------------------------------
	// Trac
	// ------------------------------------
	/**
	 * Get trac export
	 * @param bugReportEntity
	 * @return formatted Trac export
	 */
	private static String getFormattedTrac(BugReportEntity bugReportEntity) {
		String export = "";

		export += bugReportEntity.description + "<br />\n<br />\n";
		export += getSystemInformationTrac(bugReportEntity.systemInformation);
		export += getExceptionTrac(bugReportEntity.additionalInformation);
		export += getFormattedAnalyticsTrac(bugReportEntity.analyticsSession);

		return getFormatedHeadline("TRAC", export);
	}

	/**
	 * Get system information for trac
	 * @param systemInformation
	 * @return formatted Trac Export
	 */
	private static String getSystemInformationTrac(String systemInformation) {
		if (systemInformation != null) {
			StringBuilder stringBuilder = new StringBuilder();
			appendTracHeaderBig("System Information", stringBuilder);
			stringBuilder.append(systemInformation);
			newline(stringBuilder, 2);
			return stringBuilder.toString();
		}
		return "";
	}

	/**
	 * Append a big trac headline
	 * @param header
	 * @param stringBuilder
	 */
	private static void appendTracHeaderBig(String header, StringBuilder stringBuilder) {
		stringBuilder.append("== ").append(header).append(" ==");
		newline(stringBuilder);
	}

	/**
	 * Append a small trac headline
	 * @param header
	 * @param stringBuilder
	 */
	private static void appendTracHeaderSmall(String header, StringBuilder stringBuilder) {
		stringBuilder.append("==== ").append(header).append(" ====");
		newline(stringBuilder);
	}


	/**
	 * Get formatted trac exception
	 * @param exception
	 * @return formatted exception if one exists
	 */
	private static String getExceptionTrac(String exception) {
		if (exception != null) {
			StringBuilder stringBuilder = new StringBuilder();
			appendTracHeaderBig("Exception", stringBuilder);
			stringBuilder.append("{{{");
			newline(stringBuilder);
			stringBuilder.append(exception);
			newline(stringBuilder);
			stringBuilder.append("}}}");
			newline(stringBuilder);
			return stringBuilder.toString();
		}
		return "";
	}

	/**
	 * Get analytics information as Trac
	 * @param session analytics session, if null this method does nothing
	 * @return formatted string for analytics information, empty string if session is null
	 *         or empty
	 */
	private static String getFormattedAnalyticsTrac(AnalyticsSessionEntity session) {
		if (session != null && !session.scenes.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder();
			appendTracHeaderBig("Analytics", stringBuilder);
			appendLastActionsTrac(session.scenes.get(session.scenes.size() - 1), stringBuilder);
			newline(stringBuilder, 2);

			appendTracHeaderSmall("Scenes", stringBuilder);
			for (AnalyticsSceneEntity scene : session.scenes) {
				appendSceneTrac(scene, stringBuilder);
			}

			newline(stringBuilder);

			return stringBuilder.toString();
		} else {
			return "";
		}
	}

	/**
	 * Appends the 10 (or less) latest actions in the current scene
	 * @param scene latest scene
	 * @param stringBuilder where to append latest action
	 */
	private static void appendLastActionsTrac(AnalyticsSceneEntity scene, StringBuilder stringBuilder) {
		int startIndex = scene.events.size() - 1;
		int endIndex = startIndex - 9;

		if (endIndex < 0) {
			endIndex = 0;
		}

		appendTracHeaderSmall("Last 10 actions in `" + scene.name + "` scene", stringBuilder);
		tableHeaderTrac(stringBuilder);

		for (int i = startIndex; i >= endIndex; --i) {
			appendEventTrac(scene, scene.events.get(i), stringBuilder);
		}
	}


	/**
	 * Append scene with events information
	 * @param scene the scene to append
	 * @param stringBuilder where to append the scene with events
	 */
	private static void appendSceneTrac(AnalyticsSceneEntity scene, StringBuilder stringBuilder) {
		appendTracHeaderSmall(scene.name + " events", stringBuilder);
		tableHeaderTrac(stringBuilder);

		for (AnalyticsEventEntity event : scene.events) {
			appendEventTrac(scene, event, stringBuilder);
		}

		newline(stringBuilder);
	}

	/**
	 * Append event information
	 * @param scene scene information
	 * @param event the event to append
	 * @param stringBuilder where to append the event
	 */
	private static void appendEventTrac(AnalyticsSceneEntity scene, AnalyticsEventEntity event, StringBuilder stringBuilder) {
		tableRowStartTrac(stringBuilder);

		// Scene name
		tableColumnTrac(scene.name, stringBuilder);

		// Event name
		tableColumnTrac(event.name, stringBuilder);

		// Event time (as seconds since scene started)
		long diffTimeMili = event.time.getTime() - scene.startTime.getTime();
		double diffTime = diffTimeMili * 0.001;
		tableColumnTrac(String.format("%.2f", diffTime), stringBuilder);

		// Event data
		tableColumnTrac(event.data, stringBuilder);

		tableRowEndTrac(stringBuilder);
	}

	/**
	 * Appends a new table header
	 * @param header
	 * @param stringBuilder
	 */
	private static void tableHeaderTrac(String header, StringBuilder stringBuilder) {
		stringBuilder.append("**");
		stringBuilder.append(header);
		stringBuilder.append("**||");
	}

	/**
	 * Append all table headers to the table
	 * @param stringBuilder
	 */
	private static void tableHeaderTrac(StringBuilder stringBuilder) {
		tableRowStartTrac(stringBuilder);
		tableHeaderTrac("Scene", stringBuilder);
		tableHeaderTrac("Event", stringBuilder);
		tableHeaderTrac("Time", stringBuilder);
		tableHeaderTrac("Data", stringBuilder);
		tableRowEndTrac(stringBuilder);
	}

	/**
	 * Appends a new table column
	 * @param value text in the column
	 * @param stringBuilder
	 */
	private static void tableColumnTrac(String value, StringBuilder stringBuilder) {
		stringBuilder.append(value);
		stringBuilder.append("||");
	}

	/**
	 * Start table row
	 * @param stringBuilder
	 */
	private static void tableRowStartTrac(StringBuilder stringBuilder) {
		stringBuilder.append("||");
	}

	/**
	 * End table row
	 * @param stringBuilder
	 */
	private static void tableRowEndTrac(StringBuilder stringBuilder) {
		newline(stringBuilder);
	}

	/**
	 * Append a newline
	 * @param stringBuilder
	 */
	private static void newline(StringBuilder stringBuilder) {
		stringBuilder.append("<br />\n");
	}

	/**
	 * Append several newlines
	 * @param stringBuilder
	 * @param count
	 */
	private static void newline(StringBuilder stringBuilder, int count) {
		for (int i = 0; i < count; ++i) {
			stringBuilder.append("<br />\n");
		}
	}

	private HashMap<String, Entity> mUsersCached = new HashMap<>();
	private BugReportResponse mResponse = null;
}
