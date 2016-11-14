package com.spiddekauga.voider.network.backup;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response when restoring blobs
 */
public class RestoreBlobsResponse implements IEntity, ISuccessStatuses {
/** Response status */
public GeneralResponseStatuses status = GeneralResponseStatuses.FAILED_SERVER_CONNECTION;
/** Error message, null if #isSuccessful() returns true */
public String errorMessage = null;

@Override
public boolean isSuccessful() {
	return status != null && status.isSuccessful();
}
}
