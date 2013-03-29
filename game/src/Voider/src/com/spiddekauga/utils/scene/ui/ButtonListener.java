package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

/**
 * Calls the method {@link #onChecked(boolean)} when the button becomes checked/unchecked
 * Calls {@link #onPressed()} when the button has been pressed.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class ButtonListener implements EventListener {
	/**
	 * Creates a button listener for the specified button. Always
	 * calls onChange. Adds this class to the button automatically as a listener.
	 * @param button the button to listen to
	 */
	public ButtonListener(Button button) {
		this(button, null);
	}

	/**
	 * Creates a button listener for the specified button. Always
	 * calls onChange. Adds this class to the button automatically as a listener.
	 * When adding the tooltip listener this will make sure the tooltip message box
	 * isn't active when calling the {@link #onPressed()} method.
	 * @param button the button to listen to
	 * @param tooltipListener the GUI to check if the button shall be pressed.
	 */
	public ButtonListener(Button button, TooltipListener tooltipListener) {
		mButton = button;
		mButton.addListener(this);
		mCheckedLast = mButton.isChecked();
		mTooltipListener = tooltipListener;
	}

	@Override
	public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
			if (mButton.isChecked() != mCheckedLast) {
				mCheckedLast = mButton.isChecked();
				onChecked(mCheckedLast);

				if (mTooltipListener == null || !mTooltipListener.isMsgBoxActive()) {
					onPressed();
				}
			}
		}
		return true;
	}

	/**
	 * Called when an actor changed from checked to unchecked or vice versa
	 * @param checked true if the new state is checked
	 */
	protected void onChecked(boolean checked) {
		// Does nothing
	}

	/**
	 * Called when the button is pressed. Will not be called if the tooltip message box
	 * is active even if the button is pressed.
	 */
	protected void onPressed() {
		// Does nothing
	}

	/** The button we check for the checked state */
	protected Button mButton;
	/** Last known state of the button */
	private boolean mCheckedLast;
	/** The Tooltip to check if the tooltip message box is being displayed */
	private TooltipListener mTooltipListener;
}
