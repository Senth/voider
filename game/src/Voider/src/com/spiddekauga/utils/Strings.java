package com.spiddekauga.utils;

/**
 * String utilities
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Strings {
	/**
	 * Pads the string with empty spaces to the right
	 * @param <T> type of the message to write
	 * @param message the string/value to pad
	 * @param n number of spaces total the string should contain (including padded)
	 * @return padded string
	 */
	public static <T> String padRight(T message, int n) {
		return String.format("%1$-" + n + "s", message);
	}

	/**
	 * Pads the string with empty spaces to the left
	 * @param <T> type of the message to write
	 * @param message the string/value to pad
	 * @param n number of spaces total the string should contain (including padded)
	 * @return padded string
	 */
	public static <T> String padLeft(T message, int n) {
		return String.format("%1$" + n + "s", message);
	}
}
