package com.spiddekauga.utils.commands;

import java.util.Date;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.misc.BugReportEntity;
import com.spiddekauga.voider.network.misc.BugReportEntity.BugReportTypes;
import com.spiddekauga.voider.network.misc.BugReportResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.analytics.AnalyticsRepo;
import com.spiddekauga.voider.repo.misc.BugReportWebRepo;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.resources.BugReportDef;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.ui.UiFactory;
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
	 * @param exception the exception that was thrown
	 */
	public CBugReportSend(Gui gui, Exception exception) {
		mGui = gui;
		mException = exception;
		createBugReportEntity();
	}

	/**
	 * Creates a command that will send a bug report.
	 * @param gui GUI to show progress and success on
	 */
	public CBugReportSend(Gui gui) {
		this(gui, null);
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

		// Screen size
		systemInformation += "\n";
		systemInformation += "Screen size: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight();

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
			MsgBoxExecuter msgBox = UiFactory.getInstance().msgBox.add("Success");
			msgBox.content(Messages.Info.BUG_REPORT_SENT);

			// Exception, quit the game
			if (mBugReport.type == BugReportTypes.BUG_EXCEPTION) {
				Command quitGame = new CGameQuit();
				msgBox.button("Quit game", quitGame);
				msgBox.key(Input.Keys.ESCAPE, quitGame);
				msgBox.key(Input.Keys.ENTER, quitGame);
				msgBox.key(Input.Keys.BACK, quitGame);
			}
			// Continue with the game
			else {
				msgBox.addCancelButtonAndKeys("OK");
			}

			mGui.hideWaitWindow();
		} else {
			saveBugReportLocally();
		}
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
		MsgBoxExecuter msgBox = UiFactory.getInstance().msgBox.add("Failed To Send Bug Report");
		msgBox.content(Messages.Info.BUG_REPORT_SAVED_LOCALLY);
		if (mBugReport.type == BugReportTypes.BUG_EXCEPTION) {
			Command quitGame = new CGameQuit();
			msgBox.button("Quit game", quitGame);
			msgBox.key(Input.Keys.ESCAPE, quitGame);
			msgBox.key(Input.Keys.ENTER, quitGame);
			msgBox.key(Input.Keys.BACK, quitGame);
		} else {
			msgBox.addCancelButtonAndKeys("OK");
		}

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

	/** If the bug report should be sent anonymously */
	private boolean mSendAnonymously = false;
	/** Resource repository */
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
	/** The exception to send */
	private Exception mException;
	/** The network entity to send */
	private BugReportEntity mBugReport = null;
	/** GUI to show success / progress on */
	private Gui mGui;
}
