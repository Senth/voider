package com.spiddekauga.voider.resources;

/**
 * A resource that needs updating
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResourceUpdate {
	/**
	 * Updates the resource
	 * @param deltaTime seconds elapsed since last call
	 */
	public void update(float deltaTime);
}
