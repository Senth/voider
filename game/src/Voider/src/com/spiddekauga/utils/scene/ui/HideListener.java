package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

/**
 * Hides specified GUI elements when a button is enabled/disabled
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class HideListener extends GuiHider implements EventListener {
	/**
	 * Creates a hide listener, listens to the specified button if it's enabled
	 * or not. Calls onHide() after the actors get hidden, and onShow() after
	 * they are shown.
	 * @param button the button to listen for (checked/unchecked)
	 * @param showWhenChecked set to true to show the actors when this is checked.
	 * Set to false to show the actors when the button is unchecked.
	 */
	public HideListener(Button button, boolean showWhenChecked) {
		mShowWhenChecked = showWhenChecked;
		setButton(button);
	}

	/**
	 * Creates a hide listener. Calls onHide() after the actors get hidden, and onShow() after
	 * they are shown.
	 * @param showWhenChecked set to true to show the actors when this is checked.
	 * @note remember to set a button via #setButton(Button)
	 */
	public HideListener(boolean showWhenChecked) {
		mShowWhenChecked = showWhenChecked;
	}

	/**
	 * Sets the button to listen to. Also corrects the state of the actors to toggle
	 * @param button the button to listen for (checked/unchecked)
	 */
	public void setButton(Button button) {
		if (mButton != null) {
			mButton.removeListener(this);
		}

		mButton = button;

		if (mButton != null) {
			mButton.addListener(this);
			mCheckedLast = mButton.isChecked();
			updateToggleActors();
		}
	}

	@Override
	public final boolean handle(Event event) {
		if (mButton.isChecked() != mCheckedLast) {
			mCheckedLast = mButton.isChecked();
			updateToggleActors();
		}
		return true;
	}

	@Override
	protected boolean shallShowActors() {
		return mButton != null && ((mButton.isChecked() && mShowWhenChecked) || (!mButton.isChecked() && !mShowWhenChecked));
	}

	/** Button to listen for */
	protected Button mButton = null;
	/** Shows the actors when the button is checked */
	private boolean mShowWhenChecked;
	/** Last state */
	private boolean mCheckedLast;
}
