package com.spiddekauga.voider.editor.commands;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.spiddekauga.utils.Command;

/**
 * A command that checks a GUI element. Undo will uncheck it, and if
 * it belongs to a group it will check the other actor that was
 * unchecked by this command
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CGuiCheck extends Command {
	/**
	 * Creates a command that checks a GUI elements
	 * @param button the button to check/uncheck
	 * @param check set to true if it shall check the button, false to uncheck it
	 */
	public CGuiCheck(Button button, boolean check) {
		mButton = button;
		mCheck = check;

		if (mfButtonGroup == null) {
			try {
				mfButtonGroup = Button.class.getField("buttonGroup");
				mfButtonGroup.setAccessible(true);
			} catch (Exception e) {
				Gdx.app.error("CGuiCheck", e.toString());
			}
		}
	}

	@Override
	public boolean execute() {
		try {
			ButtonGroup buttonGroup = (ButtonGroup) mfButtonGroup.get(mButton);

			// Has a button group, remember other checked buttons
			if (buttonGroup != null && mCheck) {
				for (Button button : buttonGroup.getButtons()) {
					if (button.isChecked()) {
						mGroupButtons.add(button);
					}
				}
			}

		} catch (Exception e) {
			Gdx.app.error("CGuiCheck", e.toString());
			return false;
		}

		mButton.setChecked(mCheck);

		return true;
	}

	@Override
	public boolean undo() {
		mButton.setChecked(!mCheck);

		// Return state of the old buttons
		for (Button button : mGroupButtons) {
			button.setChecked(true);
		}

		return true;
	}

	/** If the button should be checked/unchecked */
	private boolean mCheck;
	/** Button to check/uncheck */
	private Button mButton;
	/** Field for reflection to get the button group */
	private static Field mfButtonGroup = null;
	/** Buttons from the button group that were checked/unchecked */
	private ArrayList<Button> mGroupButtons = new ArrayList<Button>();
}
