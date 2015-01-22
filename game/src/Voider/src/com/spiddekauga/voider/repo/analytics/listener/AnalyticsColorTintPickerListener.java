package com.spiddekauga.voider.repo.analytics.listener;

import com.spiddekauga.utils.scene.ui.ColorTintPicker;
import com.spiddekauga.voider.network.analytics.AnalyticsEventTypes;

/**
 * Posts analytics event when the color is changed
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class AnalyticsColorTintPickerListener extends AnalyticsSliderListener {
	/**
	 * @param eventName analytics event name
	 * @param colorTintPicker
	 */
	public AnalyticsColorTintPickerListener(String eventName, ColorTintPicker colorTintPicker) {
		super(eventName, colorTintPicker);
		mColorTintPicker = colorTintPicker;
	}

	@Override
	protected void onChange(float newValue) {
		mAnalyticsRepo.addEvent(getName(), AnalyticsEventTypes.SLIDER, mColorTintPicker.getPickColor().toString());
	}

	private ColorTintPicker mColorTintPicker;
}
