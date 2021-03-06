package com.spiddekauga.voider.repo;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Web response listener for callers
 */
public interface IResponseListener {
/**
 * Handle the web response
 * @param method the method that was called on the server
 * @param response the actual web response
 */
void handleWebResponse(IMethodEntity method, IEntity response);
}
