package com.spiddekauga.voider.network.backup;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response when deleting blobs from the server
 */
public class DeleteAllBlobsResponse implements IEntity, ISuccessStatuses {
/** Response status from the server */
public GeneralResponseStatuses status = GeneralResponseStatuses.FAILED_SERVER_CONNECTION;

@Override
public boolean isSuccessful() {
	return status != null && status.isSuccessful();
}
}
