package com.spiddekauga.voider.network.entities.method;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Bug report response from the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BugReportMethodResponse implements IEntity, ISuccessStatuses {
	/** Bug reports that failed to send */
	public ArrayList<UUID> failedBugReports = new ArrayList<>();
	/** Response status */
	public Statuses status = null;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

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
		FAILED_CONNECTION,
		/** User was not logged in */
		FAILED_USER_NOT_LOGGED_IN,

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}
