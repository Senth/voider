package com.spiddekauga.voider.network.entities.method;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response of publish method.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class PublishMethodResponse implements IEntity, ISuccessStatuses {
	/** If publish was successful */
	public Statuses status = null;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	/**
	 * Return statuses of the method
	 */
	public enum Statuses implements ISuccessStatuses {
		/** Successfully publish the resources */
		SUCCESS,
		/** Failed internal server error */
		FAILED_SERVER_ERROR,
		/** Failed could not connect to the server */
		FAILED_SERVER_CONNECTION,
		/** User is not logged in */
		FAILED_USER_NOT_LOGGED_IN,

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}