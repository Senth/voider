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

	//	/**
	//	 * @return revision of the resource. This will actually be -1
	//	 * if the resource isn't an istance of IResourceRevision!
	//	 */
	//	public int getRevision();

	/**
	 * Returns all references this resource uses
	 * @param references all resources this resource uses
	 */
	@Deprecated
	public void getReferences(ArrayList<UUID> references);

	/**
	 * Binds the specific reference to this resource
	 * @param resource the resource this class uses which needs to be bound to this resource
	 * @return true if the resource was bound
	 */
	@Deprecated
	public boolean bindReference(IResource resource);

	/**
	 * Removes a bound resource
	 * @param boundResource the resource to remove
	 * @return true if the resource was removed successfully
	 */
	@Deprecated
	public boolean removeBoundResource(IResource boundResource);

	/**
	 * Adds a resource to be bound
	 * @param boundResource the resource to add and bind
	 * @return true if the resource was added successfully
	 */
	@Deprecated
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
