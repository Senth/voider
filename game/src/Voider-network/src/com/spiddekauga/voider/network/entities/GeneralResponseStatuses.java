package com.spiddekauga.voider.network.entities;


/**
 * General response statuses
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum GeneralResponseStatuses implements ISuccessStatuses {
	/** Successfully fetched levels, fetched all. */
	SUCCESS,
	/** Failed due to some internal server error */
	FAILED_SERVER_ERROR,
	/** Failed to connect to the server */
	FAILED_SERVER_CONNECTION,
	/** User not logged in */
	FAILED_USER_NOT_LOGGED_IN,

	;

	@Override
	public boolean isSuccessful() {
		return this == SUCCESS;
	}

}
