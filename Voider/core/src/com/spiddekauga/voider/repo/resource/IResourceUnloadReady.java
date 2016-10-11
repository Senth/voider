package com.spiddekauga.voider.repo.resource;

/**
 * Checks if a resource is ready to unload
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
interface IResourceUnloadReady {
	/**
	 * Checks if the resource is ready to unload
	 * @param resource the resource to check if it's ready to unload
	 * @return true if it's ready to unload
	 */
	boolean isReadyToUnload(Object resource);
}
