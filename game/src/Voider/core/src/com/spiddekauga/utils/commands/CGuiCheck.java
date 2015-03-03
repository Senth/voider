package com.spiddekauga.utils.commands;

import com.badlogic.gdx.scenes.scene2d.ui.Button;

/**
 * A command that checks a GUI element. Undo will uncheck it.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CGuiCheck extends CGui {
	/**
	 * Creates a command that checks/unchecks a button.
	 * @param button the button to check/uncheck
	 * @param check set to true if it shall check the button, false to uncheck it
	 *
	 */
	public CGuiCheck(Button button, boolean check) {
		mCheckOnExecute = button;
		mCheck = check;
	}

	/**
	 * Creates a command that checks a button and checks another
	 * button on undo. This works if both buttons belongs to the same group
	 * @param checkOnExecute the button to check now
	 * @param checkOnUndo the button to check on undo
	 * @See {@link #CGuiCheck(Button, boolean)} if you just want to check/uncheck one button
	 */
	public CGuiCheck(Button checkOnExecute, Button checkOnUndo) {
		mCheckOnExecute = checkOnExecute;
		mCheckOnUndo = checkOnUndo;
	}

	@Override
	public boolean execute() {
		// Set temporary name, this will make sure the event doesn't fire
		// another CGuiCheck command.
		boolean success = setTemporaryName(mCheckOnExecute);
		if (success) {
			mCheckOnExecute.setChecked(mCheck);
			setOriginalName(mCheckOnExecute);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean undo() {
		// Set temporary name, this will make sure the event doesn't fire
		// another CGuiCheck command.
		Button buttonToCheck = null;
		if (mCheckOnUndo != null) {
			buttonToCheck = mCheckOnUndo;

		} else {
			buttonToCheck = mCheckOnExecute;
		}

		setTemporaryName(buttonToCheck);
		buttonToCheck.setChecked(true);
		setOriginalName(buttonToCheck);

		return true;
	}

	/** If the button should be checked/unchecked */
	private boolean mCheck = true;
	/** Button to check/uncheck, or check on execute */
	private Button mCheckOnExecute;
	/** Button to check on undo */
	private Button mCheckOnUndo = null;
}
