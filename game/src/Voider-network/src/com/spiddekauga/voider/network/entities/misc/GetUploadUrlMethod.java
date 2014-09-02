package com.spiddekauga.voider.network.entities.misc;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Gets an upload URL message
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class GetUploadUrlMethod implements IMethodEntity {
	@Override
	public MethodNames getMethodName() {
		return MethodNames.GET_UPLOAD_URL;
	}

	/** Redirect method after upload is done */
	public String redirectMethod = null;
}
