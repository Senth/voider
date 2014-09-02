package com.spiddekauga.voider.network.entities.resource;

import java.util.Date;

/**
 * Resource blob entity with revision number
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ResourceRevisionBlobEntity extends ResourceBlobEntity {
	/** Revision of the resource */
	public int revision;
	/** Creation date of the resource */
	public Date created;
}
