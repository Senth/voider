package com.spiddekauga.voider.network.backup;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Restores the blobs to the server.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class RestoreBlobsMethod implements IMethodEntity {
	@Override
	public MethodNames getMethodName() {
		return MethodNames.RESTORE_BLOBS;
	}
}
