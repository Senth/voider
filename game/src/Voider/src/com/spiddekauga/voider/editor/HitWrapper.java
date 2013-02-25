package com.spiddekauga.voider.editor;

import com.spiddekauga.voider.resources.IResource;

/**
 * Wraps a resource together with some arbitrary data for handling hits.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class HitWrapper {
	/**
	 * Constructor with an actor
	 * @param actor the actor that should be wrapped
	 */
	public HitWrapper(IResource actor) {
		this.resource = actor;
	}

	/**
	 * Creates hit wrapper with optional arbitrary data
	 * @param actor the actor that should be wrapped
	 * @param data the data to wrap with this actor
	 */
	public HitWrapper(IResource actor, Object data) {
		this.resource = actor;
		this.data = data;
	}


	/** IResource together with hit */
	public IResource resource;
	/** Arbitrary data */
	public Object data = null;
}
