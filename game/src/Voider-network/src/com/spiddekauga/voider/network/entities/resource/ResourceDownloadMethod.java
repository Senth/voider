package com.spiddekauga.voider.network.entities.resource;

import java.util.UUID;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method for downloading resources from the server
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ResourceDownloadMethod implements IMethodEntity {
	/** Id of the resource to download */
	public UUID resourceId;

	@Override
	public String getMethodName() {
		return "resource-download";
	}
}
