package com.spiddekauga.voider.network.resource;

import java.util.Date;

/**
 * Resource blob entity with revision number
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceRevisionBlobEntity extends ResourceBlobEntity {
	private static final long serialVersionUID = 1L;
	/** Revision of the resource */
	public int revision;
	/** Creation date of the resource */
	public Date created;
}
