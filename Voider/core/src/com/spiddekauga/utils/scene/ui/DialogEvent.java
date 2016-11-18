package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.spiddekauga.utils.EventBus;

/**
 * Event for dialogs
 */
class DialogEvent {
private IDialog mDialog;
private EventTypes mEventType;

/**
 * Create a new dialog event
 * @param dialog the dialog actor. Should be of either {@link MsgBox} or {@link
 * com.badlogic.gdx.scenes.scene2d.ui.Window}.
 */
DialogEvent(EventTypes eventType, IDialog dialog) {
	mEventType = eventType;
	mDialog = dialog;
}

/**
 * The dialog that posted the event
 * @return dialog that posted the event
 */
public IDialog getDialog() {
	return mDialog;
}

public EventTypes getEventType() {
	return mEventType;
}

/**
 * Show and hide event for dialogs
 */
public enum EventTypes {
	/** Called when the the dialog has called show() */
	SHOW,
	/** Called once the dialog has been removed from the {@link com.badlogic.gdx.scenes.scene2d.Stage}. */
	REMOVE,
}

/**
 * Action to post a dialog event
 */
static class PostEventAction extends Action {
	private static final EventBus mEventBus = EventBus.getInstance();
	private boolean mPostedEvent = false;
	private EventTypes mEventType;

	PostEventAction(EventTypes eventType) {
		mEventType = eventType;
	}

	@Override
	public boolean act(float delta) {
		Actor actor = getActor();
		if (actor instanceof IDialog) {
			if (!mPostedEvent) {
				mEventBus.post(new DialogEvent(mEventType, (IDialog) actor));
				mPostedEvent = true;
			}
		}
		return true;
	}
}
}
