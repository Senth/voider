package com.spiddekauga.voider.game;

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
