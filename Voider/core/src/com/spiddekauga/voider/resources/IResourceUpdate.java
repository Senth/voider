package com.spiddekauga.voider.resources;

/**
 * A resource that needs updating
 *

 */
public interface IResourceUpdate {
	/**
	 * Updates the resource
	 * @param deltaTime seconds elapsed since last call
	 */
	public void update(float deltaTime);
}
