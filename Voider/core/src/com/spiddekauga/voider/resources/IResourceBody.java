package com.spiddekauga.voider.resources;


/**
 * A Resource that has a body

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

	/**
	 * @return true if a body has been created
	 */
	public boolean hasBody();
}
