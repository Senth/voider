package com.spiddekauga.voider.repo.analytics.listener;

import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.voider.network.analytics.AnalyticsEventTypes;
import com.spiddekauga.voider.repo.analytics.AnalyticsRepo;

/**
 * Posts analytics events when a slider is changed
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class AnalyticsSliderListener extends SliderListener {
	/**
	 * @param eventName event name
	 * @param slider the slider to listen to
	 */
	public AnalyticsSliderListener(String eventName, Slider slider) {
		super(slider);
		mName = eventName;
	}

	@Override
	protected void onChange(float newValue) {
		mAnalyticsRepo.addEvent(mName, AnalyticsEventTypes.SLIDER, String.valueOf(newValue));
	}

	/**
	 * @return name of the event
	 */
	protected String getName() {
		return mName;
	}

	/** Event name */
	private String mName;
	/** Analytics repository */
	protected static AnalyticsRepo mAnalyticsRepo = AnalyticsRepo.getInstance();
}
