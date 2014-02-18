package com.spiddekauga.voider.resources;

import java.util.UUID;


/**
 * An exception that is thrown when the specified resource has
 * been corrupted and cannot be loaded
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceCorruptException extends ResourceException {
	/**
	 * Constructor that takes the resource that was corrupted
	 * @param filename the resource's filename
	 */
	public ResourceCorruptException(String filename) {
		this(filename, null);
	}

	/**
	 * Constructor that takes the resource that was corrupted
	 * @param filename the resource's filename
	 * @param resourceId UUID of the resource, if null will try to get the UUID from
	 * the filename
	 */
	public ResourceCorruptException(String filename, UUID resourceId) {
		super(filename, resourceId);
	}

	/**
	 * @return the name of the file that couldn't be found
	 */
	@Override
	public String toString() {
		return "ResourceCorruptException: " + getMessage();
	}

	/** For java serialization */
	private static final long serialVersionUID = -8265952225410023281L;

}
