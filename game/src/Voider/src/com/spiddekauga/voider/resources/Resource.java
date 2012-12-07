package com.spiddekauga.voider.resources;

import java.util.UUID;

/**
 * All dynamic resources need to derive from this kind of resource
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Resource {
	/** A unique id for the resource */
	private UUID mUniqueId;
	/** Dependencies for the resource */
	private Resource[] mDependencies;
}
