package com.spiddekauga.voider.network.backup;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Restores the blobs to the server.

 */
public class RestoreBlobsMethod implements IMethodEntity {
	@Override
	public MethodNames getMethodName() {
		return MethodNames.RESTORE_BLOBS;
	}
}
