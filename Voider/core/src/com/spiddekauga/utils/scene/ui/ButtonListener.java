package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.spiddekauga.utils.scene.ui.VisibilityChangeListener.VisibilityChangeEvent;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Calls the method {@link #onChecked(Button, boolean)} when the button becomes checked/unchecked
 * Calls {@link #onPressed(Button)} when the button has been pressed.
 */
public abstract class ButtonListener implements EventListener {
/** Last button state */
private HashMap<Button, AtomicBoolean> mCheckedLast = new HashMap<>();

/**
 * Creates a button listener
 * @param buttons will listen to this button. All this does is effectively calling
 * button.addListener(this); for all buttons
 */
public ButtonListener(Button... buttons) {
	for (Button button : buttons) {
		button.addListener(this);
	}
}

@Override
public boolean handle(Event event) {
	if (event instanceof ChangeEvent && !(event instanceof VisibilityChangeEvent)) {
		Button button = getButton(event);

		if (button != null) {
			AtomicBoolean lastChecked = mCheckedLast.get(button);
			if (lastChecked == null) {
				lastChecked = new AtomicBoolean(!button.isChecked());
				mCheckedLast.put(button, lastChecked);
			}

			if (button.isChecked() != lastChecked.get()) {
				lastChecked.set(button.isChecked());

				onChecked(button, button.isChecked());
				boolean hasCheckedImage = false;
				if (button.getStyle().checked != null) {
					hasCheckedImage = true;
				} else if (button instanceof CheckBox) {
					hasCheckedImage = true;
				} else if (button.getStyle() instanceof ImageButtonStyle) {
					if (((ImageButtonStyle) button.getStyle()).imageChecked != null) {
						hasCheckedImage = true;
					}
				}
				if (!hasCheckedImage || button.isChecked()) {
					onPressed(button);
				}
			}
		}
	}
	if (event instanceof InputEvent) {
		if (((InputEvent) event).getType() == Type.touchDown) {
			Button button = getButton(event);

			if (button != null) {
				// Add button state if none exist
				AtomicBoolean lastChecked = mCheckedLast.get(button);
				if (lastChecked == null) {
					lastChecked = new AtomicBoolean(button.isChecked());
					mCheckedLast.put(button, lastChecked);
				}

				removeKeyboardFocus(button.getStage());

				onDown(button);
			}
		} else if (((InputEvent) event).getType() == Type.touchUp) {
			Button button = getButton(event);
			if (button != null) {
				onUp(button);
			}
		}
	}
	return true;
}

/**
 * Get button from event
 * @param event
 * @return button that fired the event
 */
private static Button getButton(Event event) {
	if (event.getListenerActor() instanceof Button) {
		return (Button) event.getListenerActor();
	} else {
		return null;
	}
}

/**
 * Called when an actor changed from checked to unchecked or vice versa
 * @param button button that fired the event
 * @param checked true if the new state is checked
 */
protected void onChecked(Button button, boolean checked) {
	// Does nothing
}

/**
 * Called when the button is pressed, or actually released. If the button can be checked this will
 * only be called when the button is checked.
 * @param button button that fired the event
 */
protected void onPressed(Button button) {
	// Does nothing
}

/**
 * Remove keyboard focus from current text field
 * @param stage current stage
 */
private void removeKeyboardFocus(Stage stage) {
	stage.setKeyboardFocus(null);
}

/**
 * Called when the button is being pressed down
 * @param button button that fired the event
 */
protected void onDown(Button button) {
	// Does nothing
}

/**
 * Called when the button has gone up. Not same as {@link #onPressed(Button)}.
 * @param button button that fired the event
 */
protected void onUp(Button button) {
	// Does nothing
}
}
