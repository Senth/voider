package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.spiddekauga.utils.Command;

/**
 * Sends a bug report to the server
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CBugReportSend extends Command {
	/**
	 * Creates a command that will send a bug report.
	 * @param lastAction text field for the last action before the bug occured
	 * @param secondLastAction text field for the second last action
	 * @param thirdLastAction text field for the third last action
	 * @param description text field for the description
	 * @param exception the exception that was thrown
	 */
	public CBugReportSend(
			TextField lastAction,
			TextField secondLastAction,
			TextField thirdLastAction,
			TextField description,
			Exception exception
			) {
		mLastAction = lastAction;
		mSecondLastAction = secondLastAction;
		mThirdLastAction = thirdLastAction;
		mDescription = description;
		mException = exception;
	}

	/**
	 * Creates a command that will send a bug report.
	 * @param lastAction text field for the last action before the bug occured
	 * @param secondLastAction text field for the second last action
	 * @param thirdLastAction text field for the third last action
	 * @param description text field for the description
	 */
	public CBugReportSend(
			TextField lastAction,
			TextField secondLastAction,
			TextField thirdLastAction,
			TextField description
			) {
		this(lastAction, secondLastAction, thirdLastAction, description, null);
	}

	@Override
	public boolean execute() {
		if (!sendBugReportToServer()) {
			saveBugReportLocally();
		}

		Gdx.app.exit();
		return true;
	}


	/**
	 * Send the bug report to the server
	 * @return true if the report was successfully transfered to the server
	 */
	private boolean sendBugReportToServer() {
		/** @todo not implemented, needs a server to send the report to */
		return true;
	}

	/**
	 * Save bug report if we cannot connect to the server atm.
	 */
	private void saveBugReportLocally() {
		/** @todo not implemented, needs a server to send the report to */
	}

	@Override
	public boolean undo() {
		// Does nothing
		return true;
	}

	/** Last action text field */
	TextField mLastAction;
	/** Second last action text field */
	TextField mSecondLastAction;
	/** Third last action text field */
	TextField mThirdLastAction;
	/** Description text field */
	TextField mDescription;
	/** Exception that was thrown */
	Exception mException;
}
