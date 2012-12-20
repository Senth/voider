package com.spiddekauga.voider.game;

import java.util.UUID;

import com.spiddekauga.voider.resources.Resource;

/**
 * Base class for all triggers
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 * @TODO set to abstract
 */
public class Trigger extends Resource {
	/**
	 * Default constructor for the trigger. Creates a new unique id
	 */
	public Trigger() {
		mUniqueId = UUID.randomUUID();
	}

	/**
	 * Updates the trigger, i.e. checks if it shall send a trigger event
	 * to the listeners
	 * @TODO set to abstract
	 */
	public void update() {

	}
}
