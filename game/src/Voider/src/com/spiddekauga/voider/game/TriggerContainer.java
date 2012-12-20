package com.spiddekauga.voider.game;

import java.util.UUID;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * Contains all the triggers for a level
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class TriggerContainer implements Json.Serializable{

	/**
	 * Updates all the triggers
	 */
	public void update() {
		for (ObjectMap.Entry<UUID, Trigger> trigger : mTriggers.entries()) {
			trigger.value.update();
		}
	}

	/**
	 * Removes all unused triggers
	 */
	public void removeUnusedTriggers() {
		/** @TODO implement removeUnusedTriggers() */
	}

	/**
	 * Adds a trigger
	 * @param trigger the trigger to add
	 */
	public void addTrigger(Trigger trigger) {
		/** @TODO implement addTrigger() */
	}

	/**
	 * Removes a trigger and unbinds all actors that are listening to this trigger
	 * @param trigger the trigger to remove
	 */
	public void removeTrigger(Trigger trigger) {
		/** @TODO implement removeTrigger() */
	}

	/**
	 * Adds a listener to an existing trigger
	 * @param triggerId the trigger to add the listener to
	 * @param listenerInfo the listener with some information
	 */
	public void addListener(UUID triggerId, TriggerListenerInfo listenerInfo) {
		/** @TODO implement addListener() */
	}

	/**
	 * Remove a listener from a trigger
	 * @param triggerId the trigger to remove the listener from
	 * @param listenerId the listener to remove
	 */
	public void removeListener(UUID triggerId, UUID listenerId) {
		/** @TODO implement removeListener() */
	}

	/**
	 * Binds together all actors and triggers. This should be called whenever
	 * this class have been loaded. Can generate an error if
	 * a listener which have been specified by uuid can't be found in the listeners
	 * parameter.
	 * @param listeners a list of listeners. This list contains possible listeners
	 * as not all might be listening to a trigger.
	 */
	public void bindTriggers(Array<ITriggerListener> listeners) {
		/** @TODO implement bindTriggers() */
	}


	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#write(com.badlogic.gdx.utils.Json)
	 */
	@Override
	public void write(Json json) {
		json.writeValue("mTriggerListeners", mTriggerListeners);
	}
	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#read(com.badlogic.gdx.utils.Json, com.badlogic.gdx.utils.OrderedMap)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		ObjectMap<?, ?> triggerListeners = json.readValue("mTriggerListeners", ObjectMap.class, jsonData);
		mTriggerListeners = (ObjectMap<UUID, Array<TriggerListenerInfo>>) triggerListeners;
	}


	/** Helper for finding the actor triggers */
	private ObjectMap<Actor, Array<Trigger>> mActorTriggers = new ObjectMap<Actor, Array<Trigger>>();
	/** All triggers */
	private ObjectMap<UUID, Trigger> mTriggers = new ObjectMap<UUID, Trigger>();
	/** collection where each element is an array of the trigger's listeners, only this is saved */
	private ObjectMap<UUID, Array<TriggerListenerInfo>> mTriggerListeners = new ObjectMap<UUID, Array<TriggerListenerInfo>>();
}
