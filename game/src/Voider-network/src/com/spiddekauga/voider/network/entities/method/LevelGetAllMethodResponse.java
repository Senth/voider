package com.spiddekauga.voider.network.entities.method;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelInfoEntity;

/**
 * All levels that matched the query
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LevelGetAllMethodResponse implements IEntity {
	/** All levels */
	public ArrayList<LevelInfoEntity> levels = new ArrayList<>();
	/** Datastore cursor to continue the query */
	public String cursor = null;
	/** Status of the response */
	public Statuses status = null;

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

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}
