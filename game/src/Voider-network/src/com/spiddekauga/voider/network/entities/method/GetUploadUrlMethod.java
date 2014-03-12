package com.spiddekauga.voider.network.entities.method;

/**
 * Gets an upload URL message
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class GetUploadUrlMethod implements IMethodEntity {
	@Override
	public String getMethodName() {
		return "get-upload-url";
	}

	/** Redirect method after upload is done */
	public String redirectMethod = null;
}
