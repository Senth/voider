package com.spiddekauga.voider.resources;


/**
 * Listens to resource change events

 */
public interface IResourceChangeListener extends IResource {
	/**
	 * Called when the resource has changed
	 * @param resource the changed resource
	 * @param type what was changed inside the resource
	 */
	public void onResourceChanged(IResource resource, EventTypes type);

	/**
	 * All different type of change events
	 */
	public enum EventTypes {
		/** Position was changed */
		POSITION,
		/** @deprecated Use game events instead */
		@Deprecated LIFE_DECREASED
	}
}
