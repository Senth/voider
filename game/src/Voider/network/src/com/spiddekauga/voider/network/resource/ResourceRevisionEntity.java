package com.spiddekauga.voider.network.resource;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Wrapper for resource a resource revision to upload or download
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceRevisionEntity implements IEntity {
	/** Resource id */
	public UUID resourceId;
	/** Type */
	public UploadTypes type;
	/** Revisions */
	public ArrayList<RevisionEntity> revisions = new ArrayList<>();
}
