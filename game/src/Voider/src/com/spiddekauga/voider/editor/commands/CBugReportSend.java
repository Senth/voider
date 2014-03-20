package com.spiddekauga.voider.editor.commands;

import java.util.Date;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.voider.network.entities.BugReportEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.BugReportMethodResponse;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.LoginMethodResponse;
import com.spiddekauga.voider.repo.BugReportWebRepo;
import com.spiddekauga.voider.repo.ICallerResponseListener;
import com.spiddekauga.voider.repo.UserWebRepo;
import com.spiddekauga.voider.resources.BugReportDef;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.User;

/**
 * Sends a bug report to the server
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CBugReportSend extends Command implements ICallerResponseListener {
	/**
	 * Creates a command that will send a bug report.
	 * @param gui the GUI to show progress and success on
	 * @param lastAction text field for the last action before the bug occurred
	 * @param secondLastAction text field for the second last action
	 * @param thirdLastAction text field for the third last action
	 * @param expectedOutcome text field for the expected outcome
	 * @param actualOutcome text field for the actual outcome
	 * @param description text field for the description
	 * @param exception the exception that was thrown
	 */
	public CBugReportSend(
			Gui gui,
			TextField lastAction,
			TextField secondLastAction,
			TextField thirdLastAction,
			TextField expectedOutcome,
			TextField actualOutcome,
			TextField description,
			Exception exception
			) {
		mGui = gui;

		String userKey = User.getGlobalUser().getServerKey();

		// Create network entity
		mBugReport.userKey = userKey;
		mBugReport.date = new Date();
		mBugReport.lastAction = lastAction.getText();
		mBugReport.secondLastAction = secondLastAction.getText();
		mBugReport.thirdLastAction = thirdLastAction.getText();
		mBugReport.expectedOutcome = expectedOutcome.getText();
		mBugReport.actualOutcome = actualOutcome.getText();
		mBugReport.description = description.getText();
		mBugReport.exception = exception;
	}

	/**
	 * Creates a command that will send a bug report.
	 * @param gui GUI to show progress and success on
	 * @param lastAction text field for the last action before the bug occurred
	 * @param secondLastAction text field for the second last action
	 * @param thirdLastAction text field for the third last action
	 * @param expectedOutcome text field for the expected outcome
	 * @param actualOutcome text field for the actual outcome
	 * @param description text field for the description
	 */
	public CBugReportSend(
			Gui gui,
			TextField lastAction,
			TextField secondLastAction,
			TextField thirdLastAction,
			TextField expectedOutcome,
			TextField actualOutcome,
			TextField description
			) {
		this(gui, lastAction, secondLastAction, thirdLastAction, expectedOutcome, actualOutcome, description, null);
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		// Login
		if (response instanceof LoginMethodResponse) {
			handleLoginResponse((LoginMethodResponse) response);
		}
		// Bug report
		else if (response instanceof BugReportMethodResponse) {
			handleBugReportResponse((BugReportMethodResponse) response);
		}
	}

	/**
	 * Handles the response from a login request
	 * @param response server's method response
	 */
	private void handleLoginResponse(LoginMethodResponse response) {
		if (response.status.isSuccessful()) {
			sendBugReport();
		} else {
			saveBugReportLocally();
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
		mGui.setWaitWindowText("Going online");
		User user = User.getGlobalUser();
		UserWebRepo.getInstance().login(this, user.getUsername(), user.getPrivateKey());
	}

	/**
	 * Save the bug report locally
	 */
	private void saveBugReportLocally() {
		ResourceSaver.save(new BugReportDef(mBugReport));
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
		BugReportWebRepo.getInstance().sendBugReport(this, mBugReport);
	}

	@Override
	public boolean undo() {
		// Does nothing
		return true;
	}

	/** The network entity to send */
	private BugReportEntity mBugReport = new BugReportEntity();
	/** GUI to show success / progress on */
	private Gui mGui;
}
