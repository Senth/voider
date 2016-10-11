package com.spiddekauga.utils.commands;

import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * Command for changing a text field's value
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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
	}

	@Override
	public boolean combine(ICommandCombinable otherCommand) {
		boolean executeSuccess = false;

		if (otherCommand instanceof CGuiTextField) {
			CGuiTextField otherTextField = (CGuiTextField) otherCommand;

			// Combine
			if (otherTextField.mTextField == mTextField && isCombinable()) {
				executeSuccess = otherTextField.execute();

				if (executeSuccess) {
					mAfter = otherTextField.mAfter;
					setExecuteTime();
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
		}
		setExecuteTime();

		return success;
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
		mTextField.fire(new ChangeListener.ChangeEvent());
		setOriginalName(mTextField);
		return true;
	}

	/** Text field to change */
	private TextField mTextField;
	/** String before the change (on undo) */
	private String mBefore;
	/** String after the change (on execute) */
	private String mAfter;
}
