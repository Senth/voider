package com.spiddekauga.voider.network.misc;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method for downloading a blob
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BlobDownloadMethod implements IMethodEntity {
	/**
	 * Default constructor
	 */
	public BlobDownloadMethod() {
	}

	/**
	 * Sets the blob key
	 * @param blobKey
	 */
	public BlobDownloadMethod(String blobKey) {
		this.blobKey = blobKey;
	}

	/** Blob to download */
	public String blobKey;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.BLOB_DOWNLOAD;
	}
}
