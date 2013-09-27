package com.spiddekauga.voider.resources;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This resource has dependencies
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
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
	 * @see #addDependency(ResourceNames)
	 */
	public void addDependency(IResource dependency);

	/**
	 * Adds an internal dependency to the resource
	 * @param dependency the resource dependency
	 * @see #addDependency(IResource)
	 */
	public void addDependency(ResourceNames dependency);

	/**
	 * Removes an external dependency from the resource
	 * @param dependency the id of the dependency to remove
	 */
	public void removeDependency(UUID dependency);

	/**
	 * Removes an internal dependency from the resource
	 * @param dependency the name of the dependency to remove
	 */
	public void removeDependency(ResourceNames dependency);

	/**
	 * @return all external dependencies
	 */
	Map<UUID, AtomicInteger> getExternalDependencies();

	/**
	 * @return all internal dependencies
	 */
	Set<ResourceNames> getInternalDependencies();
}
