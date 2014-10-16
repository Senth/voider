package com.spiddekauga.voider.utils.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.spiddekauga.voider.utils.Pools;


/**
 * Dispatches events to all listeners
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class EventDispatcher {
	/**
	 * Private constructor to enforce singleton usage
	 */
	private EventDispatcher() {
		for (@SuppressWarnings("unused")
		EventTypes eventType : EventTypes.values()) {
			mListeners.add(new HashSet<IEventListener>());
		}
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
	 * Disconnect a listener from an event
	 * @param eventType the type of event to disconnect from
	 * @param listener the listener to disconnect
	 */
	public void disconnect(EventTypes eventType, IEventListener listener) {
		mListeners.get(eventType.ordinal()).remove(listener);
	}

	/**
	 * Disconnect a listener from all event types. This is more computational heavy.
	 * Better to use {@link #disconnect(EventTypes, IEventListener)} if you know which
	 * events has been connected to this listener.
	 * @param listener the listener to disconnect from all events
	 */
	public void disconnect(IEventListener listener) {
		for (EventTypes eventType : EventTypes.values()) {
			disconnect(eventType, listener);
		}
	}

	/**
	 * Fire an event
	 * @param event the event to fire
	 */
	public void fire(GameEvent event) {
		@SuppressWarnings("unchecked")
		HashSet<IEventListener> copy = Pools.hashSet.obtain();
		copy.addAll(mListeners.get(event.type.ordinal()));

		for (IEventListener listeners : copy) {
			listeners.handleEvent(event);
		}

		Pools.hashSet.free(copy);


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


	private List<Set<IEventListener>> mListeners = new ArrayList<>();
	private static EventDispatcher mInstance = null;
}
