package com.spiddekauga.voider.network.misc;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Gets an upload URL message
 */
public class GetUploadUrlMethod implements IMethodEntity {
/** Redirect method after upload is done */
public String redirectMethod = null;

@Override
public MethodNames getMethodName() {
	return MethodNames.GET_UPLOAD_URL;
}
}
