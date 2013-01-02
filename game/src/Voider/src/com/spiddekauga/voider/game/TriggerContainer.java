package com.spiddekauga.voider.game;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;

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
		LinkedList<UUID> triggersToRemove = new LinkedList<UUID>();

		for (ObjectMap.Entry<UUID, Array<TriggerListenerInfo>> entry : mTriggerListeners.entries()) {
			if (entry.value.size == 0) {
				triggersToRemove.add(entry.key);
			}
		}

		for (UUID triggerId : triggersToRemove) {
			mTriggerListeners.remove(triggerId);
			mTriggers.remove(triggerId);
		}
	}

	/**
	 * Adds a trigger
	 * @param trigger the trigger to add
	 */
	public void addTrigger(Trigger trigger) {
		if (!mTriggers.containsKey(trigger.getId())) {
			mTriggers.put(trigger.getId(), trigger);

			// Set common array of listeners both for this container and the trigger
			Array<TriggerListenerInfo> listeners = new Array<TriggerListenerInfo>(false, 1);
			trigger.setListeners(listeners);
			mTriggerListeners.put(trigger.getId(), listeners);
		} else {
			Gdx.app.error("Trigger", "Tried to add a trigger twice: " + trigger.getId());
		}
	}

	/**
	 * Removes a trigger and unbinds all actors that are listening to this trigger
	 * @param trigger the trigger to remove
	 */
	public void removeTrigger(Trigger trigger) {
		Object removedObject = mTriggers.remove(trigger.getId());
		if (removedObject == null) {
			Gdx.app.error("Trigger", "Could not find the trigger to remove ");
		}
		Array<TriggerListenerInfo> listeners = mTriggerListeners.remove(trigger.getId());
		if (listeners != null) {
			for (TriggerListenerInfo listenerInfo : listeners) {
				removeTriggerFromListener(trigger.getId(), listenerInfo.listenerId);
			}
		} else {
			Gdx.app.error("Trigger", "Could not find the trigger with listener to remove");
		}
	}

	/**
	 * Adds a listener to an existing trigger
	 * @param triggerId the trigger to add the listener to
	 * @param listenerInfo the listener with some information
	 */
	public void addListener(UUID triggerId, TriggerListenerInfo listenerInfo) {
		// Pre-checks to see that the listener info is valid
		if (listenerInfo.listener == null) {
			Gdx.app.error("Trigger", "ListenerInfo listener was null when adding listener!");
			return;
		} else if (listenerInfo.listenerId == null) {
			Gdx.app.error("Trigger", "ListenerInfo listenerId was null when adding listener!");
			return;
		}


		// Add listener to the trigger
		Array<TriggerListenerInfo> listeners = mTriggerListeners.get(triggerId);
		if (listeners != null) {
			listeners.add(listenerInfo);
		} else {
			Gdx.app.error("Trigger", "Trigger does not exist!");
			return;
		}


		// Add trigger to the listener's triggers
		Array<UUID> listenersTriggers = mListenerTriggers.get(listenerInfo.listenerId);

		// Did not exist yet, create it
		if (listenersTriggers == null) {
			listenersTriggers = new Array<UUID>(false, 1);
			mListenerTriggers.put(listenerInfo.listenerId, listenersTriggers);
		}

		// Add the trigger
		listenersTriggers.add(triggerId);
	}

	/**
	 * Removes a listener from all triggers
	 * @param listenerId the listener to remove
	 */
	public void removeListener(UUID listenerId) {
		Array<UUID> triggers = mListenerTriggers.remove(listenerId);
		if (triggers != null) {
			for (UUID triggerId : triggers) {
				Array<TriggerListenerInfo> listeners = mTriggerListeners.get(triggerId);
				if (listeners != null) {
					Iterator<TriggerListenerInfo> iterator = listeners.iterator();
					while (iterator.hasNext()) {
						if (iterator.next().listenerId.equals(listenerId)) {
							iterator.remove();
							break;
						}
					}
				} else {
					Gdx.app.error("Trigger", "Could not find the listeners for a trigger when removing a listener");
				}
			}
		} else {
			Gdx.app.error("Trigger", "Could not find the listener to remove");
		}
	}

	/**
	 * Remove a listener from a trigger
	 * @param triggerId the trigger to remove the listener from
	 * @param listenerId the listener to remove
	 */
	public void removeListener(UUID triggerId, UUID listenerId) {
		// Remove listener from triggers
		Array<TriggerListenerInfo> listeners = mTriggerListeners.get(triggerId);
		if (listeners != null) {
			int i = 0;
			boolean found = false;
			while (!found && i < listeners.size) {
				if (listeners.get(i).listenerId.equals(listenerId)) {
					listeners.removeIndex(i);
					found = true;
				}
				++i;
			}
			if (!found) {
				Gdx.app.error("Trigger", "Did not find listener in trigger when removing listener");
			}

		} else {
			Gdx.app.error("Trigger", "Did not find the trigger when removing a listener");
		}


		// Remove trigger from the listener
		Array<UUID> triggers = mListenerTriggers.get(listenerId);
		if (triggers != null) {
			int i = 0;
			boolean found = false;
			while (!found && i < triggers.size) {
				if (triggers.get(i).equals(triggerId)) {
					triggers.removeIndex(i);
					found = true;
				}
				++i;
			}
			if (!found) {
				Gdx.app.error("Trigger", "Did not find trigger in listener, when removing listener");
			}

			// No more triggers in this listener, remove it entirely
			if (triggers.size == 0) {
				mListenerTriggers.remove(listenerId);
			}
		} else {
			Gdx.app.error("Trigger", "Did not find the listener when removing a listener");
		}
	}

	/**
	 * Binds together all actors and triggers. This should be called whenever
	 * this class have been loaded. Can generate an error if
	 * a listener which have been specified by uuid can't be found in the listeners
	 * parameter.
	 * @param listeners a list of listeners. This list contains possible listeners
	 * as not all might be listening to a trigger.
	 */
	public void bindTriggers(ObjectMap<UUID, ITriggerListener> listeners) {
		// Go through all saved trigger listeners and try to find the listeners
		for (ObjectMap.Entry<UUID, Array<TriggerListenerInfo>> listenerInfos : mTriggerListeners.entries()) {
			for (TriggerListenerInfo listenerInfo : listenerInfos.value) {

				// Set the reference to the listener
				ITriggerListener listener = listeners.get(listenerInfo.listenerId);
				if (listener != null) {
					listenerInfo.listener = listener;
				} else {
					Gdx.app.error("Trigger", "Could not find the trigger for the listener");
				}
			}
		}
	}


	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#write(com.badlogic.gdx.utils.Json)
	 */
	@Override
	public void write(Json json) {
		json.writeValue("mTriggerListeners", mTriggerListeners);
		json.writeValue("mTriggers", mTriggers);
		json.writeValue("mListenerTriggers", mListenerTriggers);
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#read(com.badlogic.gdx.utils.Json, com.badlogic.gdx.utils.OrderedMap)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		ObjectMap<?, ?> triggerListeners = json.readValue("mTriggerListeners", ObjectMap.class, jsonData);
		mTriggerListeners = (ObjectMap<UUID, Array<TriggerListenerInfo>>) triggerListeners;
		ObjectMap<?, ?> triggers = json.readValue("mTriggers", ObjectMap.class, jsonData);
		mTriggers = (ObjectMap<UUID, Trigger>)triggers;
		ObjectMap<?, ?> listenerTriggers = json.readValue("mTriggers", ObjectMap.class, jsonData);
		mListenerTriggers = (ObjectMap<UUID, Array<UUID>>)listenerTriggers;


		// Set the listeners for the triggers
		for (ObjectMap.Entry<UUID, Trigger> trigger : mTriggers.entries()) {
			trigger.value.setListeners(mTriggerListeners.get(trigger.key));
		}
	}

	/**
	 * Removes a trigger from a listener
	 * @param triggerId the trigger to remove
	 * @param listenerId the listener id to remove it from
	 */
	private void removeTriggerFromListener(UUID triggerId, UUID listenerId) {
		Array<UUID> triggers = mListenerTriggers.get(listenerId);
		if (triggers != null) {
			boolean removed = triggers.removeValue(triggerId, false);
			if (!removed) {
				Gdx.app.error("Trigger", "Could not find the trigger in the listener to remove it from");
			}

			if (triggers.size == 0) {
				mListenerTriggers.remove(listenerId);
			}
		} else {
			Gdx.app.error("Trigger", "Could not find triggers for listener that was removed");
		}
	}


	/** Helper for finding all the triggers of a listener */
	private ObjectMap<UUID, Array<UUID>> mListenerTriggers = new ObjectMap<UUID, Array<UUID>>();
	/** All triggers */
	private ObjectMap<UUID, Trigger> mTriggers = new ObjectMap<UUID, Trigger>();
	/** collection where each element is an array of the trigger's listeners, only this is saved */
	private ObjectMap<UUID, Array<TriggerListenerInfo>> mTriggerListeners = new ObjectMap<UUID, Array<TriggerListenerInfo>>();
}
