package com.spiddekauga.voider.game;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.voider.resources.Resource;

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
	void addListener(ITriggerListener listener, float delay, TriggerAction.Actions action) {
		TriggerListenerInfo triggerListenerInfo = new TriggerListenerInfo();
		triggerListenerInfo.listener = listener;
		triggerListenerInfo.delay = delay;
		triggerListenerInfo.action = action;

		mListeners.add(triggerListenerInfo);
	}

	/**
	 * Removes/Clears all the listeners
	 */
	void clearListeners() {
		mListeners.clear();
	}

	/** Listener information about the trigger */
	ArrayList<TriggerListenerInfo> mListeners = new ArrayList<TriggerListenerInfo>();
}
