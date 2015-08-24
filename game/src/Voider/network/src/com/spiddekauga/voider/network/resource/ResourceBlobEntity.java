package com.spiddekauga.voider.network.resource;

import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Wrapper class for a resource and its type
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceBlobEntity implements IEntity {
	private static final long serialVersionUID = 2L;
	/** Resource id */
	public UUID resourceId;
	/** Blob key */
	public String blobKey;
	/** Resource type */
	public UploadTypes uploadType;
	/** Creation date of the resource */
	public Date created;
	/** True if successfully downloaded */
	public boolean downloaded = false;
}
