package com.spiddekauga.utils.commands;

import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Connect a listener to one or more events
 */
public class CEventConnect extends Command {
private final static EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
private IEventListener mListener;
private EventTypes[] mTypes;

/**
 * Creates a command that connects events to a certain listener
 * @param listener the listener to connect the events to
 * @param types all the event types to listen to
 */
public CEventConnect(IEventListener listener, EventTypes... types) {
	mListener = listener;
	mTypes = types;
}

@Override
public boolean execute() {
	for (EventTypes type : mTypes) {
		mEventDispatcher.connect(type, mListener);
	}

	return true;
}

@Override
public boolean undo() {
	for (EventTypes type : mTypes) {
		mEventDispatcher.disconnect(type, mListener);
	}

	return true;
}
}
