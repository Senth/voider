package com.spiddekauga.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String utilities
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Strings {
	/**
	 * Pads the string with the specified character to the right
	 * @param <T> type of message to write
	 * @param message the string/value to pad
	 * @param n number of spaces total the string should contain (including padded)
	 * @param padChar the character to pad with
	 * @return padded string
	 */
	public static <T> String padRight(T message, int n, char padChar) {
		String spaceString = padRight(message, n);

		// Find first space
		int firstSpace = spaceString.length();
		for (int i = spaceString.length() - 1; i <= 0; --i) {
			if (spaceString.charAt(i) != ' ') {
				firstSpace = i + 1;
				break;
			}
		}

		// Convert spaces to padChar
		String padString = spaceString.substring(0, firstSpace);
		if (firstSpace < spaceString.length()) {
			padString += spaceString.substring(firstSpace).replace(' ', padChar);
		}
		return padString;
	}

	/**
	 * Pads the string with the specified character to the left
	 * @param <T> type of message to write
	 * @param message the string/value to pad
	 * @param n number of spaces total the string should contain (including padded)
	 * @param padChar the character to pad with
	 * @return padded string
	 */
	public static <T> String padLeft(T message, int n, char padChar) {
		String spaceString = padLeft(message, n);

		// Find last space
		int firstNonSpace = spaceString.length();
		for (int i = 0; i < spaceString.length(); ++i) {
			if (spaceString.charAt(i) != ' ') {
				firstNonSpace = i;
				break;
			}
		}

		// Convert spaces to padChar
		String padString = spaceString.substring(0, firstNonSpace).replace(' ', padChar);
		if (firstNonSpace < spaceString.length()) {
			padString += spaceString.substring(firstNonSpace);
		}
		return padString;
	}

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
	 * @return string list separated by the delimiter
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

	/**
	 * Display number of seconds in minutes
	 * @param seconds number of seconds
	 * @return string time in minutes
	 */
	public static String secondsToTimeString(int seconds) {
		String minuteString = String.valueOf(seconds / 60);
		String secondString = String.valueOf(seconds % 60);

		// Append zeroes
		if (minuteString.length() == 1) {
			minuteString = "0" + minuteString;
		}
		if (secondString.length() == 1) {
			secondString = "0" + secondString;
		}

		return minuteString + ":" + secondString;
	}

	/**
	 * Count words number of words in a text
	 * @param text
	 * @return number of words in the text
	 */
	public static int wordCount(String text) {
		int cWords = 0;
		boolean prevCharWasWhitespace = true;
		for (int i = 0; i < text.length(); i++) {
			if (isWhitespace(text.charAt(i))) {
				prevCharWasWhitespace = true;
			} else {
				if (prevCharWasWhitespace) {
					cWords++;
				}
				prevCharWasWhitespace = false;

			}
		}
		return cWords;
	}

	/**
	 * Checks if a character is a whitespace or not
	 * @param character
	 * @return true if whitespace, false if not
	 */
	public static boolean isWhitespace(char character) {
		return character == ' ' || character == '\n' || character == '\t';
	}

	/** Vowel pattern */
	private static Pattern mVowelPattern = Pattern.compile("[aeiouAEIOU]");
}
