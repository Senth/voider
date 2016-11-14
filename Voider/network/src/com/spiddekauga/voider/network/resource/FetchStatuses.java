package com.spiddekauga.voider.network.resource;

import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Statuses
 */
public enum FetchStatuses implements ISuccessStatuses {
	/** Successfully fetched levels and there's more */
	SUCCESS_MORE_EXISTS,
	/** Successfully fetched levels, fetched all. */
	SUCCESS_FETCHED_ALL,
	/** Failed due to some internal server error */
	FAILED_SERVER_ERROR,
	/** Failed to connect to the server */
	FAILED_SERVER_CONNECTION,
	/** User not logged in */
	FAILED_USER_NOT_LOGGED_IN,;

@Override
public boolean isSuccessful() {
	return name().contains("SUCCESS");
}
}