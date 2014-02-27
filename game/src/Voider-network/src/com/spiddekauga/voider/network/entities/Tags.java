package com.spiddekauga.voider.network.entities;

/**
 * All tags
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public enum Tags {


	// NEXT ID TO USE =>

	;
	/**
	 * @param name Human readable name of the tag
	 * @param id unique id of the tag, used for storing in datastore
	 */
	Tags(String name, int id) {
		mName = name;
		mId = id;
	}

	/**
	 * @return id used for storing in datastore, i.e. not same as ordinal.
	 */
	public int getId() {
		return mId;
	}

	@Override
	public String toString() {
		return mName;
	}

	/** Human readable name */
	private String mName;
	/** Id used for storing in datastore */
	private int mId;
}
