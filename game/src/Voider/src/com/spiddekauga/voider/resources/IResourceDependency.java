package com.spiddekauga.voider.resources;

import java.util.Set;
import java.util.UUID;

import com.badlogic.gdx.utils.ObjectMap;

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
	 * Adds an external dependency to the resource
	 * @param uuid the unique id of the dependency
	 * @param type the type of dependency
	 */
	public void addDependency(UUID uuid, Class<?> type);

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
	ObjectMap<UUID, ResourceItem> getExternalDependencies();

	/**
	 * @return all internal dependencies
	 */
	Set<ResourceNames> getInternalDependencies();
}
