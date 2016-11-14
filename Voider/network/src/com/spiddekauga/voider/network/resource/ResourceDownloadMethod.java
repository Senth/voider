package com.spiddekauga.voider.network.resource;

import com.spiddekauga.voider.network.entities.IMethodEntity;

import java.util.UUID;

/**
 * Method for downloading resources from the server
 */
public class ResourceDownloadMethod implements IMethodEntity {
/** Id of the resource to download */
public UUID resourceId;
/** Optional revision, only used if redownload is set to true */
public int revision = -1;
/**
 * True if this is a redownload, i.e. it will only download this resource, no dependencies
 */
public boolean redownload = false;

@Override
public MethodNames getMethodName() {
	return MethodNames.RESOURCE_DOWNLOAD;
}
}
