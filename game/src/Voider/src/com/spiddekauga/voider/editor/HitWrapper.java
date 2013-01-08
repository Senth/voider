package com.spiddekauga.voider.editor;

import com.spiddekauga.voider.game.Actor;

/**
 * Wraps an Actor together with some arbitrary data for handling hits.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class HitWrapper {
	/**
	 * Constructor with an actor
	 * @param actor the actor that should be wrapped
	 */
	public HitWrapper(Actor actor) {
		this.actor = actor;
	}

	/**
	 * Creates hit wrapper with optional arbitrary data
	 * @param actor the actor that should be wrapped
	 * @param data the data to wrap with this actor
	 */
	public HitWrapper(Actor actor, Object data) {
		this.actor = actor;
		this.data = data;
	}


	/** Actor together with hit */
	public Actor actor;
	/** Arbitrary data */
	public Object data = null;
}
