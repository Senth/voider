package com.spiddekauga.voider.utils.event;

/**
 * Game event that was sent
 */
public class GameEvent {
/** Event type */
public EventTypes type;

/**
 * Sets the event type
 * @param type
 */
public GameEvent(EventTypes type) {
	this.type = type;
}
}
