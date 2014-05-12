package com.spiddekauga.voider.network.entities.method;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ResourceBlobEntity;

/**
 * All resources that should be downloaded (synced) from the server
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class SyncPublishMethodResponse implements IEntity {
	/** All published resources to download */
	public ArrayList<ResourceBlobEntity> resources = null;
	/** Status of the sync */
	public Statuses status = null;

	/** Response statuses */
	public enum Statuses implements ISuccessStatuses {
		/** Successfully tested */
		SUCCESS,
		/** User not logged in */
		FAILED_USER_NOT_LOGGED_IN,
		/** Failed internal server error */
		FAILED_INTERNAL,
		/** Connection failed */
		FAILED_CONNECTION,

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}
