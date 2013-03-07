package com.spiddekauga.voider.resources;


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
