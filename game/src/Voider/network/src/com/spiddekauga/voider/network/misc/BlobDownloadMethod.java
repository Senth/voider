package com.spiddekauga.voider.network.misc;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method for downloading a blob
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class BlobDownloadMethod implements IMethodEntity {
	/** Blob to download */
	public String blobKey;

	private static final long serialVersionUID = 1L;

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

	@Override
	public MethodNames getMethodName() {
		return MethodNames.BLOB_DOWNLOAD;
	}
}
