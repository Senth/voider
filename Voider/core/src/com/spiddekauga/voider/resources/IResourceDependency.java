package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.spiddekauga.voider.repo.resource.InternalNames;

/**
 * This resource has dependencies

 */
public interface IResourceDependency extends IResource {
	/**
	 * @return number of external dependencies
	 */
	public int getExternalDependenciesCount();

	/**
	 * @return number of internal dependencies
	 */
	public int getInternalDependenciesCount();

	/**
	 * Adds an external dependency to the resource
	 * @param dependency the resource dependency
	 * @see #addDependency(InternalDeps)
	 */
	public void addDependency(IResource dependency);

	/**
	 * Adds an internal dependency to the resource
	 * @param dependency the resource dependency
	 * @see #addDependency(IResource)
	 */
	public void addDependency(InternalDeps dependency);

	/**
	 * Removes an external dependency from the resource
	 * @param dependency the id of the dependency to remove
	 */
	public void removeDependency(UUID dependency);

	/**
	 * Removes an internal dependency from the resource
	 * @param dependency the name of the dependency to remove
	 */
	public void removeDependency(InternalDeps dependency);

	/**
	 * Clear internal dependencies
	 */
	public void clearInternalDependencies();

	/**
	 * Clear external dependencies
	 */
	public void clearExternalDependencies();

	/**
	 * @return all external dependencies
	 */
	Map<UUID, AtomicInteger> getExternalDependencies();

	/**
	 * @return all internal dependencies
	 */
	ArrayList<InternalNames> getInternalDependencies();
}
