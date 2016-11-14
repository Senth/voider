package com.spiddekauga.voider.resources;

import java.util.List;
import java.util.UUID;

import com.spiddekauga.utils.commands.Command;

/**
 * Holds a unique id, can be used as a reference for others and can hold references to
 * other objects that needs to be bound after loading

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
	 * Removes a bound resources from this resource. This method should not actually
	 * unbind the resource but add a command to the commands list.
	 * @param boundResource the resource to remove
	 * @param commands list of commands to unbound the resources, also used to undo
	 *        unbounds.
	 */
	void removeBoundResource(IResource boundResource, List<Command> commands);

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
