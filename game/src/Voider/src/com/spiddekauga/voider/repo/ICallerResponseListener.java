package com.spiddekauga.voider.repo;

/**
 * Web response listener for callers
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface ICallerResponseListener {
	/**
	 * Handle the web response
	 * @param webResponse the actual web response
	 */
	void handleWebResponse(Object webResponse);
}
