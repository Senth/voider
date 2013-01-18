package com.spiddekauga.voider.game;

import java.util.UUID;

import com.badlogic.gdx.utils.Array;
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
	 * Sets the listener array
	 * @param listeners array of listeners
	 */
	void setListeners(Array<TriggerListenerInfo> listeners) {
		mListeners = listeners;
	}

	/** Listener information about the trigger */
	Array<TriggerListenerInfo> mListeners = null;
}
