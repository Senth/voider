package com.spiddekauga.utils;

/**
 * Handles exceptions
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IExceptionHandler {
	/**
	 * Handle the exception
	 * @param exception the exception to handle
	 * @param endScene set to true to end the scene afterwards
	 */
	void handleException(Exception exception, boolean endScene);
}
