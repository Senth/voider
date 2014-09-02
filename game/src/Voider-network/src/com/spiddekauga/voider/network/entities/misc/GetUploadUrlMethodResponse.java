package com.spiddekauga.voider.network.entities.misc;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Response from GetUploadUrlMethod
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class GetUploadUrlMethodResponse implements IEntity {
	/** The upload url */
	public String uploadUrl = null;
}
