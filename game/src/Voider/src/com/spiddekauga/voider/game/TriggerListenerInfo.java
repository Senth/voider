package com.spiddekauga.voider.game;

import java.util.UUID;

import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;

/**
 * Listener information for the trigger
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TriggerListenerInfo implements Json.Serializable {
	/** The listener object */
	public ITriggerListener listener;
	/** Id of the listener */
	public UUID listenerId;
	/** The action to take to pass to the listener */
	public TriggerAction.Actions action;
	/** Delay of the trigger in seconds */
	public float delay;


	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#write(com.badlogic.gdx.utils.Json)
	 */
	@Override
	public void write(Json json) {
		json.writeValue("listenerId", listenerId.toString());
		json.writeValue("action", action);
		json.writeValue("delay", delay);
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#read(com.badlogic.gdx.utils.Json, com.badlogic.gdx.utils.OrderedMap)
	 */
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		listenerId = UUID.fromString(json.readValue("listenerId", String.class, jsonData));
		action = json.readValue("action", TriggerAction.Actions.class, jsonData);
		delay = json.readValue("delay", float.class, jsonData);
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		} else if (object.getClass() == this.getClass()) {
			return listenerId.equals(((TriggerListenerInfo)object).listenerId);
		} else if (object instanceof UUID) {
			return listenerId.equals(object);
		}
		return false;
	}
}
