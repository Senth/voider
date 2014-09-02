package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;
import com.spiddekauga.voider.network.entities.stat.LevelInfoEntity;

/**
 * All levels that matched the query
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelGetAllMethodResponse implements IEntity, ISuccessStatuses {
	/** All levels */
	public ArrayList<LevelInfoEntity> levels = new ArrayList<>();
	/** Datastore cursor to continue the query */
	public String cursor = null;
	/** Status of the response */
	public Statuses status = null;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	/**
	 * Statuses
	 */
	public enum Statuses implements ISuccessStatuses {
		/** Successfully fetched levels and there's more */
		SUCCESS_MORE_EXISTS,
		/** Successfully fetched levels, fetched all. */
		SUCCESS_FETCHED_ALL,
		/** Failed due to some internal server error */
		FAILED_SERVER_ERROR,
		/** Failed to connect to the server */
		FAILED_SERVER_CONNECTION,
		/** User not logged in */
		FAILED_USER_NOT_LOGGED_IN,

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}
