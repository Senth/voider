package com.spiddekauga.utils.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.misc.BugReportEntity;
import com.spiddekauga.voider.network.misc.BugReportEntity.BugReportTypes;
import com.spiddekauga.voider.network.misc.BugReportResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.analytics.AnalyticsRepo;
import com.spiddekauga.voider.repo.misc.BugReportWebRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.resources.BugReportDef;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.ui.UiFactory;
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
private boolean mEndScene;
private boolean mSendAnonymously = false;
private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
private Exception mException;
private BugReportEntity mBugReport = null;
private Gui mGui;
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
 * @param gui the GUI to show progress and success on
 * @param exception the exception that was thrown
 * @param endScene TODO
 */
public CBugReportSend(Gui gui, Exception exception, boolean endScene) {
	mGui = gui;
	mException = exception;
	mEndScene = endScene;
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
	mBugReport.systemInformation = getSystemInformation();

	// Exception
	if (mException != null) {
		String stackTrace = Strings.exceptionToString(mException);
		stackTrace = Strings.toHtmlString(stackTrace);
		mBugReport.additionalInformation = stackTrace;
		mBugReport.additionalInformation += "</br></br>";
		mBugReport.type = BugReportTypes.BUG_EXCEPTION;

		// Get analytics
		mBugReport.analyticsSession = AnalyticsRepo.getInstance().getSession();
	}
}

/**
 * @return string with system information
 */
public static String getSystemInformation() {
	StringBuilder builder = new StringBuilder();

	builder.append("OS: ");
	switch (Gdx.app.getType()) {
	case Android:
		builder.append("Android API v.").append(Gdx.app.getVersion());
		break;

	case Applet:
		builder.append("Applet");
		break;

	case Desktop:
		builder.append(System.getProperty("os.name"));
		break;

	case WebGL:
		builder.append("WebGL");
		break;

	case iOS:
		builder.append("iOS v.").append(Gdx.app.getVersion());
		break;

	default:
		break;
	}

	builder.append("\n");
	builder.append("Screen size: ").append(Gdx.graphics.getWidth()).append("x").append(Gdx.graphics.getHeight()).append("\n");
	builder.append("Version: ").append(SettingRepo.getInstance().info().getCurrentVersion().getVersion()).append("\n");
	builder.append("Build: ").append(Config.Debug.BUILD.toString());

	return builder.toString();
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
	mGui.hideWaitWindow();
	if (response.status.isSuccessful()) {
		showSentMessage("Success", Messages.Info.BUG_REPORT_SENT);
	} else {
		saveBugReportLocally();
		showSentMessage("Failed to Send Bug Report!", Messages.Info.BUG_REPORT_SAVED_LOCALLY);
	}
}

/**
 * Show message box for sent report
 * @param title
 * @param message
 */
private void showSentMessage(String title, String message) {
	// Message box
	MsgBoxExecuter msgBox = UiFactory.getInstance().msgBox.add(title);
	msgBox.content(message);

	if (mEndScene) {
		Command command = new CSceneEnd();
		msgBox.button("OK", command);
		msgBox.key(Input.Keys.ESCAPE, command);
		msgBox.key(Input.Keys.ENTER, command);
		msgBox.key(Input.Keys.BACK, command);
	} else {
		msgBox.addCancelButtonAndKeys("Continue");
	}
}

/**
 * Save the bug report locally
 */
private void saveBugReportLocally() {
	mResourceRepo.save(new BugReportDef(mBugReport));
}

@Override
public boolean execute() {
	mGui.showWaitWindow("");
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
	mGui.setWaitWindowText("Sending bug report");
	BugReportWebRepo.getInstance().sendBugReport(mBugReport, this);
}

/**
 * Try to go online
 */
private void goOnline() {
	EventDispatcher eventDispatcher = EventDispatcher.getInstance();
	eventDispatcher.connect(EventTypes.USER_CONNECTED, mLoginListener);
	eventDispatcher.connect(EventTypes.USER_LOGIN_FAILED, mLoginListener);

	mGui.setWaitWindowText("Going online");
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
 * @param subject
 */
public void setSubject(String subject) {
	mBugReport.subject = subject;
}

/**
 * Set the description of the bug report
 * @param description
 */
public void setDescription(String description) {
	mBugReport.description = description;
}

/**
 * Set the bug type
 * @param bugReportType
 */
public void setType(BugReportTypes bugReportType) {
	mBugReport.type = bugReportType;
}
}
