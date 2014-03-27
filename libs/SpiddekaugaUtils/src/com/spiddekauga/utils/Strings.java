package com.spiddekauga.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
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

	/**
	 * Convert an exception to string
	 * @param throwable the exception
	 * @return stack trace of throwable as a string
	 */
	public static String exceptionToString(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		return sw.toString();
	}

	/**
	 * text line breaks to HTML line breaks
	 * @param text regular text
	 * @return HTML formatted text
	 */
	public static String toHtmlString(String text) {
		return text.replace("\n", "<br />");
	}

	/**
	 * Create a string list of a list
	 * @param list the list to create a string list from
	 * @param delimiter how to delimit the elements
	 * @return string list seperated by the delimiter
	 */
	public static String toStringList(List<?> list, String delimiter) {
		String stringList = "";

		Iterator<?> iterator = list.iterator();
		while (iterator.hasNext()) {
			stringList += iterator.next().toString();

			if (iterator.hasNext()) {
				stringList += delimiter;
			}
		}

		return stringList;
	}

	/** Vowel pattern */
	private static Pattern mVowelPattern = Pattern.compile("[aeiouAEIOU]");
}
