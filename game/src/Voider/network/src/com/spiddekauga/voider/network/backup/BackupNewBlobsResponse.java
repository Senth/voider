package com.spiddekauga.voider.network.backup;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;
import com.spiddekauga.voider.network.resource.ResourceBlobEntity;
import com.spiddekauga.voider.network.resource.ResourceRevisionBlobEntity;

/**
 * All the new blobs to backup.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class BackupNewBlobsResponse implements IEntity, ISuccessStatuses {
	/** Status of the blob */
	public GeneralResponseStatuses status = GeneralResponseStatuses.FAILED_SERVER_CONNECTION;
	/** New published blobs */
	public ArrayList<ResourceBlobEntity> publishedBlobs = new ArrayList<>();
	/** New user versions */
	public ArrayList<ResourceRevisionBlobEntity> userBlobs = new ArrayList<>();

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	private static final long serialVersionUID = 1L;
}
