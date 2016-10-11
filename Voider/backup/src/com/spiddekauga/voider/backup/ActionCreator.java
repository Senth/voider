package com.spiddekauga.voider.backup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;


/**
 * Parses the argument for backup and creates the appropriate action
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class ActionCreator {
	/**
	 * @param args arguments from the application
	 */
	ActionCreator(String[] args) {
		mArgs = args;
	}

	/**
	 * Create an action from the application arguments
	 * @return created action
	 */
	Action createAction() {
		while (hasMoreArgumentsToParse()) {
			parseNextArgument();
		}

		// Not all arguments have been set
		if (mAction != null && !mAction.isAllArgumentsSet()) {
			fail("Missing arguments for " + mArgs[0]);
		}


		return mAction;
	}

	/**
	 * Parse next argument
	 */
	private void parseNextArgument() {
		String argument = mArgs[mParseIndex];

		// Should be backup or restore
		if (mParseIndex == 0) {
			argument = argument.toLowerCase();

			// Backup
			if (argument.equals("backup")) {
				mAction = new BackupAction();
			}
			// Restore
			else if (argument.equals("restore")) {
				mAction = new RestoreAction();
			}
			// Test action
			else if (argument.equals("test")) {
				mAction = new TestAction();
			}
			// Failed
			else {
				fail("Invalid first argument");
			}
		}
		// Backup
		else if (mAction instanceof BackupAction) {
			parseNextBackupArgument();
		}
		// Restore
		else if (mAction instanceof RestoreAction) {
			parseNextRestoreArgument();
		}
		// Test
		else if (mAction instanceof TestAction) {
			parseNextBackupArgument();
		}
		// Fail
		else {
			fail("Unknown error");
		}

		mParseIndex++;
	}

	/**
	 * Parse next backup argument
	 */
	private void parseNextBackupArgument() {
		String argument = mArgs[mParseIndex];

		// URL
		if (mParseIndex == 1) {
			setUrl(argument);
		}
		// Backup directory
		else if (mParseIndex == 2) {
			setBackupDir(argument);
		}
	}

	/**
	 * Tries to set the URL for the current action
	 * @param url
	 */
	private void setUrl(String url) {
		if (url.startsWith("http://") || url.startsWith("https://")) {
			mAction.setUrl(url);
		} else {
			fail("invalid URL");
		}
	}

	/**
	 * Tries to set the backup directory for the current action
	 * @param backupDir
	 */
	private void setBackupDir(String backupDir) {
		boolean success = mAction.setBackupDir(backupDir);
		if (!success) {
			fail("Invalid backup directory");
		}
	}

	/**
	 * Parse next restore argument
	 */
	private void parseNextRestoreArgument() {
		String argument = mArgs[mParseIndex];

		// DATE
		if (mParseIndex == 1) {
			setDate(argument);
		}
		// URL
		else if (mParseIndex == 2) {
			setUrl(argument);
		}
		// Backup directory
		else if (mParseIndex == 3) {
			setBackupDir(argument);
		}
	}

	/**
	 * Tries to set the date of the current restore action from a date string
	 * @param dateString
	 */
	private void setDate(String dateString) {
		if (mAction instanceof RestoreAction) {
			RestoreAction restoreAction = (RestoreAction) mAction;

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
			try {
				Date date = formatter.parse(dateString);
				restoreAction.setDate(date);
			} catch (ParseException e) {
				fail("invalid date format: " + e.getMessage());
			}
		}
	}

	/**
	 * Fail parsing
	 * @param errorMessage the error message
	 */
	private void fail(String errorMessage) {
		mLogger.severe(errorMessage);
		mFailedParsing = true;
		mAction = null;
	}

	/**
	 * @return true if we have more arguments to parse
	 */
	private boolean hasMoreArgumentsToParse() {
		return !mFailedParsing && mParseIndex < mArgs.length;
	}

	private static final Logger mLogger = Logger.getLogger(ActionCreator.class.getSimpleName());
	private boolean mFailedParsing = false;
	private String[] mArgs;
	private Action mAction = null;
	private int mParseIndex = 0;
}