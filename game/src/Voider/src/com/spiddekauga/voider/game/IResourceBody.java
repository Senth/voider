package com.spiddekauga.voider.game;

import com.spiddekauga.voider.resources.IResource;

/**
 * A Resource that has a body
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResourceBody extends IResource {
	/**
	 * Creates the body of the resource
	 */
	public void createBody();

	/**
	 * Destroys the body of the resource
	 */
	public void destroyBody();
}
