package com.spiddekauga.voider.game;

/**
 * Wrapper for trigger actions sent to the listener
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TriggerAction {
	/** The action the listener shall take */
	public Actions action = null;
	/** The reason why a trigger was triggered */
	public Reasons reason = null;
	/** The object that caused the trigger */
	public Object causeObject = null;

	/**
	 * All the different trigger actions
	 */
	public enum Actions {
		/** Activates an actor */
		ACTOR_ACTIVATE,
		/** Creates an actor */
		ACTOR_CREATE,
	}

	/**
	 * All the reasons for why a trigger is triggered
	 */
	public enum Reasons {
		/** When an actor dies */
		ACTOR_DIED,
		/** When the screen passed X-coordinate on the level */
		SCREEN_AT,
	}
}
