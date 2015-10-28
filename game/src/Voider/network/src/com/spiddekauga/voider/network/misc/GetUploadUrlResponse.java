package com.spiddekauga.voider.network.misc;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Response from GetUploadUrlMethod
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class GetUploadUrlResponse implements IEntity {
	/** The upload url */
	public String uploadUrl = null;
}
