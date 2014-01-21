package com.spiddekauga.utils;

/**
 * Handles exceptions
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IExceptionHandler {
	/**
	 * Handle the exception
	 * @param exception the exception to handle
	 */
	void handleException(Exception exception);
}
