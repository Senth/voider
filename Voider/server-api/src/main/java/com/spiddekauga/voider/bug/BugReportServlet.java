package com.spiddekauga.voider.bug;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.voider.network.analytics.AnalyticsEventEntity;
import com.spiddekauga.voider.network.analytics.AnalyticsSceneEntity;
import com.spiddekauga.voider.network.analytics.AnalyticsSessionEntity;
import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.misc.BugReportEntity;
import com.spiddekauga.voider.network.misc.BugReportMethod;
import com.spiddekauga.voider.network.misc.BugReportResponse;
import com.spiddekauga.voider.server.util.ServerConfig;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

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

/**
 * Takes bug reports and reports these
 */
@SuppressWarnings("serial")
public class BugReportServlet extends VoiderApiServlet<BugReportMethod> {
private HashMap<String, Entity> mUsersCached = new HashMap<>();
private BugReportResponse mResponse = null;

@Override
protected void onInit() throws ServletException, IOException {
	super.onInit();
	mResponse = new BugReportResponse();
	mResponse.status = GeneralResponseStatuses.FAILED_SERVER_ERROR;
}

@Override
protected IEntity onRequest(BugReportMethod method) throws ServletException, IOException {

	if (!mUser.isLoggedIn()) {
		mResponse.status = GeneralResponseStatuses.FAILED_USER_NOT_LOGGED_IN;
		return mResponse;
	}

	mResponse.status = GeneralResponseStatuses.SUCCESS;

	for (BugReportEntity bugReportEntity : method.bugs) {
		boolean success = sendBugReport(bugReportEntity);

		if (!success) {
			mResponse.status = GeneralResponseStatuses.SUCCESS_PARTIAL;
			mResponse.failedBugReports.add(bugReportEntity.id);
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
	body += getFormatedHeadline("Description", bugReportEntity.description);
	body += getFormatedHeadline("Exception", Strings.toHtmlString(bugReportEntity.exception));
	body += getSystemInformationHtml(bugReportEntity);

	if (bugReportEntity.type != BugReportEntity.BugReportTypes.FEATURE) {
		body += getFormattedGitlabIssue(bugReportEntity);
	}


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
		message.setSubject(getSubject(bugReportEntity, sentFromName));
		message.setContent(body, "text/html");
		Transport.send(message);
	} catch (MessagingException | UnsupportedEncodingException e) {
		e.printStackTrace();
		return false;
	}

	return true;
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

/**
 * Get a formatted string containing for a headline
 * @param headline this text will be bold if the message isn't empty
 * @param message message from the user
 * @return formatted headline with content, empty if message is empty.
 */
private static String getFormatedHeadline(String headline, String message) {
	if (message != null && !message.equals("")) {
		return "<h2>" + headline + "</h2><br />" + message + "<br /><br />";
	} else {
		return "";
	}
}

private static String getSystemInformationHtml(BugReportEntity bugReportEntity) {
	// Skip system information for feature requests
	if (bugReportEntity.type == BugReportEntity.BugReportTypes.FEATURE) {
		return "";
	}

	String systemInformation = "<b>OS: </b>" + bugReportEntity.os + "<br />"
			+ "<b>Game Version: </b>" + bugReportEntity.gameVersion + "<br />"
			+ "<b>Build Type: </b>" + bugReportEntity.buildType + "<br />"
			+ "<b>Screen Size: </b>" + bugReportEntity.resolution + "<br />";

	return getFormatedHeadline("System Information", systemInformation);
}

private static String getFormattedGitlabIssue(BugReportEntity bugReportEntity) {
	String export = "";

	if (bugReportEntity.description != null && !bugReportEntity.description.isEmpty()) {
		export += bugReportEntity.description + "<br />\n<br />\n";
	}
	export += getSystemInformationGitlab(bugReportEntity);
	export += getExceptionGitlab(bugReportEntity.exception);
	export += getAnalyticsGitlab(bugReportEntity.analyticsSession);

	return getFormatedHeadline("GITLAB ISSUE", export);
}

/**
 * Get bug report subject from the bug report entity
 * @param bugReportEntity bug report sent from the user
 * @param username the user that sent the bug report
 * @return subject for the bug report
 */
private static String getSubject(BugReportEntity bugReportEntity, String username) {
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

	return subject + " " + bugReportEntity.subject + " - " + username;
}

private static String getSystemInformationGitlab(BugReportEntity bugReportEntity) {
	// Skip system information for feature requests
	if (bugReportEntity.type == BugReportEntity.BugReportTypes.FEATURE) {
		return "";
	}

	return "# System Information<br />"
			+ "**OS:** " + bugReportEntity.os + "<br /><br />"
			+ "**Game Version:** " + bugReportEntity.gameVersion + "<br /><br />"
			+ "**Build Type:** " + bugReportEntity.buildType + "<br /><br />"
			+ "**Screen Size:** " + bugReportEntity.resolution + "<br /><br />";
}

private static String getExceptionGitlab(String exception) {
	if (exception != null && !exception.isEmpty()) {
		return "# Exception<br />"
				+ "```java<br />"
				+ Strings.toHtmlString(exception)
				+ "```<br /><br />";
	} else {
		return "";
	}
}

private static String getAnalyticsGitlab(AnalyticsSessionEntity session) {
	if (session != null && !session.scenes.isEmpty()) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("# AnalyticsServlet<br/>");

		appendLastActionsGitlab(session.scenes.get(session.scenes.size() - 1), stringBuilder);
		newline(stringBuilder);

		stringBuilder.append("## Scenes<br />");
		for (AnalyticsSceneEntity scene : session.scenes) {
			appendSceneGitlab(scene, stringBuilder);
		}

		newline(stringBuilder, 2);

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
private static void appendLastActionsGitlab(AnalyticsSceneEntity scene, StringBuilder stringBuilder) {
	int startIndex = scene.events.size() - 1;
	int endIndex = startIndex - 9;

	if (endIndex < 0) {
		endIndex = 0;
	}

	stringBuilder.append("## Last 10 actions in `")
			.append(scene.name)
			.append("`<br />");

	tableHeaderGitlab(stringBuilder);

	for (int i = startIndex; i >= endIndex; --i) {
		appendEventGitlab(scene, scene.events.get(i), stringBuilder);
	}
}

/**
 * Append a newline
 */
private static void newline(StringBuilder stringBuilder) {
	newline(stringBuilder, 1);
}

/**
 * Append scene with events information
 * @param scene the scene to append
 * @param stringBuilder where to append the scene with events
 */
private static void appendSceneGitlab(AnalyticsSceneEntity scene, StringBuilder stringBuilder) {
	stringBuilder.append("### `")
			.append(scene.name)
			.append("` events<br />");
	tableHeaderGitlab(stringBuilder);

	for (AnalyticsEventEntity event : scene.events) {
		appendEventGitlab(scene, event, stringBuilder);
	}

	newline(stringBuilder);
}

/**
 * Append several newlines
 * @param count number of newlines
 */
private static void newline(StringBuilder stringBuilder, int count) {
	for (int i = 0; i < count; ++i) {
		stringBuilder.append("<br />");
	}
}

/**
 * Append all table headers to the string
 * @param stringBuilder append table headers to this string builder
 */
private static void tableHeaderGitlab(StringBuilder stringBuilder) {
	stringBuilder
			.append("| **Scene** | **Event** | **Time** | **Data** |<br />")
			.append("| :-------- | :-------- | -------: | :------- |<br />");
}

/**
 * Append event information
 * @param scene scene information
 * @param event the event to append
 * @param stringBuilder where to append the event
 */
private static void appendEventGitlab(AnalyticsSceneEntity scene, AnalyticsEventEntity event, StringBuilder stringBuilder) {
	stringBuilder.append("| ")
			.append(scene.name).append(" | ")
			.append(event.name).append(" | ");

	// Event time (as seconds since scene started)
	long diffTimeMili = event.time.getTime() - scene.startTime.getTime();
	double diffTime = diffTimeMili * 0.001;
	stringBuilder.append(String.format("%.2f", diffTime)).append(" | ")
			.append(event.data).append(" |<br />");
}
}
