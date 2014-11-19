package com.spiddekauga.utils.commands;

import java.util.Date;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.misc.BugReportEntity;
import com.spiddekauga.voider.network.entities.misc.BugReportMethodResponse;
import com.spiddekauga.voider.repo.IResponseListener;
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
	 * @param lastAction text for the last action before the bug occurred
	 * @param secondLastAction text for the second last action
	 * @param description text for the description
	 * @param exception the exception that was thrown
	 */
	public CBugReportSend(Gui gui, TextField subject, TextField lastAction, TextField secondLastAction, TextField description, Exception exception) {
		mGui = gui;
		mSubject = subject;
		mLastAction = lastAction;
		mSecondLastAction = secondLastAction;
		mDescription = description;
		mException = exception;
	}

	/**
	 * Creates a command that will send a bug report.
	 * @param gui GUI to show progress and success on
	 * @param subject text for the subject
	 * @param lastAction text field for the last action before the bug occurred
	 * @param secondLastAction text field for the second last action
	 * @param description text field for the description
	 */
	public CBugReportSend(Gui gui, TextField subject, TextField lastAction, TextField secondLastAction, TextField description) {
		this(gui, subject, lastAction, secondLastAction, description, null);
	}

	/**
	 * Creates the bug report entity
	 */
	private void createBugReportEntity() {
		mBugReport = new BugReportEntity();

		String userKey = User.getGlobalUser().getServerKey();

		// Create network entity
		mBugReport.userKey = userKey;
		mBugReport.date = new Date();
		mBugReport.subject = mSubject.getText();
		mBugReport.lastAction = mLastAction.getText();
		mBugReport.secondLastAction = mSecondLastAction.getText();
		mBugReport.description = mDescription.getText();
		mBugReport.systemInformation = getSystemInformation();

		if (mException != null) {
			String stackTrace = Strings.exceptionToString(mException);
			stackTrace = Strings.toHtmlString(stackTrace);
			mBugReport.exception = stackTrace;
		}
	}

	/**
	 * @return string with system information
	 */
	private static String getSystemInformation() {
		String systemInformation;

		switch (Gdx.app.getType()) {
		case Android:
			systemInformation = "Android v." + Gdx.app.getVersion();
			break;

		case Applet:
			systemInformation = "Applet";
			break;

		case Desktop:
			systemInformation = "Desktop";
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
		if (response instanceof BugReportMethodResponse) {
			handleBugReportResponse((BugReportMethodResponse) response);
		}
	}

	/**
	 * Handles the response from a bug report request
	 * @param response server's method response
	 */
	private void handleBugReportResponse(BugReportMethodResponse response) {
		if (response.status.isSuccessful()) {
			mGui.hideWaitWindow();

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
		mGui.hideWaitWindow();


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


	/** Resource repository */
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
	/** subject of the bug report */
	private TextField mSubject;
	/** Last action the user did */
	private TextField mLastAction;
	/** Second last action the user did */
	private TextField mSecondLastAction;
	/** Optional description of the bug */
	private TextField mDescription;
	/** The exception to send */
	private Exception mException;
	/** The network entity to send */
	private BugReportEntity mBugReport = null;
	/** GUI to show success / progress on */
	private Gui mGui;
}
