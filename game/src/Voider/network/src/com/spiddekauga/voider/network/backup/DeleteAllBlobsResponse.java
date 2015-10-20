package com.spiddekauga.voider.network.backup;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response when deleting blobs from the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class DeleteAllBlobsResponse implements IEntity, ISuccessStatuses {
	/** Response status from the server */
	public GeneralResponseStatuses status = GeneralResponseStatuses.FAILED_SERVER_CONNECTION;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	private static final long serialVersionUID = 1L;
}
