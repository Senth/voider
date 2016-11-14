package com.spiddekauga.voider.network.resource;

import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Wrapper class for a resource and its type

 */
public class ResourceBlobEntity implements IEntity {
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
