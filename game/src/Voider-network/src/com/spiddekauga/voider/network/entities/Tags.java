package com.spiddekauga.voider.network.entities;

import java.util.HashMap;

/**
 * All tags
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum Tags {
	/** Easy */
	EASY(1),
	/** Challenging to play */
	CHALLENGING(2),
	/** Contains some sort of story */
	STORY(3),
	/** Complicated */
	COMPLICATED(4),
	/** Artistically made */
	ARTISTIC(5),
	/** Bullet storm */
	BULLET_STORM(6),
	/** No enemies */
	ENEMYLESS(7, "Enemy-less"),
	/** Short */
	SHORT(8),
	/** Long */
	LONG(9),
	/** Lots of colors */
	COLORFUL(10),
	/** Narrow terrain */
	NARROW(11),
	/** Surprises */
	SURPRISE(12, "Surprise!"),
	/** Weird */
	WEIRD(13),
	/** Boring */
	BORING(14),
	/** Fun */
	FUN(15),



	// NEXT ID TO USE =>

	;
	/**
	 * @param id unique id of the tag, used for storing in datastore
	 */
	Tags(int id) {
		mId = id;

		// Convert name to lower case except first letter
		mName = String.valueOf(name().charAt(0));
		mName += name().substring(1).toLowerCase();

		// Replace underscore with space
		mName = mName.replace('_', ' ');
	}

	/**
	 * @param id unique id of the tag, used for storing in datastore
	 * @param name Human readable name of the tag
	 */
	Tags(int id, String name) {
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
		return mIdToEnum.get(id);
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
