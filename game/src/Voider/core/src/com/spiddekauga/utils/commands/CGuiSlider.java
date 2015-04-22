package com.spiddekauga.utils.commands;

import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.spiddekauga.utils.scene.ui.SliderListener;

/**
 * Changes the value of a slider.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CGuiSlider extends CGui implements ICommandCombinable {
	/**
	 * Creates a slider command that will change the value of the slider. Doesn't execute
	 * the first time this is called
	 * @param sliderListener the slider to change the value of
	 * @param newValue the new value of the slider
	 * @param oldValue the old value of the slider
	 */
	public CGuiSlider(SliderListener sliderListener, float newValue, float oldValue) {
		this(null, sliderListener, newValue, oldValue, true);
	}

	/**
	 * Creates a slider command that will change the value of the slider. Doesn't execute
	 * the first time this is called
	 * @param sliderListener the slider to change the value of
	 * @param newValue the new value of the slider
	 * @param oldValue the old value of the slider
	 * @param skipFirstTime skip to execute the first time this is executed
	 */
	public CGuiSlider(SliderListener sliderListener, float newValue, float oldValue, boolean skipFirstTime) {
		this(null, sliderListener, newValue, oldValue, skipFirstTime);
	}

	/**
	 * Creates a slider command that will change the value of the slider. Doesn't execute
	 * the first time this is called
	 * @param slider the slider to change the value of
	 * @param newValue the new value of the slider
	 * @param oldValue the old value of the slider
	 */
	public CGuiSlider(Slider slider, float newValue, float oldValue) {
		this(slider, null, newValue, oldValue, true);
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
		this(slider, null, newValue, oldValue, skipFirstTime);
	}

	/**
	 * Creates a slider command that will change the value of the slider. Doesn't execute
	 * the first time this is called
	 * @param slider the slider to change the value of
	 * @param sliderListener slider listener
	 * @param newValue the new value of the slider
	 * @param oldValue the old value of the slider
	 * @param skipFirstTime skip to execute the first time this is executed
	 */
	private CGuiSlider(Slider slider, SliderListener sliderListener, float newValue, float oldValue, boolean skipFirstTime) {
		mSlider = slider;
		mSliderListener = sliderListener;
		mNewValue = newValue;
		mOldValue = oldValue;
		mSkipFirstTime = skipFirstTime;
	}

	@Override
	public boolean combine(ICommandCombinable otherCommand) {
		boolean executeSuccess = false;
		if (otherCommand instanceof CGuiSlider) {
			CGuiSlider otherSlider = (CGuiSlider) otherCommand;

			// Combine
			if (otherSlider.mSliderListener == mSliderListener && otherSlider.mSlider == mSlider && isCombinable()) {
				executeSuccess = otherSlider.execute();
				if (executeSuccess) {
					mNewValue = otherSlider.mNewValue;
					setExecuteTime();
				}
			}
		}

		return executeSuccess;
	}

	@Override
	public boolean execute() {
		setExecuteTime();

		if (!mSkipFirstTime) {
			// Slider listener
			if (mSliderListener != null) {
				if (!mSliderListener.isInvokerChangingValue()) {
					mSliderListener.setValue(mNewValue, true);
					return true;
				}
			}
			// Slider
			else if (mSlider != null) {
				boolean success = setTemporaryName(mSlider);
				if (success) {
					mSlider.setValue(mNewValue);
					setOriginalName(mSlider);
					return true;
				}
			}
		} else {
			mSkipFirstTime = false;
			return true;
		}

		return false;
	}

	@Override
	public boolean undo() {
		// Slider Listener
		if (mSliderListener != null) {
			mSliderListener.setValue(mOldValue, true);
		}
		// Slider
		else if (mSlider != null) {
			setTemporaryName(mSlider);
			mSlider.setValue(mOldValue);
			setOriginalName(mSlider);
		}
		return true;
	}

	/** Skip executing first time */
	private boolean mSkipFirstTime = true;
	private Slider mSlider;
	private SliderListener mSliderListener;
	/** New value of the slider (on execute) */
	private float mNewValue;
	/** Old value of the slider (on undo) */
	private float mOldValue;
}
