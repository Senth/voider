package com.spiddekauga.voider.network.entities.method;

/**
 * Gets an upload URL message
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class GetUploadUrlMethod implements IMethodEntity {
	@Override
	public String getMethodName() {
		return "getuploadurl";
	}

	/** Redirect method after upload is done */
	public String redirectMethod = null;
}
