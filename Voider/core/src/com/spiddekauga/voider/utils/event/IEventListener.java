package com.spiddekauga.voider.utils.event;

/**
 * Listener for game events
 */
public interface IEventListener {
/**
 * Called when an event is fired
 * @param event the event that was fired
 */
void handleEvent(GameEvent event);
}
