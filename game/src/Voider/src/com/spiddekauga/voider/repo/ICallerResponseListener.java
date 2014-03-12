package com.spiddekauga.voider.repo;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;

/**
 * Web response listener for callers
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface ICallerResponseListener {
	/**
	 * Handle the web response
	 * @param method the method that was called on the server
	 * @param response the actual web response
	 */
	void handleWebResponse(IMethodEntity method, IEntity response);
}
