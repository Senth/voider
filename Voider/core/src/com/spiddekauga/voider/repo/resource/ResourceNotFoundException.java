package com.spiddekauga.voider.repo.resource;

import com.spiddekauga.voider.resources.ResourceException;

import java.util.UUID;


/**
 * An exception that is thrown when the specified resource could not be found
 */
public class ResourceNotFoundException extends ResourceException {
/** For java serialization */
private static final long serialVersionUID = -6894610361348804648L;

/**
 * Constructor that takes the resource which could not be found
 * @param filename the resource's filename
 */
public ResourceNotFoundException(String filename) {
	this(filename, null);
}

/**
 * Constructor that takes the resource which could not be found
 * @param filename the resource's filename
 * @param resourceId UUID of the resource that wasn't found, can be null
 */
public ResourceNotFoundException(String filename, UUID resourceId) {
	super(filename, resourceId);
}

/**
 * Constructor that takes the resource which could not be found
 * @param resourceId UUID of the resource that wasn't found, can be null
 */
public ResourceNotFoundException(UUID resourceId) {
	super(null, resourceId);
}

/**
 * @return the name of the file that couldn't be found
 */
@Override
public String toString() {
	return "ResourceNotFoundException: " + getMessage();
}

}
