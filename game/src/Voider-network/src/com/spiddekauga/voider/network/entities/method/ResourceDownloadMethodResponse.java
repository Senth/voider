package com.spiddekauga.voider.network.entities.method;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ResourceFileEntity;

/**
 * Response for downloading a resource
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ResourceDownloadMethodResponse implements IEntity {
	/** Status of the response */
	public Statuses status = null;
	/** All files to download */
	public ArrayList<ResourceFileEntity> resources = new ArrayList<>();


	/**
	 * Response statuses
	 */
	public enum Statuses implements ISuccessStatuses {
		/** Found resource and its dependencies */
		SUCCESS,
		/** Failed by internal server error */
		FAILED_SERVER_INTERAL,
		/** Failed to connect to the server */
		FAILED_CONNECTION,;

		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}
