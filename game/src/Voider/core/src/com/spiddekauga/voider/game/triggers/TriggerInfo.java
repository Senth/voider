package com.spiddekauga.voider.game.triggers;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;

/**
 * Information for a trigger listener. This contains all the information
 * to bind the listener to a trigger.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class TriggerInfo {
	/** The trigger */
	@Tag(29) public Trigger trigger = null;
	/** The delay of this listener */
	@Tag(30) public float delay = 0;
	/** The action to take when the trigger is triggered */
	@Tag(31) public TriggerAction.Actions action;
	/** The listener object */
	@Tag(32) public ITriggerListener listener;

	/**
	 * Sets the trigger, automatically sets the trigger id
	 * @param trigger new trigger
	 */
	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

	/**
	 * Tests whether this trigger info equals another trigger info in triggedId
	 * and action (i.e. it doesn't test the delay).
	 * @param triggerInfo the other triggerInfo to test against
	 * @return true if triggerId and action is the same in both trigger infos.
	 */
	public boolean sameTriggerAndAction(TriggerInfo triggerInfo) {
		if (triggerInfo == null || action == null || triggerInfo.action == null) {
			return false;
		} else if (trigger.equals(triggerInfo.trigger) && action == triggerInfo.action) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return copy of this object
	 */
	public TriggerInfo copy() {
		TriggerInfo copy = new TriggerInfo();

		copy.action = action;
		copy.delay = delay;
		copy.listener = listener;
		copy.trigger = trigger;

		return copy;
	}

	/**
	 * Gets the specified enemy's trigger for the specified trigger info.
	 * I.e. this will check in all the enmeny triggers until it finds the specified
	 * trigger.
	 * @param listener the enemy to find the TriggerInfo in.
	 * @param searchTriggerInfo the trigger info to search for in the specified enemy
	 * @return TriggerInfo that have the same triggerId and action as the specified trigger.
	 * Null if the trigger info wasn't found inside the enemy.
	 */
	public static TriggerInfo getTriggerInfoByDuplicate(ITriggerListener listener, TriggerInfo searchTriggerInfo) {
		for (TriggerInfo triggerInfo : listener.getTriggerInfos()) {
			if (triggerInfo.sameTriggerAndAction(searchTriggerInfo)) {
				return triggerInfo;
			}
		}

		return null;
	}

	/**
	 * Gets the specified enemy's trigger for the specified trigger info.
	 * I.e. this will check in all the enmeny triggers until it finds the trigger info
	 * with the specified action.
	 * @param listener the enemy to find the TriggerInfo in.
	 * @param action the action the trigger contains.
	 * @return TriggerInfo that have the same triggerId and action as the specified trigger.
	 * null if the trigger info wasn't found inside the listener.
	 */
	public static TriggerInfo getTriggerInfoByAction(ITriggerListener listener, Actions action) {
		for (TriggerInfo triggerInfo : listener.getTriggerInfos()) {
			if (triggerInfo.action == action) {
				return triggerInfo;
			}
		}

		return null;
	}
}
