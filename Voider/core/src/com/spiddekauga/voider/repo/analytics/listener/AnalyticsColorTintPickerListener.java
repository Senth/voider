package com.spiddekauga.voider.repo.analytics.listener;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.spiddekauga.utils.scene.ui.ColorTintPicker;
import com.spiddekauga.voider.network.analytics.AnalyticsEventTypes;

/**
 * Posts analytics event when the color is changed
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class AnalyticsColorTintPickerListener extends AnalyticsSliderListener {

	/**
	 * @param eventName name of the event
	 */
	public AnalyticsColorTintPickerListener(String eventName) {
		super(eventName);
	}


	@Override
	public void changed(ChangeEvent event, Actor actor) {
		if (actor instanceof ColorTintPicker) {
			ColorTintPicker colorTintPicker = (ColorTintPicker) actor;
			mAnalyticsRepo.addEvent(getName(), AnalyticsEventTypes.SLIDER, colorTintPicker.getPickColor().toString());
		} else {
			super.changed(event, actor);
		}
	}
}
