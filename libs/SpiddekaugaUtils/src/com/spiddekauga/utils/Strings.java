package com.spiddekauga.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String utilities
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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

	/**
	 * Stack trace to string
	 * @param exception the exception to get the stack trace from
	 * @return stack trace in a string
	 */
	public static String stackTraceToString(Exception exception) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		exception.printStackTrace(printWriter);
		return stringWriter.toString();
	}

	/**
	 * @param word the word to check if it begins with a vowel or consonant.
	 * @return true if the word begins with a wovel, i.e. uses
	 */
	public static boolean beginsWithWovel(String word) {
		if (word != null && word.length() > 0) {
			Matcher matcher = mVowelPattern.matcher(word.substring(0, 1));
			return matcher.find();
		} else {
			return false;
		}
	}

	/** Vowel pattern */
	private static Pattern mVowelPattern = Pattern.compile("[aeiouAEIOU]");
}
