package com.spiddekauga.voider.network.entities.misc;

import com.spiddekauga.voider.network.entities.IMethodEntity;

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
