package com.spiddekauga.voider.game.triggers;

import java.util.UUID;

import com.badlogic.gdx.utils.JsonValue;
import com.spiddekauga.utils.JsonWrapper; import com.badlogic.gdx.utils.Json;
import com.spiddekauga.voider.game.triggers.TriggerAction.Actions;

/**
 * Information for a trigger listener. This contains all the information
 * to bind the listener to a trigger.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TriggerInfo implements Json.Serializable {
	/** The trigger this is bound to */
	public UUID triggerId;
	/** The trigger */
	public Trigger trigger = null;
	/** The delay of this listener */
	public float delay = 0;
	/** The action to take when the trigger is triggered */
	public TriggerAction.Actions action;
	/** The listener object */
	public ITriggerListener listener;

	/**
	 * Sets the trigger, automatically sets the trigger id
	 * @param trigger new trigger
	 */
	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
		if (trigger != null) {
			triggerId = trigger.getId();
		} else {
			triggerId = null;
		}
	}

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

	/**
	 * @return copy of this object
	 */
	public TriggerInfo copy() {
		TriggerInfo copy = new TriggerInfo();

		copy.action = action;
		copy.delay = delay;
		copy.listener = listener;
		copy.triggerId = triggerId;
		copy.trigger = trigger;

		return copy;
	}

	@Override
	public void write(Json json) {
		json.writeValue("triggerId", triggerId);
		json.writeValue("action", action);
		json.writeValue("delay", delay);
	}

	@Override
	public void read(Json json, JsonValue jsonValue) {
		action = json.readValue("action", TriggerAction.Actions.class, jsonValue);
		delay = json.readValue("delay", float.class, jsonValue);
		triggerId = json.readValue("triggerId", UUID.class, jsonValue);
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
		for (TriggerInfo enemyTriggerInfo : listener.getTriggerInfos()) {
			if (enemyTriggerInfo.sameTriggerAndAction(searchTriggerInfo)) {
				return enemyTriggerInfo;
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
	 * Null if the trigger info wasn't found inside the enemy.
	 */
	public static TriggerInfo getTriggerInfoByAction(ITriggerListener listener, Actions action) {
		for (TriggerInfo enemyTriggerInfo : listener.getTriggerInfos()) {
			if (enemyTriggerInfo.action == action) {
				return enemyTriggerInfo;
			}
		}

		return null;
	}
}
