package com.spiddekauga.voider.network.entities.misc;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method for downloading a blob
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BlobDownloadMethod implements IMethodEntity {
	/** Blob to download */
	public String blobKey;

	@Override
	public String getMethodName() {
		return "blob-download";
	}
}
