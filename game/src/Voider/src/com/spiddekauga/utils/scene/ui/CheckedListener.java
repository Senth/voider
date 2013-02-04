package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

/**
 * Calls the method #onChange(boolean) when the listener changed.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class CheckedListener implements EventListener {
	/**
	 * Creates a checked listener for the specified button. Always
	 * calls onChange. Adds this class to the button automatically as a listener.
	 * @param button the button to listen to
	 */
	public CheckedListener(Button button) {
		mButton = button;
		mButton.addListener(this);
		mCheckedLast = mButton.isChecked();

		onChange(mCheckedLast);
	}

	@Override
	public boolean handle(Event event) {
		if (mButton.isChecked() != mCheckedLast) {
			mCheckedLast = mButton.isChecked();
			onChange(mCheckedLast);
		}
		return true;
	}

	/**
	 * Called when an actor changed from checked to unchecked or vice versa
	 * @param checked true if the new state is checked
	 */
	protected abstract void onChange(boolean checked);

	/** The button we check for the checked state */
	protected Button mButton;
	/** Last know state of the button */
	private boolean mCheckedLast;
}
