package com.spiddekauga.utils.scene.ui;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

/**
 * Hides specified GUI elements when either one or several buttons is enabled/disabled If
 * several buttons have been added, if any of them are checked (if showWhenChecked is
 * true, otherwise if one of them isn't checked) the toggle actors will be shown
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class HideListener extends GuiHider implements EventListener {
	/**
	 * Creates a hide listener, listens to the specified button if it's enabled or not.
	 * Calls onHide() after the actors get hidden, and onShow() after they are shown.
	 * @param showWhenChecked set to true to show the actors when this is checked. Set to
	 *        false to show the actors when the button is unchecked.
	 * @param buttons the buttons to listen for (checked/unchecked)
	 */
	public HideListener(boolean showWhenChecked, Button... buttons) {
		mShowWhenChecked = showWhenChecked;
		for (Button button : buttons) {
			addButton(button);
		}
	}

	/**
	 * Creates a hide listener. Calls onHide() after the actors get hidden, and onShow()
	 * after they are shown.
	 * @param showWhenChecked set to true to show the actors when this is checked.
	 * @note remember to set a button via #setButton(Button)
	 */
	public HideListener(boolean showWhenChecked) {
		mShowWhenChecked = showWhenChecked;
	}

	/**
	 * Adds a button to listen to. Multiple buttons can be added
	 * @param button the button to listen for (checked/unchecked)
	 */
	public void addButton(Button button) {
		button.addListener(this);
		mCheckedLast.put(button, new AtomicBoolean(button.isChecked()));
		updateToggleActors();
	}

	@Override
	public final boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
			if (event.getListenerActor() instanceof Button) {
				Button button = (Button) event.getListenerActor();
				AtomicBoolean checkedLast = mCheckedLast.get(button);
				if (checkedLast.get() != button.isChecked()) {
					checkedLast.set(button.isChecked());
					updateToggleActors();
				}
			}
		}
		return false;
	}

	@Override
	protected boolean shallShowActors() {
		if (mShowWhenChecked) {
			for (Button button : mCheckedLast.keySet()) {
				if (button.isChecked()) {
					return true;
				}
			}
		} else {
			for (Button button : mCheckedLast.keySet()) {
				if (!button.isChecked()) {
					return true;
				}
			}
		}
		return false;
	}

	/** Shows the actors when the button is checked */
	private boolean mShowWhenChecked;
	/** Last state */
	private HashMap<Button, AtomicBoolean> mCheckedLast = new HashMap<>();
}
