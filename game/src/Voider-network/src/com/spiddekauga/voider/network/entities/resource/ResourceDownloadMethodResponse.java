package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response for downloading a resource
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ResourceDownloadMethodResponse implements IEntity, ISuccessStatuses {
	/** Status of the response */
	public Statuses status = null;
	/** All files to download */
	public ArrayList<ResourceBlobEntity> resources = new ArrayList<>();

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	/**
	 * Response statuses
	 */
	public enum Statuses implements ISuccessStatuses {
		/** Found resource and its dependencies */
		SUCCESS,
		/** Failed by internal server error */
		FAILED_SERVER_INTERAL,
		/** Failed to connect to the server */
		FAILED_CONNECTION,
		/** Failed during download */
		FAILED_DOWNLOAD,
		/** User is not logged in */
		FAILED_USER_NOT_LOGGED_IN,

		;

		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}
