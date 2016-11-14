package com.spiddekauga.voider.repo.analytics.listener;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.spiddekauga.voider.network.analytics.AnalyticsEventTypes;
import com.spiddekauga.voider.repo.analytics.AnalyticsRepo;

/**
 * Posts analytics events when a slider is changed
 */
public class AnalyticsSliderListener extends ChangeListener {
/** Analytics repository */
protected static AnalyticsRepo mAnalyticsRepo = AnalyticsRepo.getInstance();
/** Event name */
private String mName;

/**
 * @param eventName event name
 */
public AnalyticsSliderListener(String eventName) {
	mName = eventName;
}

@Override
public void changed(ChangeEvent event, Actor actor) {
	if (actor instanceof Slider) {
		mAnalyticsRepo.addEvent(mName, AnalyticsEventTypes.SLIDER, String.valueOf(((Slider) actor).getValue()));
	}
}

/**
 * @return name of the event
 */
protected String getName() {
	return mName;
}

}
