package com.spiddekauga.voider.network.resource;


/**
 * Resource blob entity with revision number
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceRevisionBlobEntity extends ResourceBlobEntity {
	private static final long serialVersionUID = 2L;
	/** Revision of the resource */
	public int revision;

}
