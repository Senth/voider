package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response of publish method.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class PublishMethodResponse implements IEntity, ISuccessStatuses {
	/** If publish was successful */
	public Statuses status = null;
	/** Already published resource */
	public ArrayList<UUID> alreadyPublished = new ArrayList<>();

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
		/** One or several resources have already been published */
		FAILED_ALREADY_PUBLISHED,

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}