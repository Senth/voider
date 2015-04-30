package com.spiddekauga.voider.network.resource;

import java.util.UUID;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method for downloading resources from the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ResourceDownloadMethod implements IMethodEntity {
	/** Id of the resource to download */
	public UUID resourceId;
	/** Optional revision, only used if redownload is set to true */
	public int revision = -1;
	/**
	 * True if this is a redownload, i.e. it will only download this resource, no
	 * dependencies
	 */
	public boolean redownload = false;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.RESOURCE_DOWNLOAD;
	}
}
