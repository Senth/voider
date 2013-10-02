package com.spiddekauga.voider.resources;

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
	 * Removes a bound resource
	 * @param boundResource the resource to remove
	 * @return true if the resource could be removed
	 */
	public boolean removeBoundResource(IResource boundResource);

	/**
	 * Adds a resource to be bound
	 * @param boundResource the resource to add and bind
	 * @return true if the resource was added successfully
	 */
	public boolean addBoundResource(IResource boundResource);

	/**
	 * Adds an on changed listener to the resource. This listener will be called
	 * whenever the resource will be changed.
	 * @param listener listens to changes in this resource
	 */
	public void addChangeListener(IResourceChangeListener listener);

	/**
	 * Removes an on changed listener from this resource.
	 * @param listener listener to remove from this resource, i.e. it won't listen
	 * to change events in this resource any longer.
	 */
	public void removeChangeListener(IResourceChangeListener listener);
}
