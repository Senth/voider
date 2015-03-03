package com.spiddekauga.voider.repo.analytics.listener;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.network.analytics.AnalyticsEventTypes;
import com.spiddekauga.voider.repo.analytics.AnalyticsRepo;

/**
 * Analytics button listener, can only contain one button. Sends an analytics event when
 * the button is pressed
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class AnalyticsButtonListener extends ButtonListener {
	/**
	 * Listens to the specified button
	 * @param button listens to this button
	 * @param name event name
	 */
	public AnalyticsButtonListener(Button button, String name) {
		this(button, name, null);
	}

	/**
	 * Listens to the specified button
	 * @param button listens to this button
	 * @param name event name
	 * @param data optional data to display, null to just use pressed/checked/unchecked.
	 */
	public AnalyticsButtonListener(Button button, String name, String data) {
		super(button);
		mName = name;
		if (data == null) {
			mData = "";
		} else {
			mData = data + " -> ";
		}

		ButtonStyle buttonStyle = button.getStyle();
		if (buttonStyle.checked != null) {
			mCheckable = true;
		} else if (button instanceof ImageButton) {
			ImageButtonStyle imageButtonStyle = ((ImageButton) button).getStyle();
			if (imageButtonStyle.imageChecked != null) {
				mCheckable = true;
			}
		}
	}

	@Override
	protected void onPressed(Button button) {
		if (!mCheckable) {
			mAnalyticsRepo.addEvent(mName, AnalyticsEventTypes.BUTTON, mData + "pressed");
		}
	}

	@Override
	protected void onChecked(Button button, boolean checked) {
		if (mCheckable) {
			String data = mData;
			data += checked ? "checked" : "unchecked";
			mAnalyticsRepo.addEvent(mName, AnalyticsEventTypes.BUTTON, data);
		}
	}

	private boolean mCheckable = false;
	private String mName;
	private String mData;
	private static AnalyticsRepo mAnalyticsRepo = AnalyticsRepo.getInstance();
}
