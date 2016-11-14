package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.game.triggers.ITriggerListener;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.game.triggers.TriggerAction;
import com.spiddekauga.voider.game.triggers.TriggerInfo;

/**
 * A command that will set the specific trigger for a trigger listener.
 */
public class CTriggerSet extends CResourceChange {
/** Trigger to set */
Trigger mNewTrigger;
/** Old trigger */
Trigger mOldTrigger = null;
/** Listener */
ITriggerListener mListener;
/** Trigger info */
TriggerInfo mTriggerInfo = null;
/**
 * Creates a command that will set, add, or remove the specified trigger for the listener.
 * @param listener the listener to set, add, or remove the trigger from
 * @param action the type of action to look for. If the listener doesn't have an action of the
 * specified type it will create the trigger.
 * @param trigger the trigger to bind, if null it will remove any trigger with the specified
 * action.
 * @param editor the editor to receive the onChanged event
 */
public CTriggerSet(ITriggerListener listener, TriggerAction.Actions action, Trigger trigger, IResourceChangeEditor editor) {
	super(listener, editor);
	mNewTrigger = trigger;
	mListener = listener;

	TriggerInfo existingInfo = TriggerInfo.getTriggerInfoByAction(mListener, action);

	if (existingInfo != null) {
		mOldTrigger = existingInfo.trigger;
		mTriggerInfo = existingInfo;
	} else if (mNewTrigger != null) {
		mTriggerInfo = new TriggerInfo();
		mTriggerInfo.listener = mListener;
		mTriggerInfo.delay = 0;
		mTriggerInfo.action = action;
		mTriggerInfo.setTrigger(mNewTrigger);
	}
}

@Override
public boolean execute() {
	// Set or add new trigger
	if (mNewTrigger != null) {
		// Set trigger
		if (mOldTrigger != null) {
			mTriggerInfo.setTrigger(mNewTrigger);
			mOldTrigger.removeListener(mListener.getId());
		}
		// Add
		else {
			mListener.addTrigger(mTriggerInfo);
		}
		mNewTrigger.addListener(mTriggerInfo);
	}
	// Remove trigger
	else {
		if (mOldTrigger != null) {
			mListener.removeTrigger(mTriggerInfo);
			mOldTrigger.removeListener(mListener.getId());
		}
		// Didn't find the trigger, so can't remove it
		else {
			return false;
		}
	}

	sendOnChange();

	return true;
}

@Override
public boolean undo() {
	// Set or add
	if (mOldTrigger != null) {
		// Set
		if (mNewTrigger != null) {
			mTriggerInfo.setTrigger(mOldTrigger);
			mNewTrigger.removeListener(mListener.getId());
		}
		// Add
		else {
			mTriggerInfo.setTrigger(mOldTrigger);
		}
		mOldTrigger.addListener(mTriggerInfo);
	}
	// Remove
	else {
		if (mNewTrigger != null) {
			mListener.removeTrigger(mTriggerInfo);
			mNewTrigger.removeListener(mListener.getId());
		}
		// Can't remove a trigger that haven't been added
		else {
			return false;
		}
	}

	sendOnChange();

	return true;
}
}
