package com.spiddekauga.voider.network.misc;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Gets an upload URL message
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class GetUploadUrlMethod implements IMethodEntity {
	@Override
	public MethodNames getMethodName() {
		return MethodNames.GET_UPLOAD_URL;
	}

	/** Redirect method after upload is done */
	public String redirectMethod = null;
}
