package com.spiddekauga.utils;

/**
 * Handles exceptions
 */
public interface IExceptionHandler {
/**
 * Handle the exception
 * @param exception the exception to handle
 */
void handleException(RuntimeException exception);
}
