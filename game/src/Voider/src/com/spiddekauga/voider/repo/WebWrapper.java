package com.spiddekauga.voider.repo;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;

/**
 * Wrapper class for a method entity and response entity
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class WebWrapper {
	/**
	 * Default constructor
	 */
	public WebWrapper() {
		// Does nothing
	}

	/**
	 * Sets both the method and response
	 * @param method method entity, i.e. method that was called on the server
	 * @param response response from the server
	 */
	public WebWrapper(IMethodEntity method, IEntity response) {
		this.method = method;
		this.response = response;
	}

	/** The method that was called */
	public IMethodEntity method = null;
	/** Response from the method */
	public IEntity response = null;
}
