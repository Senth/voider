package com.spiddekauga.voider.backup;

import java.util.Date;

/**
 * Restore actions from the Internet
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class RestoreAction extends Action {

	@Override
	public void execute() {
		// Clear all blobs on the server

		// For all files in the backup directory

		// Add file if it's before or on the restore date

		// If we have X number of files and upload them to the server
	}

	/**
	 * Set the date we want to restore to
	 * @param date
	 */
	void setDate(Date date) {
		mDate = date;
	}

	private Date mDate = null;
}
