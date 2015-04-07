package com.spiddekauga.utils.commands;

import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * Changes the value of a slider.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CGuiSlider extends CGui implements ICommandCombinable {
	/**
	 * Creates a slider command that will change the value of the slider. Doesn't execute
	 * the first time this is called
	 * @param slider the slider to change the value of
	 * @param newValue the new value of the slider
	 * @param oldValue the old value of the slider
	 */
	public CGuiSlider(Slider slider, float newValue, float oldValue) {
		this(slider, newValue, oldValue, true);
	}

	/**
	 * Creates a slider command that will change the value of the slider. Doesn't execute
	 * the first time this is called
	 * @param slider the slider to change the value of
	 * @param newValue the new value of the slider
	 * @param oldValue the old value of the slider
	 * @param skipFirstTime skip to execute the first time this is executed
	 */
	public CGuiSlider(Slider slider, float newValue, float oldValue, boolean skipFirstTime) {
		mSlider = slider;
		mNewValue = newValue;
		mOldValue = oldValue;
		mSkipFirstTime = skipFirstTime;
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
		if (!mSkipFirstTime) {
			boolean success = setTemporaryName(mSlider);
			if (success) {
				mSlider.setValue(mNewValue);
				mSlider.fire(new ChangeListener.ChangeEvent());
				setOriginalName(mSlider);
			}

			return success;
		} else {
			mSkipFirstTime = false;
			return true;
		}
	}

	@Override
	public boolean undo() {
		setTemporaryName(mSlider);
		mSlider.setValue(mOldValue);
		setOriginalName(mSlider);
		return true;
	}

	/** Skip executing first time */
	private boolean mSkipFirstTime = true;
	/** Slider to change the value on */
	private Slider mSlider;
	/** New value of the slider (on execute) */
	private float mNewValue;
	/** Old value of the slider (on undo) */
	private float mOldValue;
}
