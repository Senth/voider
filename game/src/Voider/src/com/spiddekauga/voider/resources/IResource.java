package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Holds a unique id, can be used as a reference for others and can hold references
 * to other objects that needs to be bound after loading
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResource {
	/**
	 * @return a unique id for the type
	 */
	public UUID getId();

	/**
	 * Returns all references this resource uses
	 * @param references all resources this resource uses
	 */
	public void getReferences(ArrayList<UUID> references);

	/**
	 * Binds the specific reference to this resource
	 * @param resource the resource this class uses which needs to be bound to this resource
	 */
	public void bindReference(IResource resource);
}
