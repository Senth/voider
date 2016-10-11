package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;

/**
 * Hides/shows actors depending on the current slider value
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class HideSliderValue extends GuiHider implements EventListener {
	/**
	 * Creates a hide slider for the specified slider. To show the actors the
	 * slider needs to be in [min,max] range. This will automatically add
	 * this HideSlideValue as a listener to the slider.
	 * @param slider the slider to listen to
	 * @param min minimum value of the slider to show the actors
	 * @param max maximum value of the slider to show the actors
	 */
	public HideSliderValue(Slider slider, float min, float max) {
		mSlider = slider;
		mMinValue = min;
		mMaxValue = max;

		mSlider.addListener(this);
		mOldValue = mSlider.getValue();
	}

	@Override
	public boolean handle(Event event) {
		if (mOldValue != mSlider.getValue()) {
			updateToggleActors();
			mOldValue = mSlider.getValue();
		}
		return true;
	}

	@Override
	protected boolean shallShowActors() {
		return mSlider.getValue() >= mMinValue && mSlider.getValue() <= mMaxValue;
	}

	/** Old value, saved so we don't update too often... */
	private float mOldValue = 0;
	/** Slider to check the value from */
	private Slider mSlider;
	/** Minimum value of the slider needed to show the actors */
	private float mMinValue;
	/** Maximmum value of the slider needed to show the actors */
	private float mMaxValue;
}
