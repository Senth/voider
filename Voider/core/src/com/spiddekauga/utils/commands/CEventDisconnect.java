package com.spiddekauga.utils.commands;

import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Disconnects a listener from one or more events.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CEventDisconnect extends Command {
	/**
	 * Creates a command that disconnects events from a certain listener
	 * @param listener the listener to connect the events to
	 * @param types all the event types to listen to. If no types are specified all events
	 *        are removed; note that in this case undo won't work properly.
	 */
	public CEventDisconnect(IEventListener listener, EventTypes... types) {
		mListener = listener;
		mTypes = types;
	}

	@Override
	public boolean execute() {
		if (mTypes.length > 0) {
			for (EventTypes type : mTypes) {
				mEventDispatcher.disconnect(type, mListener);
			}
		} else {
			mEventDispatcher.disconnect(mListener);
		}

		return true;
	}

	@Override
	public boolean undo() {
		for (EventTypes type : mTypes) {
			mEventDispatcher.connect(type, mListener);
		}

		return true;
	}

	private final static EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
	private IEventListener mListener;
	private EventTypes[] mTypes;
}
