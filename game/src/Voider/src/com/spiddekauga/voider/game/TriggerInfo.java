package com.spiddekauga.voider.game;

import java.util.UUID;

/**
 * Information for a trigger listener. This contains all the information
 * to bind the listener to a trigger.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TriggerInfo {
	/** The trigger this is bound to */
	public UUID triggerId;
	/** The delay of this listener */
	public float delay;
	/** The action to take when the trigger is triggered */
	public TriggerAction.Actions action;
}
