package com.spiddekauga.voider.network.backup;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response when restoring blobs
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class RestoreBlobsResponse implements IEntity, ISuccessStatuses {
	/** Response status */
	public GeneralResponseStatuses status = GeneralResponseStatuses.FAILED_SERVER_CONNECTION;

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	private static final long serialVersionUID = 1L;
}
