package com.spiddekauga.utils;

/**
 * Handles exceptions
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IExceptionHandler {
	/**
	 * Handle the exception
	 * @param exception the exception to handle
	 */
	void handleException(Exception exception);
}
