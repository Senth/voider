package com.spiddekauga.voider.version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a buffer and adds the result to a version container

 */
public class VersionParser {
	/**
	 * Creates the version parser
	 * @param input buffer input
	 */
	public VersionParser(InputStream input) {
		mInput = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
	}

	/**
	 * Creates the version parser
	 * @param input buffer input
	 */
	public VersionParser(BufferedReader input) {
		mInput = input;
	}

	/**
	 * Parse the stream
	 * @return all versions in the stream
	 */
	public VersionContainer parse() {
		// Read until end of buffer
		try {
			for (String line = mInput.readLine(); line != null; line = mInput.readLine()) {
				// Parse line if it's not only whitespaces
				if (!line.trim().isEmpty()) {
					parseLine(line);
				}
			}
		} catch (IOException e) {
			// Dose nothing
		}
		close();
		addParsingVersion();

		return mContainer;
	}

	/**
	 * Parse a line
	 * @param line the line that was read
	 */
	private void parseLine(String line) {
		// New version
		if (line.charAt(0) == '[') {
			addParsingVersion();
			createNewVersion(line);
		}
		// Variables
		else if (line.contains("=")) {
			String lowerCasedLine = line.trim().toLowerCase();
			// Date
			if (lowerCasedLine.startsWith("date")) {
				parseDate(line);
			}
			// Update Required
			else if (lowerCasedLine.startsWith("required")) {
				parseRequired(line);
			}
			// Server hotfix
			else if (lowerCasedLine.startsWith("hotfix")) {
				parseHotfix(line);
			}
			// Changelog
			else {
				mParsingVersion.addChangeLine(line);
			}
		}
		// ChangeLog
		else {
			mParsingVersion.addChangeLine(line);
		}
	}

	/**
	 * Parse date
	 * @param line
	 */
	private void parseDate(String line) {
		String stringValue = getStringAfterEqual(line);
		try {
			Date date = DATE_FORMAT.parse(stringValue);
			mParsingVersion.setDate(date);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Parse update required
	 * @param line
	 */
	private void parseRequired(String line) {
		String stringValue = getStringAfterEqual(line);
		boolean value = Boolean.parseBoolean(stringValue);
		mParsingVersion.setUpdateRequired(value);
	}

	/**
	 * Parse server hotfix
	 * @param line
	 */
	private void parseHotfix(String line) {
		String stringValue = getStringAfterEqual(line);
		boolean value = Boolean.parseBoolean(stringValue);
		mParsingVersion.setServerHotfix(value);
	}

	/**
	 * Get the string after an equal '=' sign
	 * @param line
	 * @return text after the equal signed (trimmed)
	 */
	private static String getStringAfterEqual(String line) {
		Matcher matcher = AFTER_EQUAL_PATTERN.matcher(line);
		matcher.find();
		return matcher.group(1).trim();
	}

	/**
	 * Create a new parsing version
	 * @param line the line containing the version
	 */
	private void createNewVersion(String line) {
		Matcher matcher = VERSION_PATTERN.matcher(line);
		matcher.find();
		String versionString = matcher.group(1);
		mParsingVersion = new Version(versionString);
	}

	/**
	 * Add parsing version to the container
	 */
	private void addParsingVersion() {
		if (mParsingVersion != null && mParsingVersion.getChangeLog() != null) {
			mContainer.add(mParsingVersion);
			mParsingVersion = null;
		}
	}

	/**
	 * Close the input
	 */
	private void close() {
		try {
			mInput.close();
		} catch (IOException e) {
			// Does nothing
		}
	}

	private Version mParsingVersion = null;
	private VersionContainer mContainer = new VersionContainer();
	private BufferedReader mInput;

	private static final Pattern VERSION_PATTERN = Pattern.compile("\\[(.*)\\]");
	private static final Pattern AFTER_EQUAL_PATTERN = Pattern.compile("=(.*)");
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
}
