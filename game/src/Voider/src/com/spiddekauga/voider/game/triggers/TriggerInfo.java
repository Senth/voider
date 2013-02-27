package com.spiddekauga.voider.game.triggers;

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

	/**
	 * Tests whether this trigger info equals another trigger info in triggedId
	 * and action (i.e. it doesn't test the delay).
	 * @param triggerInfo the other triggerInfo to test against
	 * @return true if triggerId and action is the same in both trigger infos.
	 */
	public boolean sameTriggerAndAction(TriggerInfo triggerInfo) {
		if (triggerInfo == null || triggerId == null || action == null ||
				triggerInfo.triggerId == null || triggerInfo.action == null) {
			return false;
		} else if (triggerId.equals(triggerInfo.triggerId) && action == triggerInfo.action) {
			return true;
		} else {
			return false;
		}
	}

}
