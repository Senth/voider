package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.spiddekauga.utils.Command;
import com.spiddekauga.utils.ICommandCombinable;

/**
 * Changes the value of a slider.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CGuiSlider extends Command implements ICommandCombinable {
	/**
	 * Creates a slider command that will change the value of the slider
	 * @param slider the slider to change the value of
	 * @param newValue the new value of the slider
	 * @param oldValue the old value of the slider
	 */
	public CGuiSlider(Slider slider, float newValue, float oldValue) {
		mSlider = slider;
		mNewValue = newValue;
		mOldValue = oldValue;
	}

	@Override
	public boolean combine(ICommandCombinable otherCommand) {
		boolean executeSuccess = false;
		if (otherCommand instanceof CGuiSlider) {
			// Same slider, else skip...
			if (((CGuiSlider) otherCommand).mSlider == mSlider) {
				executeSuccess = ((CGuiSlider) otherCommand).execute();

				if (executeSuccess) {
					mNewValue = ((CGuiSlider) otherCommand).mNewValue;
				}
			}
		}

		return executeSuccess;
	}

	@Override
	public boolean execute() {
		if (mSlider.getName() == null || !mSlider.getName().equals(TEMP_NAME)) {
			mName = mSlider.getName();
			mSlider.setName(TEMP_NAME);

			mSlider.setValue(mNewValue);

			mSlider.setName(mName);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean undo() {
		mName = mSlider.getName();
		mSlider.setName(TEMP_NAME);
		mSlider.setValue(mOldValue);
		mSlider.setName(mName);
		return true;
	}

	/** Old slider name */
	private String mName = null;
	/** Slider to change the value on */
	private Slider mSlider;
	/** New value of the slider (on execute) */
	private float mNewValue;
	/** Old value of the slider (on undo) */
	private float mOldValue;

	/** Temporary slider name to avoid executing again */
	private static final String TEMP_NAME = "C_GUI_SLIDER";
}
