package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.Config;

/**
 * A command that checks a GUI element. Undo will uncheck it, and if
 * it belongs to a group it will check the other actor that was
 * unchecked by this command
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CGuiCheck extends Command {
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
		String oldName = mCheckOnExecute.getName();
		mCheckOnExecute.setName(Config.Editor.GUI_INVOKER_TEMP_NAME);
		mCheckOnExecute.setChecked(mCheck);
		mCheckOnExecute.setName(oldName);

		return true;
	}

	@Override
	public boolean undo() {
		// Set temporary name, this will make sure the event doesn't fire
		// another CGuiCheck command.
		if (mCheckOnUndo != null) {
			String oldName = mCheckOnUndo.getName();
			mCheckOnUndo.setName(Config.Editor.GUI_INVOKER_TEMP_NAME);
			mCheckOnUndo.setChecked(true);
			mCheckOnUndo.setName(oldName);
		} else {
			String oldName = mCheckOnExecute.getName();
			mCheckOnExecute.setName(Config.Editor.GUI_INVOKER_TEMP_NAME);
			mCheckOnExecute.setChecked(!mCheck);
			mCheckOnExecute.setName(oldName);
		}

		return true;
	}

	/** If the button should be checked/unchecked */
	private boolean mCheck = true;
	/** Button to check/uncheck, or check on execute */
	private Button mCheckOnExecute;
	/** Button to check on undo */
	private Button mCheckOnUndo = null;
}
