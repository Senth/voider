package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.EventBus;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.squareup.otto.Subscribe;

import java.util.Stack;

/**
 * Helper class for keeping track on all the dialogs (message boxes, progress bars, wait windows).
 */
class DialogShower implements Disposable {
private static final EventBus mEventBus = EventBus.getInstance();
private static final UiFactory mUiFactory = UiFactory.getInstance();
private Gui mGui;
private Stack<MsgBox> mActiveMessageBoxes = new Stack<>();
private ProgressBar mProgressBar = null;

/**
 * Creates a new dialog shower for the specified GUI
 * @param gui create a dialog shower for this GUI
 */
DialogShower(Gui gui) {
	mGui = gui;
	mEventBus.register(this);
}

/**
 * Show a {@link MsgBox}. If there is a {@link MsgBox} active, it will hide it and keep it on the
 * stack. If a {@link ProgressBar} is active it will enqueue the {@link MsgBox} and it will be shown
 * after the progress bar has been hidden.
 * @param msgBox the message box to show.
 */
static void show(MsgBox msgBox) {
	Gui activeGui = Gui.getActiveGui();
	if (activeGui != null) {
		DialogShower dialogShower = activeGui.getDialogShower();
		dialogShower._show(msgBox);
	}
}

/**
 * Show a {@link MsgBox}. If there is a {@link MsgBox} active, it will hide it and keep it on the
 * stack. If a {@link ProgressBar} is active it will enqueue the {@link MsgBox} and it will be shown
 * after the progress bar has been hidden.
 * @param msgBox the message box to show.
 */
private void _show(MsgBox msgBox) {
	// Enqueue the message box
	if (mProgressBar != null) {
		mActiveMessageBoxes.push(msgBox);
	}
	// Show the message box
	else {
		// Hide the active message box if one exists
		if (!mActiveMessageBoxes.isEmpty()) {
			mActiveMessageBoxes.peek().fadeOut();
		}

		mActiveMessageBoxes.push(msgBox);
		msgBox.show(mGui.getStage());
	}
}

/**
 * Show a {@link ProgressBar}. If there is a {@link MsgBox} active, it will fade out and kept on the
 * stack.
 * @param progressBar the progress bar to display
 */
static void show(ProgressBar progressBar) {
	Gui activeGui = Gui.getActiveGui();
	if (activeGui != null) {
		DialogShower dialogShower = activeGui.getDialogShower();
		dialogShower._show(progressBar);
	}
}

/**
 * Show a {@link ProgressBar}. If there is a {@link MsgBox} active, it will fade out and kept on the
 * stack.
 * @param progressBar the progress bar to display
 */
private void _show(ProgressBar progressBar) {
	if (mProgressBar != progressBar) {
		mProgressBar = progressBar;

		// Hide active message box
		if (!mActiveMessageBoxes.isEmpty()) {
			mActiveMessageBoxes.peek().fadeOut();
		}
	}

	progressBar.show(mGui.getStage());
}

/**
 * @return return true if any dialog is active
 */
boolean isActive() {
	return mProgressBar != null || !mActiveMessageBoxes.isEmpty();
}

/**
 * Removes a dialog if it has been hidden and not first in the stack
 */
@Subscribe
@SuppressWarnings("unused")
public void removeDialog(DialogEvent event) {
	if (event.getEventType() == DialogEvent.EventTypes.REMOVE) {
		IDialog dialog = event.getDialog();

		// Message box - only remove if it's the current message box
		if (dialog instanceof MsgBox) {
			MsgBox msgBox = (MsgBox) dialog;

			if (!mActiveMessageBoxes.isEmpty()) {
				if (msgBox == mActiveMessageBoxes.peek()) {
					mActiveMessageBoxes.pop();

					if (!mActiveMessageBoxes.isEmpty()) {
						mActiveMessageBoxes.peek().fadeIn();
					}
				}
			}
		}
		// Progress Bar
		else if (dialog instanceof ProgressBar) {
			if (dialog == mProgressBar) {
				mProgressBar = null;

				if (!mActiveMessageBoxes.isEmpty()) {
					mActiveMessageBoxes.peek().fadeIn();
				}
			}
		}
	}
}

public void dispose() {
	while (!mActiveMessageBoxes.isEmpty()) {
		MsgBox msgBox = mActiveMessageBoxes.pop();
		mUiFactory.msgBox.free(msgBox);
	}
	mEventBus.unregister(this);
}
}
