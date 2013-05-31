package com.spiddekauga.voider.resources;

import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Thrown when an undefined resource type has been used
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class UndefinedResourceTypeException extends GdxRuntimeException {

	/**
	 * Constructor with the undefined resource type
	 * @param undefinedResourceType
	 */
	public UndefinedResourceTypeException(Class<?> undefinedResourceType) {
		super(undefinedResourceType.getName());
		mUndefinedResourceType = undefinedResourceType;
	}

	/**
	 * Prints the undefined resource type
	 */
	@Override
	public String toString() {
		return "Undefined resource type: " + mUndefinedResourceType.getName();
	}

	/**
	 * @return undefined resource type
	 */
	public Class<?> getUndefinedResourceType() {
		return mUndefinedResourceType;
	}


	/** The undefined resource type */
	private Class<?> mUndefinedResourceType;
	/** For serialization */
	private static final long serialVersionUID = -7748090339503068623L;
}
