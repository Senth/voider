package com.spiddekauga.voider.network.entities;

import java.util.HashMap;

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

	/**
	 * Converts an integer id to an enumeration
	 * @param id tag id of the enumeration to get the actual enumeration fro
	 * @return enumeration of the tag id
	 */
	public static Tags getEnumFromId(int id) {
		return mIdToEnum.get(mIdToEnum);
	}

	/** Human readable name */
	private String mName;
	/** Id used for storing in datastore */
	private int mId;
	/** From id to enum */
	private static HashMap<Integer, Tags> mIdToEnum = new HashMap<>();

	static {
		for (Tags tag : Tags.values()) {
			mIdToEnum.put(tag.mId, tag);
		}
	}
}
