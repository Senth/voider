package com.spiddekauga.voider.utils.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Dispatches events to all listeners
 */
public class EventDispatcher {
private static EventDispatcher mInstance = null;
private List<List<IEventListener>> mListeners = new ArrayList<>();

/**
 * Private constructor to enforce singleton usage
 */
private EventDispatcher() {
	for (@SuppressWarnings("unused")
			EventTypes eventType : EventTypes.values()) {
		mListeners.add(new ArrayList<IEventListener>());
	}
}

/**
 * @return instance of this event dispatcher
 */
public static EventDispatcher getInstance() {
	if (mInstance == null) {
		mInstance = new EventDispatcher();
	}
	return mInstance;
}

/**
 * Connect a listener to an event
 * @param eventType the type of event to connect to
 * @param listener the listener to connect
 */
public void connect(EventTypes eventType, IEventListener listener) {
	mListeners.get(eventType.ordinal()).add(listener);
}

/**
 * Disconnect a listener from all event types. This is more computational heavy. Better to use
 * {@link #disconnect(EventTypes, IEventListener)} if you know which events has been connected to
 * this listener.
 * @param listener the listener to disconnect from all events
 */
public void disconnect(IEventListener listener) {
	for (EventTypes eventType : EventTypes.values()) {
		disconnect(eventType, listener);
	}
}

/**
 * Disconnect a listener from an event
 * @param eventType the type of event to disconnect from
 * @param listener the listener to disconnect
 */
public void disconnect(EventTypes eventType, IEventListener listener) {
	mListeners.get(eventType.ordinal()).remove(listener);
}

/**
 * Fire an event
 * @param event the event to fire
 */
public void fire(GameEvent event) {
	ArrayList<IEventListener> copy = new ArrayList<>();
	copy.addAll(mListeners.get(event.type.ordinal()));

	for (IEventListener listeners : copy) {
		listeners.handleEvent(event);
	}
}
}
