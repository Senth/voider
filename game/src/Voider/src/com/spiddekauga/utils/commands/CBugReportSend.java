package com.spiddekauga.utils.commands;

import java.util.Date;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.misc.BugReportEntity;
import com.spiddekauga.voider.network.misc.BugReportResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.analytics.AnalyticsRepo;
import com.spiddekauga.voider.repo.misc.BugReportWebRepo;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.resources.BugReportDef;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Sends a bug report to the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CBugReportSend extends Command implements IResponseListener {
	/**
	 * Creates a command that will send a bug report.
	 * @param gui the GUI to show progress and success on
	 * @param subject text for the subject
	 * @param description text for the description
	 * @param exception the exception that was thrown
	 */
	public CBugReportSend(Gui gui, TextFieldListener subject, TextFieldListener description, Exception exception) {
		mGui = gui;
		mSubject = subject;
		mDescription = description;
		mException = exception;
	}

	/**
	 * Creates a command that will send a bug report.
	 * @param gui GUI to show progress and success on
	 * @param subject text for the subject
	 * @param description text field for the description
	 */
	public CBugReportSend(Gui gui, TextFieldListener subject, TextFieldListener description) {
		this(gui, subject, description, null);
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
		mBugReport.subject = mSubject.getText();
		mBugReport.description = mDescription.getText();
		mBugReport.systemInformation = getSystemInformation();

		// Exception
		if (mException != null) {
			String stackTrace = Strings.exceptionToString(mException);
			stackTrace = Strings.toHtmlString(stackTrace);
			mBugReport.additionalInformation = stackTrace;
			mBugReport.additionalInformation += "</br></br>";
		}

		// Get analytics
		String analytics = AnalyticsRepo.getInstance().getSessionDebug();
		mBugReport.additionalInformation += analytics;
	}

	/**
	 * @return string with system information
	 */
	private static String getSystemInformation() {
		String systemInformation;

		switch (Gdx.app.getType()) {
		case Android:
			systemInformation = "Android API v." + Gdx.app.getVersion();
			break;

		case Applet:
			systemInformation = "Applet";
			break;

		case Desktop:
			systemInformation = System.getProperty("os.name");
			break;

		case WebGL:
			systemInformation = "WebGL";
			break;

		case iOS:
			systemInformation = "iOS v." + Gdx.app.getVersion();
			break;

		default:
			systemInformation = "";
			break;
		}

		return systemInformation;
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
		if (response.status.isSuccessful()) {
			// Message box
			MsgBoxExecuter msgBox = mGui.getFreeMsgBox(true);
			msgBox.setTitle("Success");
			msgBox.content(Messages.Info.BUG_REPORT_SENT);
			Command quitGame = new CGameQuit();
			msgBox.button("Quit game", quitGame);
			msgBox.key(Input.Keys.ESCAPE, quitGame);
			msgBox.key(Input.Keys.ENTER, quitGame);
			msgBox.key(Input.Keys.BACK, quitGame);
			mGui.showMsgBox(msgBox);

			mGui.hideWaitWindow();
		} else {
			saveBugReportLocally();
		}
	}

	@Override
	public boolean execute() {
		createBugReportEntity();

		mGui.showWaitWindow("");
		if (User.getGlobalUser().isOnline()) {
			sendBugReport();
		} else {
			goOnline();
		}

		return true;
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

	/**
	 * Save the bug report locally
	 */
	private void saveBugReportLocally() {
		mResourceRepo.save(new BugReportDef(mBugReport));

		// Message box
		MsgBoxExecuter msgBox = mGui.getFreeMsgBox(true);
		msgBox.setTitle("Failed");
		msgBox.content(Messages.Info.BUG_REPORT_SAVED_LOCALLY);
		Command quitGame = new CGameQuit();
		msgBox.button("Quit game", quitGame);
		msgBox.key(Input.Keys.ESCAPE, quitGame);
		msgBox.key(Input.Keys.ENTER, quitGame);
		msgBox.key(Input.Keys.BACK, quitGame);
		mGui.showMsgBox(msgBox);

		mGui.hideWaitWindow();
	}

	/**
	 * Tries to send the bug report online
	 */
	private void sendBugReport() {
		mGui.setWaitWindowText("Sending bug report");
		BugReportWebRepo.getInstance().sendBugReport(mBugReport, this);
	}

	@Override
	public boolean undo() {
		// Does nothing
		return true;
	}

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
	 * Sets if the bug report should be sent anonymously or not
	 * @param anonymously true to send it anonymously
	 */
	public void setSendAnonymously(boolean anonymously) {
		mSendAnonymously = anonymously;
	}

	/** If the bug report should be sent anonymously */
	private boolean mSendAnonymously = false;
	/** Resource repository */
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
	/** subject of the bug report */
	private TextFieldListener mSubject;
	/** Optional description of the bug */
	private TextFieldListener mDescription;
	/** The exception to send */
	private Exception mException;
	/** The network entity to send */
	private BugReportEntity mBugReport = null;
	/** GUI to show success / progress on */
	private Gui mGui;
}
