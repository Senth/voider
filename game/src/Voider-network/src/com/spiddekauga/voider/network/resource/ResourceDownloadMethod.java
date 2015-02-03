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

	@Override
	public MethodNames getMethodName() {
		return MethodNames.RESOURCE_DOWNLOAD;
	}
}
