package com.spiddekauga.voider.resources;

import java.util.UUID;

/**
 * Holds a unique id, can be used as a reference for others and can hold references to
 * other objects that needs to be bound after loading
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IResource {
	/**
	 * @return a unique id for the type
	 */
	UUID getId();

	/**
	 * Explicitly set the id of the resource
	 * @param id use this resource id instead
	 */
	void setId(UUID id);

	/**
	 * Removes a bound resource
	 * @param boundResource the resource to remove
	 * @return true if the resource could be removed
	 */
	boolean removeBoundResource(IResource boundResource);

	/**
	 * Adds a resource to be bound
	 * @param boundResource the resource to add and bind
	 * @return true if the resource was added successfully
	 */
	boolean addBoundResource(IResource boundResource);

	/**
	 * Adds an on changed listener to the resource. This listener will be called whenever
	 * the resource will be changed.
	 * @param listener listens to changes in this resource
	 */
	void addChangeListener(IResourceChangeListener listener);

	/**
	 * Removes an on changed listener from this resource.
	 * @param listener listener to remove from this resource, i.e. it won't listen to
	 *        change events in this resource any longer.
	 */
	void removeChangeListener(IResourceChangeListener listener);
}
