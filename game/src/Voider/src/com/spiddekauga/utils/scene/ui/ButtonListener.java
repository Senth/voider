package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

/**
 * Calls the method {@link #onChecked(boolean)} when the button becomes checked/unchecked
 * Calls {@link #onPressed()} when the button has been pressed.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class ButtonListener implements EventListener {
	/**
	 * Creates an empty button listener. You need to call {@link #setButton(Button)}
	 * before you can use this listener appropriately
	 */
	public ButtonListener() {
		// Does nothing
	}

	/**
	 * Creates a button listener for the specified button. Always calls onChange. Adds
	 * this class to the button automatically as a listener.
	 * @param button the button to listen to
	 */
	public ButtonListener(Button button) {
		setButton(button);
	}

	/**
	 * Sets the button for this listener.
	 * @param button the button to listen to
	 */
	public void setButton(Button button) {
		if (button == null) {
			throw new IllegalArgumentException("button cannot be null");
		}

		if (mButton != null) {
			mButton.removeListener(this);
		}

		mButton = button;
		mButton.addListener(this);
		mCheckedLast = mButton.isChecked();
	}

	@Override
	public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
			if (mButton.isChecked() != mCheckedLast) {
				mCheckedLast = mButton.isChecked();
				onChecked(mCheckedLast);
				onPressed();
			}
		}
		if (event instanceof InputEvent) {
			if (((InputEvent) event).getType() == Type.touchDown) {
				onDown();
			} else if (((InputEvent) event).getType() == Type.touchUp) {
				onUp();
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
	 * Called when the button is pressed, or actually released. Will not be called if the
	 * tooltip message box is active even if the button is pressed.
	 */
	protected void onPressed() {
		// Does nothing
	}

	/**
	 * Called when the button is being pressed down
	 */
	protected void onDown() {
		// Does nothing
	}

	/**
	 * Called when the button has gone up. Not same as {@link #onPressed()}.
	 */
	protected void onUp() {
		// Does nothing
	}

	/** The button we check for the checked state */
	protected Button mButton;
	/** Last known state of the button */
	private boolean mCheckedLast;
}
