package com.spiddekauga.voider.network.entities.method;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Response from GetUploadUrlMethod
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class GetUploadUrlMethodResponse implements IEntity {
	/** The upload url */
	public String uploadUrl = null;
}
