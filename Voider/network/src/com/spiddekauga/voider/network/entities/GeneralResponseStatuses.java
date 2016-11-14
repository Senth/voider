package com.spiddekauga.voider.network.entities;


/**
 * General response statuses
 */
public enum GeneralResponseStatuses implements ISuccessStatuses {
	/** Server method completed successfully. */
	SUCCESS,
	/** Partial success */
	SUCCESS_PARTIAL,
	/** Failed due to some internal server error */
	FAILED_SERVER_ERROR,
	/** Failed to connect to the server */
	FAILED_SERVER_CONNECTION,
	/** User not logged in */
	FAILED_USER_NOT_LOGGED_IN,;

@Override
public boolean isSuccessful() {
	return this == SUCCESS;
}

}
