package com.spiddekauga.voider.network.resource;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Base class for all resource responses
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class FetchResponse implements IEntity, ISuccessStatuses {
	/** Datastore cursor to continue the query */
	public String cursor = null;
	/** Status of the response */
	public FetchStatuses status = FetchStatuses.FAILED_SERVER_ERROR;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}
}
