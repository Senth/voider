package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.spiddekauga.utils.ICommandCombinable;
import com.spiddekauga.utils.Maths;
import com.spiddekauga.voider.Config.Gui;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * Command for changing a text field's value
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CGuiTextField extends CGui implements ICommandCombinable {
	/**
	 * Creates a command that will change the text field's value
	 * @param textField the text field to change
	 * @param before string before the change (on undo)
	 * @param after string after the change (on execute)
	 */
	public CGuiTextField(TextField textField, String before, String after) {
		mTextField = textField;
		mBefore = before;
		mAfter = after;

		mCreatedTime = SceneSwitcher.getGameTime().getTotalTimeElapsed();
	}

	@Override
	public boolean combine(ICommandCombinable otherCommand) {
		boolean executeSuccess = false;

		if (otherCommand instanceof CGuiTextField) {
			// Must be same text field
			if (((CGuiTextField) otherCommand).mTextField == mTextField) {
				if (Maths.approxCompare(mCreatedTime,((CGuiTextField)otherCommand).mCreatedTime, Gui.TEXT_FIELD_COMBINABLE_WITHIN)) {
					executeSuccess = ((CGuiTextField) otherCommand).execute();

					if (executeSuccess) {
						mAfter = ((CGuiTextField)otherCommand).mAfter;
						mCreatedTime = ((CGuiTextField)otherCommand).mCreatedTime;
					}
				}
			}
		}

		return executeSuccess;
	}

	@Override
	public boolean execute() {
		boolean success = setTemporaryName(mTextField);
		if (success) {
			if (!mTextField.getText().equals(mAfter)) {
				int cursorPosition = mTextField.getCursorPosition();
				mTextField.setText(mAfter);
				if (cursorPosition >= mAfter.length()) {
					mTextField.setCursorPosition(mAfter.length() - 1);
				} else {
					mTextField.setCursorPosition(cursorPosition);
				}
			}
			setOriginalName(mTextField);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean undo() {
		setTemporaryName(mTextField);
		int cursorPosition = mTextField.getCursorPosition();
		mTextField.setText(mBefore);
		if (cursorPosition >= mAfter.length()) {
			if (!mAfter.isEmpty()) {
				mTextField.setCursorPosition(mAfter.length() - 1);
			}
		} else {
			mTextField.setCursorPosition(cursorPosition);
		}
		setOriginalName(mTextField);
		return true;
	}

	/** Text field to change */
	private TextField mTextField;
	/** String before the change (on undo) */
	private String mBefore;
	/** String after the change (on execute) */
	private String mAfter;
	/** When the command was created */
	private float mCreatedTime;
}
