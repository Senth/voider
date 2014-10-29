package com.spiddekauga.utils.commands;

import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * Changes the value of a slider.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CGuiSlider extends CGui implements ICommandCombinable {
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
		boolean success = setTemporaryName(mSlider);
		if (success) {
			mSlider.setValue(mNewValue);
			mSlider.fire(new ChangeListener.ChangeEvent());
			setOriginalName(mSlider);
		}

		return success;
	}

	@Override
	public boolean undo() {
		setTemporaryName(mSlider);
		mSlider.setValue(mOldValue);
		setOriginalName(mSlider);
		return true;
	}

	/** Slider to change the value on */
	private Slider mSlider;
	/** New value of the slider (on execute) */
	private float mNewValue;
	/** Old value of the slider (on undo) */
	private float mOldValue;
}
