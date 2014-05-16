package com.spiddekauga.voider.network.entities;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Wrapper for resource a resource revision to upload or download
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ResourceRevisionEntity implements IEntity {
	/** Resource id */
	public UUID resourceId;
	/** Type */
	public UploadTypes type;
	/** Revisions */
	public ArrayList<RevisionEntity> revisions = new ArrayList<>();
}
