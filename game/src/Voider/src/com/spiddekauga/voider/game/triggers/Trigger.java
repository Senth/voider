package com.spiddekauga.voider.game.triggers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import com.spiddekauga.voider.game.ITriggerListener;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * Base class for all triggers
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Trigger extends Resource {
	/**
	 * Default constructor for the trigger. Creates a new unique id
	 */
	public Trigger() {
		mUniqueId = UUID.randomUUID();
	}

	/**
	 * Updates the trigger, i.e. checks if it shall send a trigger event
	 * to the listeners
	 */
	public void update() {
		float totalTimeElapsed = SceneSwitcher.getGameTime().getTotalTimeElapsed();

		if (!mTriggered) {
			if (isTriggered()) {
				mTriggered = true;
				mTriggeredTime = totalTimeElapsed;
			}
		}

		if (mTriggered) {
			float timeSinceTriggered = totalTimeElapsed - mTriggeredTime;

			// Trigger and remove all triggers which delays have run out
			Iterator<TriggerListenerInfo> iterator = mListeners.iterator();
			while (iterator.hasNext()) {
				TriggerListenerInfo triggerListenerInfo = iterator.next();

				if (timeSinceTriggered >= triggerListenerInfo.delay) {
					TriggerAction triggerAction = new TriggerAction();
					triggerAction.action = triggerListenerInfo.action;
					triggerAction.reason = getReason();
					triggerAction.causeObject = getCauseObject();
					triggerListenerInfo.listener.onTriggered(triggerAction);

					iterator.remove();
				}
			}
		}
	}

	/**
	 * Checks if the trigger has triggered all listeners. You can safetly remove
	 * the trigger now
	 * @return true if all listeners have been triggered
	 */
	public boolean hasAllTriggered() {
		return mListeners.isEmpty();
	}

	/**
	 * @return the reason for the trigger
	 */
	protected abstract TriggerAction.Reasons getReason();

	/**
	 * @return object that caused the trigger to trigger
	 */
	protected abstract Object getCauseObject();

	/**
	 * Checks if the trigger is triggered
	 * @return true if the trigger has triggered
	 */
	protected abstract boolean isTriggered();

	/**
	 * Adds a listener to the trigger
	 * @param listener the listener to add
	 * @param delay how many seconds after the trigger has been trigger the actual
	 * #onTriggered(Actions) is called.
	 * @param action the action to take when triggered
	 */
	public void addListener(ITriggerListener listener, float delay, TriggerAction.Actions action) {
		TriggerListenerInfo triggerListenerInfo = new TriggerListenerInfo();
		triggerListenerInfo.listener = listener;
		triggerListenerInfo.delay = delay;
		triggerListenerInfo.action = action;

		mListeners.add(triggerListenerInfo);
	}

	/**
	 * Removes/Clears all the listeners
	 */
	public void clearListeners() {
		mListeners.clear();
	}

	/** If the trigger has been triggered, this is used to avoid heavy calculations */
	private boolean mTriggered = false;
	/** Triggered time */
	private float mTriggeredTime = -1;
	/** Listener information about the trigger */
	private ArrayList<TriggerListenerInfo> mListeners = new ArrayList<TriggerListenerInfo>();
}
