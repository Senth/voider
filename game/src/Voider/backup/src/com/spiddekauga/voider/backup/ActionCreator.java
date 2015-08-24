package com.spiddekauga.voider.backup;


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
			// Backup
			if (argument.toLowerCase().equals("backup")) {
				mAction = new BackupAction();
			}
			// Restore
			else if (argument.toLowerCase().equals("restore")) {
				mAction = new RestoreAction();
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
		// Fail
		else {
			fail("Unknown error");
		}
	}

	/**
	 * Parse next backup argument
	 */
	private void parseNextBackupArgument() {
		String argument = mArgs[mParseIndex];

		// URL
		if (mParseIndex == 1) {
			// starts with http or https
			if (argument.startsWith("http://") || argument.startsWith("https://")) {
				mAction.setUrl(argument);
			} else {
				fail("invalid URL");
			}
		}
		// Backup directory
		else if (mParseIndex == 2) {
			boolean success = mAction.setBackupDir(argument);
			if (!success) {
				fail("Invalid backup directory");
			}
		}
	}

	/**
	 * Parse next restore argument
	 */
	private void parseNextRestoreArgument() {
		String argument = mArgs[mParseIndex];

		// TODO
	}

	/**
	 * Fail parsing
	 * @param errorMessage the error message
	 */
	private void fail(String errorMessage) {
		System.err.print(errorMessage);
		mFailedParsing = true;
		mAction = null;
	}

	/**
	 * @return true if we have more arguments to parse
	 */
	private boolean hasMoreArgumentsToParse() {
		return !mFailedParsing && mParseIndex < mArgs.length;
	}

	private boolean mFailedParsing = false;
	private String[] mArgs;
	private Action mAction = null;
	private int mParseIndex = 0;
}