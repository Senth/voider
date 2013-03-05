package com.spiddekauga.voider.game.triggers;

import java.util.UUID;

import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;

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
	public float delay;
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

	@Override
	public void write(Json json) {
		json.writeValue("triggerId", triggerId);
		json.writeValue("action", action);
		json.writeValue("delay", delay);
	}

	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		action = json.readValue("action", TriggerAction.Actions.class, jsonData);
		delay = json.readValue("delay", float.class, jsonData);
		triggerId = json.readValue("triggerId", UUID.class, jsonData);
	}
}
