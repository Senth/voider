package com.spiddekauga.voider.resources;


/**
 * An exception that is thrown when the specified resource has
 * been corrupted and cannot be loaded
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceCorruptException extends RuntimeException {
	/**
	 * Constructor that takes the resource that was corrupted
	 * @param filename the resource's filename
	 */
	public ResourceCorruptException(String filename) {
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
	private static final long serialVersionUID = -8265952225410023281L;

}
