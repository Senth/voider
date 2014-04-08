package com.spiddekauga.voider.network.entities;

import java.util.UUID;

/**
 * Wrapper class for a resource and its type
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ResourceBlobEntity implements IEntity {
	/** Resource id */
	public UUID resourceId;
	/** Blob key */
	public String blobKey;
	/** Resource type */
	public UploadTypes uploadType;
	/** True if successfully downloaded */
	public boolean downloaded = false;
}
