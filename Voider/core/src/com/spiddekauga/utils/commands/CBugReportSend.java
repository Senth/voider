package com.spiddekauga.utils.commands;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.utils.scene.ui.MsgBox;
import com.spiddekauga.utils.scene.ui.ProgressBar;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.misc.BugReportEntity;
import com.spiddekauga.voider.network.misc.BugReportEntity.BugReportTypes;
import com.spiddekauga.voider.network.misc.BugReportResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.analytics.AnalyticsRepo;
import com.spiddekauga.voider.repo.misc.BugReportWebRepo;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.resources.BugReportDef;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.spiddekauga.voider.settings.SettingRepo;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

import java.util.Date;

/**
 * Sends a bug report to the server
 */
public class CBugReportSend extends Command implements IResponseListener {
private boolean mSendAnonymously = false;
private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
private Exception mException;
private BugReportEntity mBugReport = null;
private IEventListener mLoginListener = new IEventListener() {
	@Override
	public void handleEvent(GameEvent event) {
		switch (event.type) {
		case USER_CONNECTED:
			sendBugReport();
			break;

		case USER_LOGIN_FAILED:
			saveBugReportLocally();
			break;

		default:
			break;
		}

		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.disconnect(EventTypes.USER_CONNECTED, mLoginListener);
		eventDispatcher.disconnect(EventTypes.USER_LOGIN_FAILED, mLoginListener);
	}
};

/**
 * Creates a command that will send a bug report.
 * @param exception the exception that was thrown
 */
public CBugReportSend(Exception exception) {
	mException = exception;
	createBugReportEntity();
}

/**
 * Creates the bug report entity
 */
private void createBugReportEntity() {
	mBugReport = new BugReportEntity();

	String userKey = User.getGlobalUser().getServerKey();

	// Create network entity
	if (!mSendAnonymously) {
		mBugReport.userKey = userKey;
	}
	mBugReport.date = new Date();
	mBugReport.subject = "";
	mBugReport.description = "";
	setSystemInformation(mBugReport);

	// Exception
	if (mException != null) {
		mBugReport.exception = Strings.exceptionToString(mException);
		mBugReport.type = BugReportTypes.BUG_EXCEPTION;

		// Get analytics
		mBugReport.analyticsSession = AnalyticsRepo.getInstance().getSession();
	} else {
		mBugReport.type = BugReportTypes.BUG_CUSTOM;
	}
}

/**
 * Get the system information that is sent to the server
 * @return system information
 */
public static String getSystemInformation() {
	String info = "OS: ";

	switch (Gdx.app.getType()) {
	case Android:
		info += "Android API v." + Gdx.app.getVersion();
		break;

	case Applet:
		info += "Applet";
		break;

	case Desktop:
		info += System.getProperty("os.name");
		break;

	case WebGL:
		info += "WebGL";
		break;

	case iOS:
		info += "iOS v." + Gdx.app.getVersion();
		break;

	default:
		break;
	}


	info += "\nGame Version: " + SettingRepo.getInstance().info().getCurrentVersion().getVersion();
	info += "\nBuild type: " + Config.Debug.BUILD.toString();
	info += "\nScreen resolution: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight();

	return info;
}

/**
 * Set the system information of the bug report
 * @param bugReport the bug report to set
 */
private static void setSystemInformation(BugReportEntity bugReport) {
	switch (Gdx.app.getType()) {
	case Android:
		bugReport.os = "Android API v." + Gdx.app.getVersion();
		break;

	case Applet:
		bugReport.os = "Applet";
		break;

	case Desktop:
		bugReport.os = System.getProperty("os.name");
		break;

	case WebGL:
		bugReport.os = "WebGL";
		break;

	case iOS:
		bugReport.os = "iOS v." + Gdx.app.getVersion();
		break;

	default:
		break;
	}

	bugReport.resolution = "" + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight();
	bugReport.gameVersion = SettingRepo.getInstance().info().getCurrentVersion().getVersion();
	bugReport.buildType = Config.Debug.BUILD.toString();
}

@Override
public void handleWebResponse(IMethodEntity method, IEntity response) {
	// Bug report
	if (response instanceof BugReportResponse) {
		handleBugReportResponse((BugReportResponse) response);
	}
}

/**
 * Handles the response from a bug report request
 * @param response server's method response
 */
private void handleBugReportResponse(BugReportResponse response) {
	ProgressBar.hide();
	if (response.status.isSuccessful()) {
		showSentMessage("Success", Messages.Info.BUG_REPORT_SENT);
	} else {
		saveBugReportLocally();
		showSentMessage("Failed to Send Bug Report!", Messages.Info.BUG_REPORT_SAVED_LOCALLY);
	}
}

/**
 * Show message box for sent report
 * @param title message box title
 * @param message the message of the sent report
 */
private void showSentMessage(String title, String message) {
	// Message box
	MsgBox msgBox = UiFactory.getInstance().msgBox.add(title);
	msgBox.content(message);
	msgBox.addCancelButtonAndKeys("Continue");
}

/**
 * Save the bug report locally
 */
private void saveBugReportLocally() {
	mResourceRepo.save(new BugReportDef(mBugReport));
}

@Override
public boolean execute() {
	ProgressBar.showSpinner("");
	if (User.getGlobalUser().isOnline()) {
		sendBugReport();
	} else {
		goOnline();
	}

	return true;
}

/**
 * Tries to send the bug report online
 */
private void sendBugReport() {
	ProgressBar.showSpinner("Sending bug report");
	BugReportWebRepo.getInstance().sendBugReport(mBugReport, this);
}

/**
 * Try to go online
 */
private void goOnline() {
	EventDispatcher eventDispatcher = EventDispatcher.getInstance();
	eventDispatcher.connect(EventTypes.USER_CONNECTED, mLoginListener);
	eventDispatcher.connect(EventTypes.USER_LOGIN_FAILED, mLoginListener);

	ProgressBar.showSpinner("Going online");
	User user = User.getGlobalUser();
	user.login();
}

@Override
public boolean undo() {
	// Does nothing
	return true;
}

/**
 * Sets if the bug report should be sent anonymously or not
 * @param anonymously true to send it anonymously
 */
public void setSendAnonymously(boolean anonymously) {
	mSendAnonymously = anonymously;
}

/**
 * Set the subject of the bug report
 * @param subject email subject
 */
public void setSubject(String subject) {
	mBugReport.subject = subject;
}

/**
 * Set the description of the bug report
 * @param description optional description of the bug report
 */
public void setDescription(String description) {
	mBugReport.description = description;
}

/**
 * Set the bug type
 * @param bugReportType type of bug report
 */
public void setType(BugReportTypes bugReportType) {
	mBugReport.type = bugReportType;
}
}
