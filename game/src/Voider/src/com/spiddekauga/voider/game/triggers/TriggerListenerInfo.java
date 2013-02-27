package com.spiddekauga.voider.game.triggers;

import com.spiddekauga.voider.game.ITriggerListener;


/**
 * Listener information for the trigger
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TriggerListenerInfo {
	/** The listener object */
	public ITriggerListener listener;
	/** The action to take to pass to the listener */
	public TriggerAction.Actions action;
	/** Delay of the trigger in seconds */
	public float delay;
}
