package com.spiddekauga.voider.network.stat;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response from syncing statistics
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class StatSyncResponse implements IEntity, ISuccessStatuses {
	private static final long serialVersionUID = 1L;
	/** Stats to sync to client */
	public StatSyncEntity syncEntity = new StatSyncEntity();
	/** Response status */
	public GeneralResponseStatuses status = null;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}
}
