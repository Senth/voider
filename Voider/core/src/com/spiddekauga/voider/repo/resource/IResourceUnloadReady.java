package com.spiddekauga.voider.repo.resource;

/**
 * Checks if a resource is ready to unload
 */
interface IResourceUnloadReady {
/**
 * Checks if the resource is ready to unload
 * @param resource the resource to check if it's ready to unload
 * @return true if it's ready to unload
 */
boolean isReadyToUnload(Object resource);
}
