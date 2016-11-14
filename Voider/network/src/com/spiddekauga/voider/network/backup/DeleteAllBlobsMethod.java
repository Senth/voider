package com.spiddekauga.voider.network.backup;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Deletes all blobs

 */
public class DeleteAllBlobsMethod implements IMethodEntity {
	/** Secret key for deleting all blobs, not very secure, but secure enough */
	public long key = 15665486L;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.DELETE_ALL_BLOBS;
	}
}
