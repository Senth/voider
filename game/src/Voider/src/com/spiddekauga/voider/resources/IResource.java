package com.spiddekauga.voider.resources;

import java.util.UUID;

/**
 * Holds a unique id
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResource {
	/**
	 * @return a unique id for the type
	 */
	public UUID getId();
}
