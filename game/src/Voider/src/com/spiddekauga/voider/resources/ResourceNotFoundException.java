package com.spiddekauga.voider.resources;


/**
 * An exception that is thrown when the specified resource could
 * not be found
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceNotFoundException extends RuntimeException {
	/**
	 * Constructor that takes the resource which could not be found
	 * @param filename the resource's filename
	 */
	public ResourceNotFoundException(String filename) {
		mFilename = filename;
	}

	/**
	 * @return the name of the file that couldn't be found
	 */
	@Override
	public String toString() {
		return mFilename;
	}

	/** Name of the resource */
	private String mFilename = null;
	/** For java serialization */
	private static final long serialVersionUID = -6894610361348804648L;

}
