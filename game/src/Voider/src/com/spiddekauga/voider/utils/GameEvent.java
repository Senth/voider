package com.spiddekauga.voider.utils;

/**
 * Game event that was sent
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class GameEvent {
	/** Event type */
	public Events type;

	/**
	 * Sets the event type
	 * @param type
	 */
	public GameEvent(Events type) {
		this.type = type;
	}

	/**
	 * All the different game events
	 */
	public enum Events {
		/** User logged in */
		USER_LOGIN,
		/** User logged out */
		USER_LOGOUT,
		/** Camera zoom was changed */
		CAMERA_ZOOM_CHANGE,
	}
}
