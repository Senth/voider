package com.spiddekauga.voider.resources;

import com.spiddekauga.voider.repo.resource.InternalNames;

/**
 * Internal resource

 */
interface IInternalResource {
	/**
	 * @return true if this resource should be used
	 */
	boolean useResource();

	/**
	 * @return all dependencies of this resource
	 */
	InternalNames[] getDependencies();
}
