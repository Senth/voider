package com.spiddekauga.voider.network.entities.method;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.BugReportEntity;
import com.spiddekauga.voider.network.entities.IEntity;
;

/**
 * Bug report response from the server
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BugReportMethodResponse implements IEntity {
	/** Bug reports that failed to send */
	public ArrayList<BugReportEntity> failedBugReports = new ArrayList<>();
	/** Response status */
	public Statuses status = null;

	/**
	 * Different response statuses
	 */
	public enum Statuses implements ISuccessStatuses {
		/** Successfully sent the bug reports */
		SUCCESS,
		/** Successfully some of the bug reports, at least one was not sent */
		SUCCESS_WITH_ERRORS,
		/** Internal server error */
		FAILED_SERVER_ERROR,
		/** Connection failed */
		FAILED_CONNECTION,;

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}
